package org.holtz.zoe;
import java.awt.Color;
/**
* A pellet of edible energy in a <code>World</code>.
* @author Brian Holtz
*/
public class Joule extends ZObject {
    private static int numEverCreated = 0;
    
	public double joules;
	
	public Joule( World theWorld ) {
		super( theWorld );
		joules = theWorld.random.nextFloat() * World.MaxJoule;
	}

    @Override
    public double radius() {
        return Math.sqrt( joules )/2;
    }

	@Override
	public double mass() {
		return joules;
	}

    @Override
    public Color color() {
        return Color.red;
    }	
	@Override
	public boolean isGone() {
		return joules <= 0;
	}
	
	public void getBit( double bite ) {
		joules -= bite;
		if (joules <= 0) joules = 0;
		setChanged();
		notifyObservers();
	}
	
	@Override
	public int getNextId() {
		return ++numEverCreated;
	}

	@Override
	public int getNumEverCreated() {
		return numEverCreated;
	}
	
}
