package org.holtz.zoe;

import java.awt.geom.Point2D;
import java.io.Serializable;

/**
 * A point in a Zoe world that can compute range and bearing to other points in that world.
 * @author Brian Holtz
 */
public class Point extends Point2D.Double implements Serializable {
   
    private static final long serialVersionUID = 201111271037L;

    Point( double x, double y ) {
        setLocation( x, y );
    }

    private double rangeThruTop( Point target, World world ) {
        double warpedY = y + world.height;
        if (y > target.getY()) { warpedY = y - world.height; }
        return distance( x, warpedY, target.getX(), target.getY() );
    }
    
    private double rangeThruSides( Point target, World world ) {
        double warpedX = x + world.width;
        if (x > target.getX()) { warpedX = x - world.width; }
        return distance( warpedX, y, target.getX(), target.getY() );
    }
    
    private double rangeThruNWCorner( Point target, World world ) {
        double warpedX = x + world.width;
        double warpedY = y + world.height;
        if (x > target.getX() && y > target.getY()) {
            warpedX = x - world.width; 
            warpedY = y - world.height;
        }
        return distance( warpedX, warpedY, target.getX(), target.getY() );
    }
    
    private double rangeThruNECorner( Point target, World world ) {
        double warpedX = x - world.width;
        double warpedY = y + world.height;
        if (x < target.getX() && y > target.getY()) {
            warpedX = x + world.width; 
            warpedY = y - world.height;
        }
        return distance( warpedX, warpedY, target.getX(), target.getY() );
    }
    
    public double range( Point target, World world ) {
        double[] range = new double[1];
        bearing( target, world, range );
        return range[0];
    }

    public double bearing( Point target, World world ) {
        double[] range = new double[1];
        return bearing( target, world, range );
    }
    
    public double bearing( Point target, World world, double[] range ) {
        if (target == null) return 0;
        double targetX = target.getX();
        double targetY = target.getY();
        double shortestRange = distance( x, y, targetX, targetY );
        double bearing = Math.atan2( targetY - y, targetX - x );
        double anotherRange = rangeThruTop( target, world );
        if (anotherRange < shortestRange) {
            shortestRange = anotherRange;
            double warpedY = y + world.height;
            if (y > targetY) { warpedY = y - world.height; }
            bearing = Math.atan2( targetY - warpedY, targetX - x );
        }
        anotherRange = rangeThruSides( target, world );
        if (anotherRange < shortestRange) {
            shortestRange = anotherRange;
            double warpedX = x + world.width;
            if (x > targetX) { warpedX = x - world.width; }
            bearing = Math.atan2( targetY - y, targetX - warpedX );
        }
        anotherRange = rangeThruNECorner( target, world );
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
        anotherRange = rangeThruNWCorner( target, world );
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
        range[0] = shortestRange;
        return bearing;
    }

    public static double normalize(double heading) {
        if (heading < 0) {
            heading += Math.PI * 2;
        }
        if (heading > Math.PI * 2) {
            heading -= Math.PI * 2;
        }
        return heading;
    }

    public static Point parse( String text ) {
        if (text == null) return null;
        if (! text.startsWith("{ x=")) return null;
        text = text.replaceFirst( "\\{ x=", "" );
        text = text.replaceFirst( ", y=", " " );
        text = text.replaceFirst( " }", "" );
        String[] strings = text.split( " " );
        double x = java.lang.Double.parseDouble( strings[0] );
        double y = java.lang.Double.parseDouble( strings[1] );
        return new Point( x, y );
    }
    
    public String toString() {
        String msg = "{ x=" + String.format("%.3f", getX());
        msg += ", y=" + String.format("%.3f", getY()) + " }";
        return msg;
    }

}
