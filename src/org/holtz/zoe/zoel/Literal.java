package org.holtz.zoe.zoel;


import java.io.Serializable;

/**
 * A string or numeric <code>Operand</code> of a <code>Statement</code> in a <code>Zoel</code> program.
 * @author Brian Holtz
 */
public abstract class Literal extends Value implements Serializable {
    public abstract boolean isTrue();
    public abstract double toNumber();
    public abstract int toInteger();
}
