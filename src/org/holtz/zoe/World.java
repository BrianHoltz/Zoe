package org.holtz.zoe;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Observable;
import java.util.Properties;
import java.util.Random;
import java.awt.Color;
import java.awt.Dimension;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Field;
/**
 * A Zoe universe, with all the parameters to control and replay its evolution.
 * @author Brian Holtz
 */
public class World extends Observable {
    public static boolean Trace = false;
    public static boolean SuppressAllBirths = false;
	//
	// Constants controlling how the world begins
	//
	public static int Seed = 0; // 0 means get seed from clock
	public static boolean LoadBugFiles = false;
    public static boolean AutoStart = true;
	public static int InitialBugCount = 0; // 0 == use InitialPixelsBetweenBugs
	public static int InitialPixelsBetweenBugs = 140;
	public static int MaxGenesOfRandomSpecies = 10;
	
	public static boolean SizeWorldToScreen = true;
	public static int Width = 1200;
	public static int Height = 400;
    public static int BugMaxSize = 40;
    public static int BugMinSize = 5;
	public static double MaxJoule = 125;
	//
	// Constants controlling how the world changes
	//
    public static double NewJoulesPerTurnPerPixel = 0; // was 0.00000001;
    public static double NewPlanktonPerTurnPerPixel = 0.00000001;
    // 1.0 == universe radius i.e. totally random, 0.0 == always at universe midpoint
    public static double PlanktonDistributionRandomness = 1.0;
	public static double SolarJoulesPerUnitBodyAreaPerCycle = 0.00001;
	public static double BrownianMotionPerCycle = 0.2;
    public static int MaxThoughtsPerCycle = 100;
    // Energy to move 1 sq pixel through a distance of 1 pixel in 1 cycle
    public static double StrengthToMove = 0.0003;
    public static double MaxTurnPerCycle = Math.PI / 16;
    public static double VisionRange = 30;
    // How big you can bite is a fraction of your circumference.
    // Is proportional to square root of area, disadvantaging megafauna.
    public static double BiteFractionOfOwnCircumference = 0.01;
    public static double BiteEfficiency = 0.8;
    public static double BirthEfficiency = 0.95;
    public static double MoveNoise = 0.1;
    // Objects below this fraction of your mass are invisible to you
    public static double InvisibilityThreshold = 0.3;
    // Bugs less than this much bigger than BugMinSize can see everything
    public static double BiggerThanMinSizeToSeeEverything = 1.8;
    // Mother/daughter are mutually invisible for this many cycles after Split
    public static int SplitInvisiblityCycles = 100;
    // Can't fork if it would leave you weaker than this
    public static double MinPostPartumStrengthLevel = 0.3;
    public static double MutantChildrenFreq = 0.2;
    // Set this to zero to never have spontaneous splitting
    public static int ExpectedCyclesBeforeSpontaneousSplit = 5000;
    public static boolean ForgetMateAfterFirstChild = false;
    public static boolean ChildrenOfAMatingShareGenotype = true;
	public static int AgeToDataStackLimit = 100;
	public static int NewbornDataStackLimit = 10;
    //
	// Constants controlling how the world looks
    // These constants do not affect world's outcome
    //
	public static int GarbageCollectionFreq = 50000;
	public static int MinMilliSecsPerTurn = 1;
    // Colors that sum too high are too faint
    public static int MaxColorMutation = 90;

    public static int MinCyclesBeforeSpontaneousSplit = ExpectedCyclesBeforeSpontaneousSplit/2;
    public static double SplitProbabilityPerCycle
            = 1 - Math.pow(0.5, 1.0 / World.ExpectedCyclesBeforeSpontaneousSplit);
    
	public int width = Width;
	public int height = Height;
	public ArrayList<Bug> bugs;
	public ArrayList<Joule> joules;
	public int cycle = 1; // So bugs that spawn at cycle % N won't spawn immediately
	public Date start = new Date();
	public long seed;
	// All choices use world.random, so the world can be replayed
	// using the initial seed -- modulo user intervention.
	public java.util.Random random;
	private ArrayList<Bug> newBugs;
	private Iterator<Bug> bug2RunItr;
	
	public static Properties props = null;
	private static String propsFileName = "Zoe.properties";

	public World( Dimension size ) {
		initProperties();
		resize( size );
		seed = Seed;
		if (seed == 0) seed = System.currentTimeMillis() % 1000;
		random = new java.util.Random( seed );
		bugs = new ArrayList<Bug>();
		int initialBugCount = size.height * size.width
                / (InitialPixelsBetweenBugs * InitialPixelsBetweenBugs);
        // panel size can be broken when run in browser
        if (size.height <= 0 || size.width <= 0) initialBugCount = 20;
        //System.out.println("\n" + size.width + " x " + size.height);
		if (InitialBugCount > 0) initialBugCount = InitialBugCount;
        for (int i = 0; i < initialBugCount; i++) {
            if (initialBugCount > Genotype.getNumFounders()) {
                new Bug( this );
            } else {
                new Bug( this, Genotype.founder( i ));
            }
        }
		joules = new ArrayList<Joule>();
		int initialJouleCount = (int)(NewJoulesPerTurnPerPixel * size.width * size.height * 2000);
        for (int i = 0; i < initialJouleCount; i++) {
        	add( new Joule( this ));
        }
        // Genotype2 species = Genotype2.getRandomGenotype( random );
        // System.out.println( species.toString( "\n" ) );
        /*
        Point loc = new Point( 2.0, 3.5 );
        System.out.println( loc.toString() );
        Point loc2 = Point.parse( loc.toString() );
        System.out.println( loc2.toString() );
        */
	}
	
	public static void initProperties() {
		if (props != null) return;
		props = new Properties();
		FileInputStream propsIstr;
		try {
			propsIstr = new FileInputStream( propsFileName );
			props.load( propsIstr );
			propsIstr.close();
		} catch (java.security.AccessControlException e) {
			System.err.println( propsFileName + ": " + e.toString() );
			return;
		} catch (FileNotFoundException e) {
			System.err.println( propsFileName + ": " + e.toString() );
		} catch (IOException e) {
			System.err.println( propsFileName + ": " + e.toString() );
		}
		for ( Field field : World.class.getFields() ) {
			Class<?> fieldClass = field.getType();
			String prop = field.getName();
			if (! Character.isUpperCase( prop.charAt(0) )) continue;
			String propVal = props.getProperty( prop );
			if (propVal == null) continue;
			try {
				if (fieldClass == Integer.TYPE) {
					field.set( null, Integer.parseInt( propVal ));
				}
				if (fieldClass == Double.TYPE) {
					field.set( null, Double.parseDouble( propVal ));
				}
				if (fieldClass == Boolean.TYPE) {
					field.set( null, Boolean.parseBoolean( propVal ));
				}
			} catch (Exception e) {
				System.err.println( "Cannot read/set World property \"" + prop
					+ "=" + propVal + "\" because: " + e.toString() );
			}
		}
	}
	
	public double brownianMotion() {
		return BrownianMotionPerCycle / 2 - random.nextFloat() * BrownianMotionPerCycle;
	}
	
	public Point midpoint() {
	    return new Point( width/2, height/2 );
	}
	
	public double radius() {
	    return Math.max( width/2, height/2 );
	}
	
	public void add( Bug newBug ) {
		// If we add bugs mid-cycle, bugs list gets a ConcurrentModification exception
		if (bug2RunItr == null) {
			bugs.add( newBug );
			setChanged();
			notifyObservers( newBug );
		} else {
			if (newBugs == null) newBugs = new ArrayList<Bug>();
			newBugs.add( newBug );
		}
	}

	public void add( Joule newJoule ) {
		joules.add( newJoule );
		setChanged();
		notifyObservers( newJoule );
	}

	public void remove( Joule deadJoule ) {
		Iterator<Joule> jouleItr = joules.iterator();
		while (jouleItr.hasNext()) {
			if (jouleItr.next() != deadJoule) continue;
			jouleItr.remove();
		}
	}

    public void remove( Bug bug ) {
        Iterator<Bug> bugItr = bugs.iterator();
        while (bugItr.hasNext()) {
            if (bugItr.next() != bug) continue;
            bugItr.remove();
        }
    }
    
	public double strength() {
		double total = 0;
		for( Bug bug : bugs ) {
			total += bug.strength();
		}
		return total;
	}
	
	public double mass() {
		double total = 0;
		for( Bug bug : bugs ) {
			total += bug.mass();
		}
		return total;
	}

	public double joules() {
		double total = 0;
		for( Joule joule : joules ) {
			total += joule.joules;
		}
		return total;
	}

	public Genotype topSpecies() {
		Genotype top = null;
		for (Bug bug : bugs) {
		    if (bug.genotype == Genotype.algae()) continue;
			if (top == null) top = bug.genotype;
			if (bug.genotype.numLiving > top.numLiving) top = bug.genotype;
		}
		return top;
	}
	
	public int numLive() {
		int total = 0;
		for( Bug bug : bugs ) {
			if (! bug.isDead()) total++;
		}
		return total;
	}

	public int numDead() {
		int total = 0;
		for( Bug bug : bugs ) {
			if (bug.isDead()) total++;
		}
		return total;
	}

	public int numSpecies() {
		HashSet<Genotype> species = new HashSet<Genotype>();
		for( Bug bug : bugs ) {
			if (! bug.isDead()) species.add( bug.genotype );
		}
		return species.size();
	}

    // minRange is used when looking beyond a seen object
	private static ZObject closestOf( List<? extends ZObject> objects, Bug from,
	        double maxRange, double minRange ) 
	{
		Iterator<? extends ZObject> objItr = objects.listIterator();
		ZObject closest = null;
		double closestRange = 100000;
		while (objItr.hasNext()) {
			ZObject obj = objItr.next();
			if (obj == from) continue;
			double range = from.range( obj );
			if (range > maxRange) continue;
			if (range <= minRange) continue;
			if (range >= closestRange) continue;
			if (! from.canSee( obj )) continue;
			closest = obj;
			closestRange = range;
		}
		return closest;
	}

    public Bug closestBug( Bug from, double maxRange ) {
        return closestBug( from, maxRange, -1 );
    }
	public Bug closestBug( Bug from, double maxRange, double minRange ) {
		return (Bug)closestOf( bugs, from, maxRange, minRange );
	}

	private Joule closestJoule( Bug from, double maxRange, double minRange ) {
        return (Joule)closestOf( joules, from, maxRange, minRange );
	}

    public ZObject closestObject( Bug from, double maxRange ) {
        return closestObject( from, maxRange, -1 );
    }
	public ZObject closestObject( Bug from, double maxRange, double minRange ) {
		Bug bug = closestBug( from, maxRange, minRange );
		Joule joule = closestJoule( from, maxRange, minRange );
		if (bug == null) return joule;
		if (joule == null) return bug;
		if (from.range( bug ) < from.range( joule )) return bug;
		return joule;
	}

	public void resize( Dimension newSize ) {
		if (newSize.width > 0) width = newSize.width;
		if (newSize.height > 0) height = newSize.height;
	}
    
    public void nextWorldCycle() {
    	double probabilityOfANewPlankton = NewPlanktonPerTurnPerPixel * width * height;
        double dice = random.nextDouble();
        if (dice < probabilityOfANewPlankton) {
            Bug newBug = new Bug( this );
        }
    	if (cycle % World.GarbageCollectionFreq == 0) System.gc();
    	while (nextBugCycle()) {}
    }
    
    // return true if more bugs to run in this cycle
    public boolean nextBugCycle() {
    	if (bug2RunItr == null) {
    		bug2RunItr = bugs.iterator();
    	}
    	if (! bug2RunItr.hasNext()) {
    		bug2RunItr = null;
    		cycle++;
    		if (newBugs != null) {
    			for (Bug bug : newBugs) {
    				add( bug );
    			}
    			newBugs = null;
    		}
    		return false;
    	}
    	Bug bug = bug2RunItr.next();
    	if (bug.isGone()) {
    		bug.repaint();
    		bug2RunItr.remove();
    	} else {
    		bug.next();
    	}
    	return true;
    }
    
    public void removeGoneBugs() {
        Iterator<Bug> bugItr = bugs.iterator();
        while (bugItr.hasNext()) {
            if (bugItr.next().isGone()) {
                bugItr.remove();
            }
        }
    }
    
    private static int mutateColor( int color, Random random ) {
    	color += random.nextInt( MaxColorMutation ) - ( MaxColorMutation / 2 );
    	if (color > 255) color = 255;
    	if (color < 0) color = 0;
    	return color;
    }

    public static Color mutateColor( Color color, Random random ) {
        int red = mutateColor( color.getRed(), random );
        int green = mutateColor( color.getGreen(), random );
        int blue = mutateColor( color.getBlue(), random );
        return new Color( red, green, blue );
    }

    public static Color mutateColor( Color color1, Color color2 ) {
        int red = (color1.getRed() + color2.getRed()) / 2;
        int green = (color1.getGreen() + color2.getGreen()) / 2;
        int blue = (color1.getBlue() + color2.getBlue()) / 2;
        return new Color( red, green, blue );
    }

	public static Color color( String str ) {
		int key = 0;
		while (str.length() > 0) {
			key += str.charAt(0) * str.charAt(0);
			str = str.substring( 1 );
		}
		return color( key );
	}
	
	public static Color color( int key ) {
	    final float hue = (key % 256) / (float)256;
	    final float saturation = 1.0f; //1.0 for brilliant, 0.0 for dull
	    final float luminance = 0.7f;  //1.0 for brighter, 0.0 for black
	    return Color.getHSBColor(hue, saturation, luminance);
	}
	
}
