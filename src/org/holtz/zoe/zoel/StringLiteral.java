package org.holtz.zoe.zoel;


/**
 * A string <code>Literal</code> <code>Operand</code> of a <code>Statement</code> in a <code>Zoel</code> program.
 * @author Brian Holtz
 */
public class StringLiteral extends Literal {
	public String val;
	
	public StringLiteral( String theVal ) {
		val = theVal;
	}

	public StringLiteral( StringLiteral obj2Copy ) {
		val = obj2Copy.val;
	}
	
	@Override
	public StringLiteral copy() {
		return new StringLiteral( this );
	}
	
	@Override
	public String toString(String statementSeparator, Expression currExpr, String cursor) {
		return '"' + val + '"';
	}

	@Override
	public boolean isTrue() {
		return val != null && val.length() > 0;
	}

	@Override
	public String toString() {
		return val;
	}

	@Override
	public double toNumber() {
	    try {
	        return Double.parseDouble( val );
	    } catch (NumberFormatException ex) {
	        return 0;
	    }
	}
	
    public static StringLiteral parse( ZoelTokenizer zoelTokenizer ) throws Exception {
        switch (zoelTokenizer.nextToken()) {
            case ZoelTokenizer.TT_NUMBER:
                return new StringLiteral( Double.toString( zoelTokenizer.nval ));
            case ZoelTokenizer.TT_WORD:
                return new StringLiteral( zoelTokenizer.sval );
            case '"':
                /*
                String str = "";
                String separator = "";
                while (zoelTokenizer.nextToken() != '"') {
                    if (zoelTokenizer.ttype == ZoelTokenizer.TT_EOF) {
                        throw new Exception( "EOF while seeking close quote: "
                            + zoelTokenizer.toString());
                    }
                    str += separator + zoelTokenizer.sval;
                    separator = " ";
                }
                */
                return new StringLiteral( zoelTokenizer.sval );
            default:
                throw new Exception( "EOF while seeking close quote: "
                    + zoelTokenizer.toString());
        }
    }
}
