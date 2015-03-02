package org.holtz.zoe.zoel;
import java.util.Random;

/**
 * The verb of an <code>Operation</code> in a <code>Zoel</code> program.
 *
 * Binary operators first push any arg onto the stack.
 * Stack.0 is the right operand, and Stack.1 is the left.
 * Stack.0 is popped, and Stack.1 is replaced with the operation result.
 * If a string and a number are the operands
 * and the string can parse to a number, then it is treated as such,
 * else the number is treated as a string.
 *
 * Unary operators first evaluate any arg and replace Stack.0 with it.
 * They then replace Stack.0 with the result of the operation on it.
 *
 * @author Brian Holtz
 */
public enum Operator {
	// ----- Action (ends turn)
	/**
	 * Move forward along $Heading. If no arg, arg = 1.0.
	 * If 0 <= arg <= 2, arg is speed. Energy cost is proportional to speed ^ 2.
	 */
	Move,
	/**
	 * Bite energy from the nearest Bug or Joule within Feel/Bite range.
	 */
	Bite,
	/**
	 * Barf up some energy to leave at the current location. If no arg, arg = 1.0.
	 * If 0 <= arg <= 1.0, arg is fraction of current bite size.
	 */
	Barf,
    /**
     * Set LastMate to the nearest visible bug that is within Feel range.
     * A system property controls whether
     * <ul><li>
     * LastMate is unset after a Spawn or Split, or
     * </li><li>
     * all children from a mating are of the same genotype.
     * </li></ul>
     */
    Mate,
    /**
	 * Attempt to spawn a child using only available strength.
	 * <ul><li>
	 * If arg >= 1, the strength to invest is arg times a max-strength min-size bug.</li><li>
	 * If 0 <= arg < 1, the strength to invest is arg times the mother's current max strength.
	 * </li></ul>
	 * A child is only created if
	 * <ol><li>
	 * she would have at least the mass-energy of a max-strength min-size bug, and </li<li>
	 * the mother would be left with strength >= 0.
	 * </li></ol>
     * If LastMate is set, then the child's genome is created from the mother and her mate.
	 */
	Spawn,
	/**
	 * Attempt to split into a parent and child of equal size and strength.
	 * <ul><li>
	 * If arg > 1, the minimum mass-energy to invest is arg times a max-strength min-size bug.
	 * </li></ul>
	 * A child is only created if she would have at least the mass-energy of a max-strength min-size bug.
	 * Because bugs are ignored by their own Feel/Look/Bite operations, mother and child are 
	 * invisible to each other's Feel/Look/Bite operations for a brief period following a Split,
	 * until their differential interactions with their environment make the distinguishable.
	 * If LastMate is set, then the child's genome is created from the mother and her mate.
	 */
	Split,
	/**
	 * End the current turn, preventing evaluation of remaining genes.
	 */
	EndTurn,
    /**
     * Turn to a different direction in order to Move that way.
     * Arg can be Me.Toward or Me.Away from last-sensed ZObject. If no arg, arg is Me.Toward.
     * Arg can be a Location e.g. Me.BirthLocation or It.AncestralLocation.
     * Direction registers (Toward Away Heading) evaluate to Direction - Me.Heading,
     * and so do as expected when used as a Turn operand.
     * If -2 PI <= arg <= 2 PI, arg is radians. Else, if -180 <= arg <= 180, arg is degrees.
     */
    Turn,
	/**
	 * Set It to the closest object farther than the current It.
	 */
	SenseFarther,
	// ----- Input/Output
    /**
     * Set the Mood register. Pushes any arg, then copies the popped stack top to Me.Mood.
     */
    Mood,
	/**
	 * Send a message to the gods. Pushes any arg, then prints popped stack top on System.out.
	 */
	Print,
	// ----- Flow Control
	/**
	 * If Stack.0 is true (nonzero/nonempty), evaluate arg.  If stack top is not true and
	 * the next Statement is Else, then skip the Else.
	 */
	IfThen,
	/**
	 * If Stack.0 is false (zero/empty), evaluate arg.
	 */
	Else,
	/**
	 * While Stack.0 is true (nonzero/nonempty), evaluate arg.
	 */
	While,
    /**
     * Evaluate the When code with the label matching the operand (or Stack.0 if
     * no operand)
     */
    When,
    /**
     * Evaluate the Do code with the label matching the operand (or Stack.0 if
     * no operand)
     */
    Do,
	// ----- Stack & Heap
	/**
	 * Push the arg onto the stack. The stack size is limited to a function of bug age, so
	 * the deepest stack element might be discarded.
	 * If no arg, arg is Stack.0, and thus the stack top is duplicated.
	 */
	Push,
	/**
	 * Pop the top off the stack.
	 */
	Pop,
	/**
	 * Set a mapping in the heap, where arg is the map key and the stack top is the value.
	 * If no arg, Stack.0 is the key, Stack.1 is the value, and Stack.0 is popped.
	 * The heap size is limited to a function of bug age, so the oldest stack element might be discarded.
	 */
	Set,
	/**
	 * Copy a heap value to the stack, where arg is the map key.
	 * If no arg, arg is Stack.0 -- i.e., the key is replaced with the value.
	 * If the key is not found, the value copied is 0.
	 */
	Get,
	// ----- Math & Logic
	/**
     * Iff stack top is true (nonzero/nonempty), evaluate arg.
	 */
	And,
	/**
     * If Stack.0 is false (zero/empty), evaluate arg. The only difference from Else is that
     * Or cannot be skipped by a prior IfThen.
	 */
	Or,
	Equals,
	LessThan,
	GreaterThan,
	/**
	 * Addition or string concatenation.
	 */
	Plus,
	/**
	 * Subtraction or string suffix removal.
	 * If Stack.1 is a string and Stack.0 is a number, then removes
	 * Stack.0 characters from the end of the string.
	 * If Stack.0 and Stack.1 are Points, compute the distance.
	 */
	Minus,
	/**
	 * Multiplication or tokenizing for strings.
	 * S1 * S0 leaves S1, pushes tokens as would be returned from PHP explode(),
	 * and finally pushes a count of tokens pushed.
	 */
	Times,
	/**
	 * Arithmetic division or substring removal/extraction.
	 * Division can yield NaN, -infinity, or +infinity, as in Java.
	 * If string/n, then leaves the first 1/|n| of the string.
	 * If n/string and n>0, then leaves the nth character from front of string.
	 * If n/string and n<0, then leaves the nth character from back of string.
	 */
	DividedBy,
	/**
	 * Arithmetic modulus or string prefix removal.
	 * If Stack.1 is a string and Stack.0 is a number, then leaves
	 * the last Stack.0 characters of the string.
	 */
	Modulus,
	/**
	 * Logical negation or string isEmpty.
	 */
	Not,
	/**
	 * Replace Stack.0 with a number 0 <= n < Stack.0. If Stack.0 is an
	 * integer other than 1.0 or -1.0, then n will be an integer too.
	 * If Stack.0 is a string, the result is a random substring of it.
	 */
	Random,
	/**
	 * Negation or string reversal.
	 */
	Negate,
	/**
	 * Absolute value or string length.
	 */
	AbsoluteVal;
	
	private static final Operator LastActionOperator = EndTurn;
	
	private static final Operator[] BestNonActionOperators = {
	       And,
	       Or,
	       Not
	};
	
	public boolean isBinary() {
	    return maxNumArgs() == 2;
	}
	
	public int maxNumArgs() {
	    switch (this) {
            case Else:
            case Or:
            case And:
            case Equals:
            case GreaterThan:
            case LessThan:
            case Plus:
            case Minus:
            case Modulus:
            case Times:
            case DividedBy:
                return 2;
            case SenseFarther:
                return 0;
            default:
                break;
	    }
	    return 1;
	}

	public ZoelVM.Turn whetherTurnContinues() {
		if (this.ordinal() <= LastActionOperator.ordinal()) return ZoelVM.Turn.Finished;
		return ZoelVM.Turn.Continues;
	}

	public static Operator pseudoRandom( Random random, boolean actionOperatorsAllowed ) {
        if (actionOperatorsAllowed) {
            if (random.nextBoolean()) return Move;
            if (random.nextBoolean()) return Turn;
            if (random.nextBoolean()) return Bite;
            return Mate;
        }
        if (random.nextDouble() < 0.1) { // slight chance of whacky Operator
            Operator vals[] = values();
            return values()[ LastActionOperator.ordinal()
                             + random.nextInt(  vals.length - LastActionOperator.ordinal()) ];
        }
        return BestNonActionOperators[ random.nextInt( BestNonActionOperators.length )];
	}

    public static Operator fromString( String str ) {
        if (str == null) return null;
        for (Operator op : values()) {
            if (str.equals( op.toString())) return op;
        }
        return null;
    }
}
