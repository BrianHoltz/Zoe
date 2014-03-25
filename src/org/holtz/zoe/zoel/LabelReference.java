package org.holtz.zoe.zoel;


/**
 * A <code>Statement Operand</code> that points to a <code>Label</code> elsewhere in the same <code>Zoel</code> program.
 * @author Brian Holtz
 */
public class LabelReference extends Value {
	public ExpressionList val;

	// Scan the freshly-copied Genotype to accumulate a
	// translation hashmap with an entry for every LabelReference target,
	// keyed with the target's id, and value is a pointer to the
	// corresponding ExpressionList in the child genotype.  Then walk the
	// child genotype, and use the hashmap to update every 
	// LabelReference to point to the corresponding child ExpressionList.
	private LabelReference( LabelReference obj2Copy ) {
		val = new ExpressionList( obj2Copy.val );
	}

	@Override
	public String toString(String statementSeparator, Expression currExpr, String cursor) {
		String msg = "@";
		if (val == null) return msg + "null";
		return msg + val.label;
	}

	@Override
	public String toString() {
		return toString( null, null, null );
	}

	@Override
	public LabelReference copy() {
		return new LabelReference( this );
	}
}
