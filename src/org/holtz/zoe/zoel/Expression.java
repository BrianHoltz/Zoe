package org.holtz.zoe.zoel;

import java.io.Serializable;
import java.util.Random;


/**
 * A <code>Value</code>, <code>Operation</code>, or <code>StatementList</code> in a <code>Zoel</code> program.
 * @author Brian Holtz
 */
public abstract class Expression implements Serializable {

    public abstract Expression copy();

    public static Expression random( Random random, boolean actionsAllowed ) {
        if (random.nextBoolean()) return new RegisterReference( random );
        return new Operation( random, actionsAllowed );
    }
    
    public int totalStatements() {
        return 0;
    }

    public ExpressionListCall nthStatement( Integer[] nth ) {
        return null;
    }

    public boolean isFertile() {
        return false;
    }

    public abstract String toString( String statementSeparator, Expression currStmt, String cursor );

    public abstract String toString();

    public static Expression parse( String word1, ZoelTokenizer zoelTokenizer ) throws Exception {
        if (RegisterReference.is( word1 )) {
            return RegisterReference.parse( word1 );
        }
        return Operation.parse( word1, zoelTokenizer );
    }

    public static Expression parse( ZoelTokenizer zoelTokenizer ) throws Exception {
        int peek = zoelTokenizer.nextToken();
        String peekStr = zoelTokenizer.sval;
        zoelTokenizer.pushBack();
        switch (peek) {
            case ZoelTokenizer.StatementTerminator:
            case '}':
                return null;
            case ZoelTokenizer.TT_NUMBER:
            case '"':
            case '$':
                return Value.parse( zoelTokenizer );
            case '{':
                return ExpressionList.parse( zoelTokenizer );
            case ZoelTokenizer.TT_WORD:
                if (RegisterReference.is( peekStr )) {
                    return RegisterReference.parse( zoelTokenizer );
                }
                // fall through
            default:
                return Operation.parse( zoelTokenizer );
        }
    }
}
