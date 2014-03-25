package org.holtz.zoe;

import java.util.Random;

import org.holtz.zoe.zoel.Expression;
import org.holtz.zoe.zoel.Literal;
import org.holtz.zoe.zoel.Operator;
import org.holtz.zoe.zoel.RegisterReference;
import org.holtz.zoe.zoel.ZoelVM;
import org.holtz.zoe.zoel.ZoelVMHost;

/**
 * The expression of a <code>Gene</code>, consisting of its <code>ZoelVM</code> execution state.
 * @author Brian Holtz
 */
public class Phene implements ZoelVMHost {
	
	protected Gene gene;
	//protected double excitement = 0;
	protected ZoelVM zvm;
	protected Bug bug;
    // True iff zvm is executing our Do program
	protected boolean doing = false;

	public Phene( Bug b, Gene g ) {
		bug = b;
		gene = g;
	}
	public ZoelVM.Turn Do() {
	    if (World.Trace ) System.out.println( bug.tracePrefix() + " Do " + gene.action.toString() );
		//excitement = 0;
		if (! doing) {
			doing = true;
			// Restart the VM to begin Do
			zvm = new ZoelVM( this, gene.action );
		}
		ZoelVM.Turn result = zvm.next();
		if (result == ZoelVM.Turn.Exited) doing = false;
		return result;
	}
	public boolean when() {
		if (gene.when == null) return true;
        if (World.Trace ) System.out.println( bug.tracePrefix() + " When " + gene.when.toString() );
		// Completely restart the ZVM
		zvm = new ZoelVM( this, gene.when );
		zvm.next(); // When is not allowed to take >1 turn
		if (zvm.peek() == null) return false;
		return zvm.peek().isTrue();
	}
	/*
	public double excitement() {
		return excitement;
	}
	public void excite( int priority ) {
		excitement += (1 - excitement) / (1 + priority);
		if (excitement > 1) excitement = 1;
	}
	public void dampen(int priority) {
		excitement -= 1 / World.Cycles2DampenExcitement;
		if (excitement < 0) excitement = 0;
	}
	*/
	public String toString( String separator ) {
		// String msg = "";
		// msg += "excitement=" + String.format( "%.3f", excitement );
		// if (doing) msg += " doing";
		return gene.toString( separator );
	}
	@Override
	public Random random() {
		return bug.random();
	}
    @Override
    public World world() {
        return bug.world;
    }
	@Override
	public int maxDataSize() {
		return bug.maxDataSize();
	}
	
	@Override
	public int maxStepsPerTurn() {
		return bug.maxStepsPerTurn();
	}
	@Override
	public String tracePrefix() {
		return bug.tracePrefix();
	}
	@Override
	public Expression implicitArgOf( Operator op ) {
		return bug.implicitArgOf( op );
	}
	@Override
	public Literal get( RegisterReference arg ) {
		return bug.get( arg );
	}
    @Override
    public Literal get( Literal key ) {
        return bug.get( key );
    }
    @Override
    public Literal put(Literal key, Literal val) {
        return bug.put( key, val );
    }
	@Override
	public org.holtz.zoe.zoel.ZoelVM.Turn execute( Operator operator, Literal operand ) {
		if (! doing && operator.whetherTurnContinues() == ZoelVM.Turn.Finished) {
			// Actions are illegal during When evaluation, so ignore them.
			return ZoelVM.Turn.Continues;
		}
		return bug.execute( operator, operand );
	}
}
