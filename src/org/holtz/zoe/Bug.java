package org.holtz.zoe;

import java.awt.Color;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.holtz.zoe.zoel.Expression;
import org.holtz.zoe.zoel.Literal;
import org.holtz.zoe.zoel.Number;
import org.holtz.zoe.zoel.Operator;
import org.holtz.zoe.zoel.Register;
import org.holtz.zoe.zoel.RegisterReference;
import org.holtz.zoe.zoel.StringLiteral;
import org.holtz.zoe.zoel.ZoelVM;
import org.holtz.zoe.zoel.ZoelVMHost;

/**
 * A Zoe organism controlled by <code>Gene</code>s each executing on its own
 * <code>ZoelVM</code> virtual machines.
 * 
 * @author Brian Holtz
 */
public class Bug extends ZObject implements ZoelVMHost {

    public Bug             mother;
    public Bug             father;
    public Bug             lastMate;
    public ZObject         lastSensed;
    protected Genotype     genotype;
    public int             age;

    private ArrayList<Bug> children       = new ArrayList<Bug>();
    private Phenotype      phenotype;

    public double          diameter;
    public double          heading;
    public double          course;
    private double         strength;
    /**
     * Gaze in radians relative to heading.
     */
    public double          gaze;
    private int            lastCycleLooked;
    private double         massEnergyAfterLastTurn;
    private double         bittenSinceLastTurn;
    /**
     * Bug's memory, shared across all Phenes
     */
    private Map<Literal, Literal> heap = new LinkedHashMap<Literal, Literal>() {
        private static final long serialVersionUID = 201203182139L;
        protected boolean removeEldestEntry(Map.Entry<Literal,Literal> eldest) {
           return (size() > maxDataSize());
        }
    };

    private Point          birthPlace;
    private int            birthCycle     = -1;
    private int            deathCycle     = -1;
    private Bug            lastBiter;
    private Bug            killer;
    private int            kills;

    private static int     numEverCreated = 0;

    public Bug( World theWorld ) {
        super( theWorld );
        diameter = World.BugMinSize
            + world.random.nextInt( World.BugMaxSize - World.BugMinSize ) / 2;
        strength = world.random.nextFloat() * maxStrength();
        genotype = new Genotype( world.random );
        enterTheWorld();
    }

    public Bug( World theWorld, Genotype theGenotype ) {
        super( theWorld );
        if (theGenotype == Genotype.algae() && World.PlanktonDistributionRandomness < 1) {
            double distance = theWorld.radius() * World.PlanktonDistributionRandomness * world.random.nextFloat();
            setXY( theWorld.midpoint().x, theWorld.midpoint().y );
            move( world.random.nextFloat() * Math.PI, distance );
            System.out.println( String.format("%.0f\n", distance));
        }
        double cost = 2 * minNewbornEnergy();
        double birthMass = cost / 2;
        diameter = Math.sqrt( 4 * birthMass / Math.PI );
        strength = cost / 2;
        genotype = theGenotype;
        enterTheWorld();
    }

    private Bug(Bug mom, Bug dad, double strength2Invest) {
        super( mom.world );
        beBornFrom( mom, dad, strength2Invest );
        enterTheWorld();
    }

    private void enterTheWorld() {
        birthCycle = world.cycle;
        setCourse(world.random.nextFloat() * Math.PI * 2);
        setHeading( course );
        gaze = heading + (world.random.nextFloat() * Math.PI / 2)
                - (Math.PI / 4);
        gaze = Point.normalize( gaze );
        birthPlace = new Point( x(), y() );
        massEnergyAfterLastTurn = mass() + strength;
        genotype.addMember( this );
        phenotype = new Phenotype( this );
        world.add( this );
    }

    private void beBornFrom(Bug mom, Bug dad, double strength2Invest) {
        mother = mom;
        father = dad;
        lastSensed = mom;
        mother.lastSensed = this;
        setXY( mother.x(), mother.y() );
        setHeading(mother.heading + Math.PI);
        setCourse(mother.course + Math.PI);
        gaze = bearing( mother );
        if (dad != null) {
            if (World.ChildrenOfAMatingShareGenotype) {
                Bug sibling = mom.youngestChild();
                if (sibling != null && sibling.father == dad) genotype = sibling.genotype;
            }
            if (genotype == null) {
                // No sibling from this mating pair, so create new genotype
                genotype = new Genotype( mother.genotype, dad.genotype, world.random, location() );
            }
        } else if (world.random.nextFloat() < World.MutantChildrenFreq) {
            genotype = new Genotype( mother.genotype, world.random, location() );
        } else {
            genotype = mother.genotype;
        }
        double birthMass = strength2Invest / 2;
        diameter = Math.sqrt( 4 * birthMass / Math.PI );
        strength = strength2Invest / 2;
        grow( 0 ); // adjust our size if strength is too high or low
        double birthCost = (strength + mass()) / World.BirthEfficiency;
        mother.shrink( birthCost );
        mother.children.add( this );
    }

    @Override
    public void brownianMotion() {
        setHeading(heading + world.brownianMotion() * Math.PI / 16);
        setCourse( course  + world.brownianMotion() * Math.PI / 16 );
        super.brownianMotion();
    }

    @Override
    public double radius() {
        return diameter/2;
    }

    // Mass is area of circle with diameter of "size"
    @Override
    public double mass() {
        return mass( diameter );
    }

    private static double mass(double theSize) {
        return Math.PI * theSize * theSize / 4;
    }

    /**
     * The fraction of full strength at current size.
     * 
     * @return a double between 0 and 1.0.
     */
    public double strengthRatio() {
        return strength / maxStrength();
    }

    public double strength() {
        return strength;
    }

    public Genotype genotype() {
        return genotype;
    }
    
    public boolean isDead() {
        return diameter < World.BugMinSize;
    }

    public int birthCycle() {
        return birthCycle;
    }

    private static double minNewbornEnergy() {
        return mass( World.BugMinSize ) + maxStrength( World.BugMinSize );
    }

    private void pruneDeadSubtrees() {
        if (hasLivingDescendents()) {
            return;
        }
        if (isDead()) {
            children = null;
        }
        if (mother != null) mother.pruneDeadSubtrees();
    }

    public void disappear() {
        if (! isDead()) declareDeath();
        diameter = 0;
        strength = 0;
    }

    private void declareDeath() {
        genotype.numLiving--;
        deathCycle = world.cycle;
        pruneDeadSubtrees();
    }

    private boolean hasLivingDescendents() {
        if (children == null) return false;
        for (Bug child : children) {
            if (!child.isDead()) return true;
        }
        for (Bug child : children) {
            if (child.hasLivingDescendents()) return true;
        }
        return false;
    }

    public int numLivingDescendents() {
        if (children == null) return 0;
        int count = 0;
        for (Bug child : children) {
            if (!child.isDead()) count++;
            count += child.numLivingDescendents();
        }
        return count;
    }

    // maxStrength is area of circle with diameter of "size"
    private static double maxStrength(double theSize) {
        return Math.PI * theSize * theSize / 4;
    }

    private double maxStrength() {
        return maxStrength( diameter );
    }

    private double biteSize() {
        // return mass() * World.BiteFractionOfOwnMass;
        return diameter * Math.PI * World.BiteFractionOfOwnCircumference;
    }

    private void shrink(double howMuch) {
        grow( -howMuch );
    }

    private void grow(double extraStrength) {
        strength += extraStrength;
        if (strength > 0 && strength < maxStrength()) return;
        double massEnergy = mass() + strength;
        if (strength < 0) {
            // shrink
            diameter = Math.sqrt( 4 * massEnergy / Math.PI );
            strength = 0;
        } else {
            // grow
            diameter = 2 * Math.sqrt( massEnergy / 2 / Math.PI );
            strength = maxStrength();
        }
    }

    private double bite(Joule joule) {
        double oldSize = diameter;
        assert joule != null : "Biting null joule";
        double maxUsableBite = maxStrength( World.BugMaxSize ) - strength;
        double toBite = Math.min( biteSize(), maxUsableBite );
        double strengthBitten = Math.min( toBite, joule.joules );
        grow( World.BiteEfficiency * strengthBitten );
        assert strength >= 0 : "Negative strength after biting " + joule;
        joule.getBit(strengthBitten);
        assert oldSize <= diameter : "bug shrank by biting a joule";
        return strengthBitten;
    }

    private double bite(Bug victim) {
        if (victim == null) return 0;
        boolean bugWasAlive = !victim.isDead();
        double oldDiam = diameter;
        double oldStrength = strength;
        double oldBugStrength = victim.strength;
        double oldBugDiam = victim.diameter;

        double maxUsableBite = maxStrength( World.BugMaxSize ) - strength;
        double toBite = Math.min( biteSize(), maxUsableBite );
        double strengthBitten = Math.min( toBite, victim.strength + victim.mass() );
        grow( World.BiteEfficiency * strengthBitten );
        assert strength >= 0 : "negative after strengthBitten="
                + strengthBitten;
        victim.shrink( strengthBitten );
        victim.bittenSinceLastTurn += strengthBitten;

        if (bugWasAlive && victim.isDead()) {
            victim.killer = this;
            kills++;
        }
        victim.lastBiter = this;

        if (World.Trace) { 
            String msg = world.cycle + ": " + id + " ("
                + String.format( "%5.1f", oldDiam ) + " +"
                + String.format( "%5.1f", oldStrength ) + " => "
                + String.format( "%5.1f", diameter )
                + String.format( "%5.1f", strength ) + ")" + " bit " + victim.id
                + " (" + String.format( "%5.1f", oldBugStrength ) + " + "
                + String.format( "%5.1f", oldBugDiam ) + " => "
                + String.format( "%5.1f", victim.strength ) + " + "
                + String.format( "%5.1f", victim.diameter ) + ")";
            System.err.println( msg );
        }
        assert victim.strength >= 0 : "being bit left negative strength";
        assert oldDiam <= diameter + 0.001 : "bug shrank by biting another bug";
        return strengthBitten;
    }

    private Bug mate() {
        Bug closest = world.closestBug(this, radius());
        lastSensed = lastMate = closest;
        // if (lastMate != null) lastMate.lastMate = this;  // allow rape?
        return lastMate;
    }

    // Returns amount of energy bitten
    private double bite() {
        ZObject closest = world.closestObject( this, radius() );
        if (closest == null) {
            return 0;
        }
        lastSensed = closest;
        if (closest instanceof Bug) return bite( (Bug) closest );
        if (closest instanceof Joule) return bite( (Joule) closest );
        return 0;
    }

    // Returns amount of energy barfed
    private double barf() {
        Joule barfedJoule = new Joule( world );
        barfedJoule.setXY(x(), y());
        lastSensed = barfedJoule;
        barfedJoule.joules = strength;
        world.add( barfedJoule );
        strength = 0;
        return barfedJoule.joules;
    }

    private Bug randomSplit() {
        int mostRecentBirth = 0;
        if (youngestChild() != null) mostRecentBirth = youngestChild().birthCycle();
        if (world().cycle - mostRecentBirth < World.MinCyclesBeforeSpontaneousSplit) {
            return null;
        }
        if (world().random.nextDouble() >= World.SplitProbabilityPerCycle) return null;
        return split( 1 );
    }

    private Bug spawn(double multipleOfMinInvestment) {
        // TODO push child ID onto stack, or zero
        if (World.SuppressAllBirths) return null;
        double strength2Invest = multipleOfMinInvestment * minNewbornEnergy();
        if (strength2Invest > strength) return null;
        Bug child = new Bug( this, lastMate, strength2Invest );
        if (World.ForgetMateAfterFirstChild) lastMate = null;
        return child;
    }

    private Bug split(double multipleOfMinInvestment) {
        if (World.SuppressAllBirths) return null;
        double minStrength2Invest = multipleOfMinInvestment * minNewbornEnergy();
        double halfMyMassEnergy = (mass() + strength) / 2;
        if (minStrength2Invest > halfMyMassEnergy) {
            // Cannot make a daughter with more than half our energy
            return null;
        }
        Bug child = new Bug( this, lastMate, halfMyMassEnergy );
        if (World.ForgetMateAfterFirstChild) lastMate = null;
        return child;
    }

    private boolean isFamily(Bug bug) {
        if (bug == null) return false;
        if (mother == bug) return true;
        if (bug.mother == this) return true;
        return bug.mother == mother && mother != null;
    }

    private boolean isDescendentOf(Bug bug) {
        if (bug == null) return false;
        if (bug == mother) return true;
        return mother != null && mother.isDescendentOf(bug);
    }

    private boolean isAncestorOf(Bug bug) {
        return bug != null && bug.isDescendentOf(this);
    }

    public boolean canSee(ZObject obj) {
        double threshold = World.InvisibilityThreshold;
        // The smallest bugs can see anything
        if (diameter < World.BugMinSize
                * World.BiggerThanMinSizeToSeeEverything) threshold = 0;
        if (obj.mass() / mass() < threshold) return false;
        if (!(obj instanceof Bug)) return true;
        Bug bug = (Bug) obj;
        // Split mothers/daughters are temporarily invisible to each other
        if (bug.mother != this && bug != this.mother) return true;
        if (mass() / obj.mass() < World.InvisibilityThreshold) {
            return true; // I am too small for her to see, so I can see her
        }
        if (   bug.age < World.SplitInvisiblityCycles
            || age < World.SplitInvisiblityCycles)
        {
            // We recently split, and neither is too small
            // to be seen by the other, so we are eligible 
            // for post-Split mutual invisibility
            return false;
        }
        return false;
    }

    private ZObject look( double maxRange, double minRange ) {
        ZObject closest = world.closestObject( this, maxRange, minRange );
        if (closest == null) {
            gaze = 0;
            return null;
        }
        lastSensed = closest;
        double bearing = bearing( closest );
        gaze = bearing - heading;
        /*
        String msg = id + " heading=" + (int) (heading / Math.PI * 180)
                + " sees " + closest.id + " mass="
                + String.format( "%5.3f", closest.mass() ) + " bearing="
                + (int) (bearing / Math.PI * 180) + " gaze="
                + (int) (gaze / Math.PI * 180);
        msg += "";
        System.out.println( msg );
        */
        return closest;
    }

    private void move() {
        setHeading( heading + world.brownianMotion() * Math.PI / 4 );
        double howFar = 1 + world.random.nextDouble() * World.MoveNoise * 2
                - World.MoveNoise;
        // Cost to move rises with square of speed
        shrink( howFar * howFar * mass() / 2 * World.StrengthToMove );
        assert strength >= 0 : world.cycle + " " + id
                + " strength negative after moving";
        move(heading, howFar);
    }

    private void turnTowardsCourse() {
        double turn = course - heading;
        if (turn < - Math.PI) turn += 2 * Math.PI;
        if (turn > Math.PI) turn -= 2 * Math.PI;
        if (turn > World.MaxTurnPerCycle) turn = World.MaxTurnPerCycle;
        if (turn < - World.MaxTurnPerCycle) turn = - World.MaxTurnPerCycle;
        setHeading( heading + turn );
    }

    private void setHeading( double newHeading ) {
        heading = Point.normalize( newHeading );
    }

    private void setCourse( double newCourse ) {
        course = Point.normalize( newCourse );
    }

    private void photoSynthesize() {
        double newEnergy = World.SolarJoulesPerPixelPerCycle
            * getRadius() * getRadius() * Math.PI;
        world.energyEverPhotosynthesized += newEnergy;
        grow( newEnergy );
    }

    private void updateRegisters( RegisterReference regRef ) {
        if (lastCycleLooked == world.cycle) return;
        if (regRef.reg.requiresLooking()) updateRegisters( 0 );
    }
    
    private void updateRegisters( double minRange ) {
        lastSensed = look( World.VisionRange, minRange );
        if (World.Trace) {
            System.out.println( tracePrefix()
                    + " sensed "
                    + ((lastSensed == null) ? "nothing" : String.format(
                            "%-30.30s", lastSensed.toString() )) );
        }
        lastCycleLooked = world.cycle;
    }

    @Override
    public Expression implicitArgOf(Operator op) {
        switch (op) {
            case Turn:
                return new RegisterReference( Register.Toward );
            case Split:
            case Spawn:
                return new Number( 1 );
            default:
                return null;
        }
    }

    @Override
    public Literal get(RegisterReference arg) {
        Literal val = null;
        Bug bug = null;
        updateRegisters( arg );
        if (lastSensed instanceof Bug) bug = (Bug) lastSensed;
        if (arg.who == RegisterReference.Whose.It) {
            if (bug != null) {
                if (arg.reg.hasSameValForBothBugs()) {
                    val = evaluate( arg.reg, bug );
                } else {
                    val = bug.evaluate( arg.reg, this );
                }
            } else if (lastSensed != null) {
                val = lastSensed.evaluate( arg.reg );
            } else {
                val = new Number( 0 );
            }
        } else {
            val = evaluate( arg.reg, bug );
        }
        // System.out.println( cycle() + " " + id() + " " + arg.toString()
        // + " => " + val.toString() );
        return val;
    }

    @Override
    public Literal get(Literal key ) {
        return heap.get( key );
    }
    
    @Override
    public Literal put(Literal key, Literal value) {
        return heap.put( key, value );
    }
    
    private Literal evaluate(Register reg, Bug bug) {
        switch (reg) {
            case Cycle:
                return new Number( world.cycle );
            case ID:
                return new Number( this.id );
            case Age:
                return new Number( age );
            case Size:
                return new Number( diameter );
            case Strength:
                return new Number( strength );
            case Heading:
                // System.out.println( "this.heading = "
                // + String.format( "%4.0f", 180 * heading / Math.PI ));
                if (bug != null) {
                    // System.out.println( "bug.heading = "
                    // + String.format( "%4.0f", 180 * bug.heading / Math.PI ));
                    return new Number( heading - bug.heading );
                } else {
                    // Zero is useless, so give absolute value of our heading
                    return new Number( heading );
                }
            case Location:
                return new StringLiteral( location().toString() );
            case BirthLocation:
                return new StringLiteral( birthPlace.toString() );
            case AncestralLocation:
                return new StringLiteral( genotype.birthPlace.toString() );
            case Species:
                return new Number( this.genotype.id );
            case Pain:
                double massEnergyNow = mass() + strength;
                //System.out.println( "now: " + String.format( "%4.2f", massEnergyNow )
                //        + " last: " + String.format( "%4.2f", massEnergyAfterLastTurn )
                //        + " bitten: " + String.format( "%4.2f", bittenSinceLastTurn ));
                return new Number( bittenSinceLastTurn > 0 && massEnergyNow < massEnergyAfterLastTurn );
            case FeelSomething:
                return new Number( range( lastSensed ) - radius() <= 0.1 );
            case SeeSomething:
                return new Number( range( lastSensed ) <= World.VisionRange);
            case Toward:
                return new Number( gaze );
            case Away:
                return new Number( this.gaze + Math.PI );
            case IsAlive:
                return new Number( (!isDead()) && (this.genotype != Genotype.algae()) );
            case Range:
                return new Number( range( lastSensed ) - radius() );
            case IsSameSpecies:
                if (bug != null && !bug.isDead()) {
                    // Compare id in case we ever stop sharing Genotype objects
                    return new Number( genotype.id == bug.genotype.id );
                } else {
                    return new Number( false );
                }
            case IsFamily:
                return new Number( isFamily( bug ) );
            case IsParent:
                return new Number( bug != null && bug.mother == this );
            case IsLastMate:
                return new Number( bug != null && bug.lastMate == this );
            case IsChild:
                return new Number( bug != null && mother == bug );
            case IsAncestor: // Me.IsAncestor
                return new Number( this.isAncestorOf( bug ) );
            case IsDescendent: // Me.IsDescendent
                return new Number( this.isDescendentOf( bug ) );
            default:
                return super.evaluate(  reg );
        }
    }

    @Override
    public ZoelVM.Turn execute(Operator operator, Literal operand) {
        switch (operator) {
            case Move:
                move();
                break;
            case Turn:
                double turnRadians = 0;
                Point dest = Point.parse( operand.toString() );
                if (dest != null) {
                    setCourse(bearing(dest));
                } else {
                    setCourse( heading + operand.toNumber() );
                }
                break;
            case Bite:
                if (bite() == 0) return ZoelVM.Turn.Continues;
                break;
            case Barf:
                if (barf() == 0) return ZoelVM.Turn.Continues;
                break;
            case Spawn:
                if (spawn( operand.toNumber() ) == null) return ZoelVM.Turn.Continues;
                break;
            case Split:
                if (split( operand.toNumber() ) == null) return ZoelVM.Turn.Continues;
                break;
            case Mate:
                if (mate() == null) return ZoelVM.Turn.Continues;
                break;
            case SenseFarther:
                if (lastSensed == null) {
                    updateRegisters( -1 );
                } else {
                    updateRegisters( range( lastSensed ) );
                }
                break;
            default:
                break;
        }
        return operator.whetherTurnContinues();
    }

    // Step until turn ends
    public void next() {
        if (isDead()) {
            if (deathCycle < 0) declareDeath();
            brownianMotion();
            return;
        }
        age++;
        photoSynthesize();
        if (randomSplit() == null) phenotype.next();
        massEnergyAfterLastTurn = mass() + strength;
        bittenSinceLastTurn = 0;
        turnTowardsCourse();
        brownianMotion();
    }

    private String ancestralSpecies() {
        if (mother != null) return mother.ancestralSpecies();
        return genotype.name;
    }

    private String genealogy(Genotype childGenotype, int generations,
            String separator) {
        if (generations == 0) return " ... " + ancestralSpecies();
        String msg = id + "";
        if (genotype.id != childGenotype.id) {
            msg += " [" + genotype.id;
            if (genotype.name != null) msg += "=" + genotype.name;
            msg += "]";
        }
        if (mother == null) return msg;
        return msg + separator + "< "
                + mother.genealogy( genotype, generations - 1, separator );
    }

    private String genealogy(int generations, String separator) {
        if (mother == null) return "";
        return mother.genealogy( genotype, generations, separator );
    }
    
    public Bug nextDescendant( Bug currentDescendant, List<Bug> generation ) {
        if (generation == null || generation.isEmpty()) return null;
        Bug previousDescendant = null;
        for (Bug descendant : generation) {
            if (descendant.isDead()) continue;
            if (currentDescendant == null) return descendant;
            if (previousDescendant == currentDescendant) return descendant;
            previousDescendant = descendant;
        }
        // Check the next generation
        List<Bug> nextGen = new LinkedList<Bug>();
        for (Bug descendant : generation) {
            if (descendant.children == null) continue;
            nextGen.addAll( descendant.children );
        }
        return nextDescendant( currentDescendant, nextGen );
    }

    public Bug nextDescendant( Bug currentDescendant ) {
        return nextDescendant( currentDescendant, children );
    }
    
    public Bug nextChild( Bug currentChild ) {
        if (children == null) return null;
        Bug previousChild = null;
        for (Bug child : children) {
            if (child.isDead()) continue;
            if (currentChild == null) return child;
            if (previousChild == currentChild) return child;
            previousChild = child;
        }
        // Wrap around
        for (Bug child : children) {
            if (! child.isDead()) return child;
        }
        return null;
    }
    
    public Bug nextSibling() {
        if (mother == null) return null;
        return mother.nextChild( this );
    }
    
    public Bug youngestChild() {
        if (children == null || children.isEmpty()) return null;
        return children.get( children.size()-1 );
    }

    public String genealogy(String separator) {
        return genealogy( -1, separator );
    }

    public String genealogy(int generations) {
        return genealogy( generations, " " );
    }

    public String descendents() {
        return descendents( " " );
    }

    public String descendents(String separator) {
        int[] numDescendents = { 0, 0 };
        String descendents = descendents( numDescendents, separator );
        return numDescendents[1] + "/" + numDescendents[0] + " Kids:"
                + descendents;
    }

    public String descendentsCount() {
        int[] numDescendents = { 0, 0 };
        descendents( numDescendents, " " );
        return numDescendents[1] + "/" + numDescendents[0];
    }

    public String descendents(int[] descendents, String separator) {
        if (children == null || children.size() <= 0) return "";
        String tree = separator;
        if (!separator.startsWith( "\n" )) tree += "(";
        String spacer = "";
        for (Bug child : children) {
            descendents[0]++;
            tree += spacer;
            spacer = separator;
            if (child.isDead()) {
                tree += "!";
            } else {
                descendents[1]++;
            }
            tree += child.id;
            String newSeparator = separator;
            if (newSeparator.startsWith( "\n" )) {
                newSeparator += "  ";
            }
            tree += child.descendents( descendents, newSeparator );
        }
        if (!separator.startsWith( "\n" )) tree += ")";
        return tree;
    }

    public String toString(String separator, boolean labels) {
        String msg = id + " [";
        if (labels) msg += "species ";
        msg += genotype.id;
        if (genotype.name != null) msg += "=" + genotype.name;
        msg += "] " + separator;
        if (labels) msg += " strength=";
        msg += String.format( "$%.2f ", strength );
        if (labels) msg += " mass=";
        msg += String.format( "%.2fg ", mass() );
        if (labels) msg += " diam=";
        msg += String.format( "%.1fpx ", diameter );
        if (labels) msg += " age=";
        msg += age + "s";
        if (lastMate != null) msg += " mate=" + lastMate.id;
        if (separator.startsWith( "\n" )) {
            msg += separator + phenotype.toString( separator );
        }
        return msg;
    }

    public String toString() {
        return toString( " ", false );
    }

    @Override
    public int getNextId() {
        return ++numEverCreated;
    }

    @Override
    public int getNumEverCreated() {
        return numEverCreated;
    }

    @Override
    public Random random() {
        return world.random;
    }

    @Override
    public World world() {
        return world;
    }
    
    @Override
    public int maxDataSize() {
        return Math.max( World.NewbornDataStackLimit, age
                / World.AgeToDataStackLimit );
    }

    @Override
    public int maxStepsPerTurn() {
        return World.MaxThoughtsPerCycle;
    }

    public int id() {
        return id;
    }

    public double getRadius() {
        return diameter/2;
    }

    public static int numEverCreated() {
        return numEverCreated;
    }

    public Color color() {
        return genotype.color;
    }

    @Override
    public String tracePrefix() {
        return world.cycle + " " + id;
    }
}
