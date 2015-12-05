package org.holtz.zoe.zoel;


/**
 * A <code>Statement</code> argument that is a literal or reference rather than a <code>StatementList</code> block statement.
 * @author Brian Holtz
 */
public abstract class Value extends Expression {

    public static Value parse( ZoelTokenizer zoelTokenizer ) throws Exception {
        // Must be literal | $myRegister | $$yourRegister
        switch (zoelTokenizer.nextToken()) {
            case ZoelTokenizer.TT_WORD:
                if (RegisterReference.is( zoelTokenizer.sval )) {
                    zoelTokenizer.pushBack();
                    return RegisterReference.parse( zoelTokenizer );
                }
                break;
            case '"':
                zoelTokenizer.pushBack();
                return StringLiteral.parse( zoelTokenizer );
            case ZoelTokenizer.TT_NUMBER:
                return new Number( zoelTokenizer.nval );
        }
        throw new Exception( "Not a valid Expression: " + zoelTokenizer.toString() );
    }
}
