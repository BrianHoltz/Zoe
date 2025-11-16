package org.holtz.zoe;
import java.io.Serializable;
import java.util.Observable;
import java.awt.Color;

import org.holtz.zoe.zoel.Literal;
import org.holtz.zoe.zoel.Number;
import org.holtz.zoe.zoel.Register;
import org.holtz.zoe.zoel.StringLiteral;
/**
 * A physical object in a toroidal Zoe <code>World</code>.
 * @author Brian Holtz
 */
public abstract class ZObject extends Observable implements Serializable {

    public World world;
    private double x;
    private double y;
    public int id = getNextId();
    private Object context;
    private Point cachedLocation;
    private boolean locationDirty = true;

    public ZObject( World theWorld ) {
        world = theWorld;
        setXY( world.random.nextFloat() * world.width, world.random.nextFloat() * world.height );
    }

    public abstract int getNextId();
    public abstract int getNumEverCreated();

    public Point location() {
        if (locationDirty || cachedLocation == null) {
            cachedLocation = new Point( x, y );
            locationDirty = false;
        }
        return cachedLocation;
    }
    public double x() { return x; }
    public double y() { return y; }
    
    public Object getContext() { return context; }
    public  ZObject setContext( Object ctxt ) {
        context = ctxt;
        return this;
    }

    public void repaint() {
        setChanged();
        notifyObservers();
    }

    public abstract Color color();

    public abstract double radius();

    public abstract double mass();

    public boolean isGone() {
        return mass() <= 0;
    }

    public void brownianMotion() {
        setXY( x + world.brownianMotion(), y + world.brownianMotion() );
    }

    public void setXY( double newX, double newY ) {
        x = newX;
        y = newY;
        while (x < 0) { x += world.width; }
        while (x >= world.width) { x -= world.width; }
        while (y < 0) { y += world.height; }
        while (y >= world.height) { y -= world.height; }
        locationDirty = true; // Mark location cache as invalid
        // Mark spatial grid as dirty when object moves (only if it's a Bug)
        if (this instanceof Bug) {
            world.markSpatialGridDirty();
        }
        repaint();
    }

    public void move( double heading, double howFar ) {
        setXY( x() + Math.cos( heading ) * howFar,
               y() + Math.sin( heading ) * howFar );
    }

    public Literal evaluate( Register reg ) {
        switch (reg) {
            case Location:
                Point loc = new Point( x(), y() );
                return new StringLiteral( loc.toString() );
            default:
                System.err.println( "Unimplemented register: " + reg.toString() );
                return new Number( 0 );
        }
    }

    private static double distance( double x1, double y1, double x2, double y2 ) {
        return Math.sqrt( (x1 - x2) * (x1 - x2) + (y1 - y2) * (y1 - y2) );
    }

    private double rangeThruTop( Point target ) {
        double warpedY = y + world.height;
        if (y > target.getY()) { warpedY = y - world.height; }
        return distance( x, warpedY, target.getX(), target.getY() );
    }

    private double rangeThruSides( Point target ) {
        double warpedX = x + world.width;
        if (x > target.getX()) { warpedX = x - world.width; }
        return distance( warpedX, y, target.getX(), target.getY() );
    }

    private double rangeThruNWCorner( Point target ) {
        double warpedX = x + world.width;
        double warpedY = y + world.height;
        if (x > target.getX() && y > target.getY()) {
            warpedX = x - world.width;
            warpedY = y - world.height;
        }
        return distance( warpedX, warpedY, target.getX(), target.getY() );
    }

    private double rangeThruNECorner( Point target ) {
        double warpedX = x - world.width;
        double warpedY = y + world.height;
        if (x < target.getX() && y > target.getY()) {
            warpedX = x + world.width;
            warpedY = y - world.height;
        }
        return distance( warpedX, warpedY, target.getX(), target.getY() );
    }

    public double range( Point target ) {
        if (target == null) return Double.MAX_VALUE;
        double shortestRange = distance( x, y, target.getX(), target.getY() );
        double anotherRange = rangeThruTop( target );
        if (anotherRange < shortestRange) {
            shortestRange = anotherRange;
        }
        anotherRange = rangeThruSides( target );
        if (anotherRange < shortestRange) {
            shortestRange = anotherRange;
        }
        anotherRange = rangeThruNECorner( target );
        if (anotherRange < shortestRange) {
            shortestRange = anotherRange;
        }
        anotherRange = rangeThruNWCorner( target );
        if (anotherRange < shortestRange) {
            shortestRange = anotherRange;
        }
        return shortestRange;
    }

    public double range( ZObject theZObject ) {
        if (theZObject == null) return Double.MAX_VALUE;
        double range = range( theZObject.location() ) - theZObject.radius();
        return Math.max( 0, range );
    }

    public double bearing( ZObject theZObject ) {
        return bearing( theZObject.location() );
    }
    
    public double bearing( Point target ) {
        if (target == null) return 0;
        double targetX = target.getX();
        double targetY = target.getY();
        double shortestRange = distance( x, y, targetX, targetY );
        double bearing = Math.atan2( targetY - y, targetX - x );
        double anotherRange = rangeThruTop( target );
        if (anotherRange < shortestRange) {
            shortestRange = anotherRange;
            double warpedY = y + world.height;
            if (y > targetY) { warpedY = y - world.height; }
            bearing = Math.atan2( targetY - warpedY, targetX - x );
        }
        anotherRange = rangeThruSides( target );
        if (anotherRange < shortestRange) {
            shortestRange = anotherRange;
            double warpedX = x + world.width;
            if (x > targetX) { warpedX = x - world.width; }
            bearing = Math.atan2( targetY - y, targetX - warpedX );
        }
        anotherRange = rangeThruNECorner( target );
        if (anotherRange < shortestRange) {
            shortestRange = anotherRange;
            double warpedX = x - world.width;
            double warpedY = y + world.height;
            if (x < targetX && y > targetY) {
                warpedX = x + world.width;
                warpedY = y - world.height;
            }
            bearing = Math.atan2( targetY - warpedX, targetX - warpedY );
        }
        anotherRange = rangeThruNWCorner( target );
        if (anotherRange < shortestRange) {
            shortestRange = anotherRange;
            double warpedX = x + world.width;
            double warpedY = y + world.height;
            if (x > targetX && y > targetY) {
                warpedX = x - world.width;
                warpedY = y - world.height;
            }
            bearing = Math.atan2( targetY - warpedX, targetX - warpedY );
        }
        return bearing;
    }
}
