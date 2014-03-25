package org.holtz.zoe.zoel;


/**
 * A numeric <code>Literal</code> <code>Operand</code> of a <code>Statement</code> in a <code>Zoel</code> program.
 * @author Brian Holtz
 */
public class Number extends Literal {
	public double val;

	public Number( double theVal ) {
		val = theVal;
	}

	public Number( boolean theVal ) {
		val = theVal ? 1 : 0;
	}

	public Number( Number obj2Copy ) {
		val = obj2Copy.val;
	}
	@Override
	public Number copy() {
		return new Number( this );
	}
	
	@Override
	public String toString(String statementSeparator, Expression currExpr, String cursor) {
		if (val - Math.round(val) == 0) return String.format("%.0f", val);
		return String.format("%.3f", val);
	}

	@Override
	public boolean isTrue() {
		return val != 0;
	}

	@Override
	public String toString() {
		return toString( null, null, null );
	}

	@Override
	public double toNumber() {
		return val;
	}
}
