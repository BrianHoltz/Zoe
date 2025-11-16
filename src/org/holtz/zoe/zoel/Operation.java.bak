package org.holtz.zoe.zoel;

import java.io.Serializable;
import java.util.Random;

/**
 * A fundamental instruction in a <code>Zoel</code> program.
 * @author Brian Holtz
 */
public class Operation extends Expression implements Serializable {
    public Operator op;
    public Expression arg;

    public Operation( Random random, boolean actionOperatorsAllowed ) {
        op = Operator.pseudoRandom( random, actionOperatorsAllowed );
        switch (op) {
            case IfThen:
            case Else:
                ExpressionList expressions = new ExpressionList();
                expressions.add( Expression.random( random, actionOperatorsAllowed ));
                arg = expressions;
                break;
            case And:
            case Or:
            case Not:
                if (random.nextBoolean()) break; // no arg
                // fall through
            case Push:
                arg = new RegisterReference( random );
                break;
            default:
                break;
        }
    }
    // Makes a deep copy so that it can mutate separately
    public Operation( Operation operation2Copy ) {
        op = operation2Copy.op;
        if (operation2Copy.arg != null) arg = operation2Copy.arg.copy();
    }
    @Override
    public Expression copy() {
        return new Operation( this );
    }
    public Operation( Operator theOp ) {
        op = theOp;
    }
    public Operation( Operator theOp, Operation theArg ) {
        this( theOp );
        arg = theArg;
    }
    public Operation( Operator theOp, Expression theArg ) {
        this( theOp );
        arg = theArg;
    }
    public Operation( Operator theOp, String theArg ) {
        op = theOp;
        arg = new StringLiteral(theArg);
    }
    @Override
    public int totalStatements() {
        if (arg == null) return 0;
        return arg.totalStatements();
    }
    @Override
    public ExpressionListCall nthStatement( Integer[] nth ) {
        if (arg == null) return null;
        return arg.nthStatement(nth);
    }
    @Override
    public boolean isFertile() {
        return op == Operator.Split || op == Operator.Spawn;
    }
    public String toString() {
        return toString( null, null, null );
    }
    @Override
    public String toString( String stmtSeparator, Expression currExpr, String cursor ) {
        if (stmtSeparator == null) stmtSeparator = "";
        String str = "";
        if (this == currExpr) str += cursor + " ";
        str += op.toString();
        if (arg != null) {
            str += " " + arg.toString( stmtSeparator, currExpr, cursor );
        }
        return str;
    }
    public static Operation parse( String firstWord, ZoelTokenizer zoelTokenizer ) throws Exception {
        Operator op = Operator.fromString( firstWord );
        if (op == null) {
            throw new Exception( "Not a valid operator: " + firstWord );
        }
        Expression arg = Expression.parse( zoelTokenizer );
        return new Operation( op, arg );
    }

    public static Operation parse( ZoelTokenizer zoelTokenizer ) throws Exception {
        if (zoelTokenizer.nextToken() != ZoelTokenizer.TT_WORD) {
            throw new Exception( "operation must start with a word: " + zoelTokenizer.toString() );
        }
        return parse( zoelTokenizer.sval, zoelTokenizer );
    }
}
