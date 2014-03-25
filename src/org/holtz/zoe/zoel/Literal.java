package org.holtz.zoe.zoel;


/**
 * A string or numeric <code>Operand</code> of a <code>Statement</code> in a <code>Zoel</code> program.
 * @author Brian Holtz
 */
public abstract class Literal extends Value {
	public abstract boolean isTrue();
	public abstract double toNumber();
}
