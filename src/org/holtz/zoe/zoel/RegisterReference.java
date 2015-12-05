package org.holtz.zoe.zoel;

import java.util.Random;

/**
 * A <code>Bug</code> attribute referenced as a <code>Statement's Operand</code> in a <code>Zoel</code> program.
 * @author Brian Holtz
 */
public class RegisterReference extends Value {
    /**
     * Whether a RegisterReference refers to the bug itself or to the bug it last sensed.
     */
    public enum Whose {
        Me,
        It
    };
    public Register reg;
    public Whose who = Whose.Me;

    public RegisterReference( Random random ) {
        if (random.nextDouble() < 0.1) {
            reg = Register.random( random );
            who = Whose.It;
            if (random.nextBoolean()) who = Whose.Me;
            return;
        }
        reg = BestRegisters[ random.nextInt( BestRegisters.length ) ];
        switch (reg) {
            case Pain:
            case FeelSomething:
            case SeeSomething:
                who = Whose.Me;
                break;
            default:
                who = Whose.It;
        }
    }

    private static final Register[] BestRegisters = {
           Register.Pain,
           Register.FeelSomething,
           Register.SeeSomething,
           Register.IsSameSpecies,
           Register.IsFamily
    };
    
    public static Register best( Random random ) {
        return BestRegisters[ random.nextInt( BestRegisters.length )];
    }	
    public RegisterReference( Register theReg ) {
        reg = theReg;
    }

    public RegisterReference( Register theReg, Whose theWho ) {
        reg = theReg;
        who = theWho;
    }

    public RegisterReference( RegisterReference obj2Copy ) {
        reg = obj2Copy.reg;
        who = obj2Copy.who;
    }
    @Override
    public RegisterReference copy() {
        return new RegisterReference( this );
    }

    @Override
    public String toString(String statementSeparator, Expression currExpr, String cursor) {
        return who.name() + "." + reg.toString();
    }

    @Override
    public String toString() {
        return toString( null, null, null );
    }

    public static boolean is( String sval ) {
        for (Whose who : Whose.values()) {
            if (sval.startsWith( who.name() + ".")) return true;
        }
        return false;
    }

    public static RegisterReference parse( ZoelTokenizer zoelTokenizer ) throws Exception {
        if (zoelTokenizer.nextToken() != ZoelTokenizer.TT_WORD) {
            throw new Exception( "Not a valid RegisterReference: " + zoelTokenizer.toString() );
        }
        return parse( zoelTokenizer.sval );
    }

    public static RegisterReference parse( String sval ) throws Exception {
        String regName;
        Whose who;
        if (sval.startsWith( Whose.Me.name() + ".")) {
            who = Whose.Me;
            regName = sval.substring( Whose.Me.name().length() + 1 );
        } else if (sval.startsWith( Whose.It.name() + ".")) {
            who = Whose.It;
            regName = sval.substring( Whose.It.name().length() + 1 );
        } else {
            throw new Exception( "Not a valid RegisterReference: " + sval );
        }
        Register reg = Register.fromString( regName );
        if (reg == null) {
            throw new Exception( "RegisterReference name is not a register:" + sval );
        }
        return new RegisterReference( reg, who );
    }

}
