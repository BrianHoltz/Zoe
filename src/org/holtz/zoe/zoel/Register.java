package org.holtz.zoe.zoel;

import java.util.Random;

/**
 * A <code>Bug</code> attribute that can be an <code>Operator</code>'s <code>Operand</code> in a <code>Zoel</code> program.
 * @author Brian Holtz
 */
public enum Register {
    Cycle,
    ID,
    Age,
    Size,
    Strength,
    Heading,
    /**
     * Locations are strings encoding an XY pair: { x = 100.333, y = 200 }
     * Math operators can work on Locations.
     */
    Location,
    BirthLocation,
    AncestralLocation,
    Species,
    /*
     * True iff bitten since last turn and strength is lower
     */
    Pain,
    // All below invoke a look()
    FeelSomething,
    SeeSomething,
    Toward,
    Away,
    IsAlive,
    // All above depend only on 1 bug's state
    IsParent,
    IsChild,
    IsLastMate,
    IsAncestor,
    IsDescendent,
    // All below have the same value for both Me and It
    IsSameSpecies,
    IsFamily,
    Range;

    public boolean requiresLooking() {
        return (this.ordinal() >= FeelSomething.ordinal());
    }

    public boolean hasSameValForBothBugs() {
        return (this.ordinal() >= IsSameSpecies.ordinal());
    }

    public static Register fromString( String str ) {
        if (str == null) return null;
        for (Register reg : values()) {
            if (str.equals( reg.toString())) return reg;
        }
        return null;
    }

    public static Register random( Random random ) {
        int nth = random.nextInt( values().length );
        for (Register val : values()) {
            if (nth == 0) return val;
            nth--;
        }
        return Cycle;
    }
}
