package org.holtz.zoe.zoel;

import java.util.Random;

import org.holtz.zoe.World;
import org.holtz.zoe.zoel.ZoelVM.Turn;
/**
 * An entity that uses a <code>ZoelVM</code> to execute Zoel code and interact with its <code>World</code>.
 * @author Brian Holtz
 */
public interface ZoelVMHost {

	public Random random();
	public World world();
	public int maxDataSize();
	public int maxStepsPerTurn();
	public String tracePrefix();

	public Literal get( RegisterReference arg );
    public Literal get( Literal key );
    public Literal put( Literal key, Literal val );
	public Turn execute( Operator op, Literal arg );
	/**
	 * The implicit argument, if any, of an Operator.
	 * @param op The Operator.
	 * @return A RegisterReference or Literal, or null if op has no implicit argument.
	 */
	public Expression implicitArgOf( Operator op );
}
