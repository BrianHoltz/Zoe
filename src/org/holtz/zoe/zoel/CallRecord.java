package org.holtz.zoe.zoel;


/**
 * An entry in the <code>CallStack</code> of the <code>ZoelVM</code> of a Zoe <code>Bug</code>.
 * @author Brian Holtz
 */
public interface CallRecord {
	public Expression peek();
    public void repeat();
}
