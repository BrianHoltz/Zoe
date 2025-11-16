package org.holtz.zoe.zoeswing;
import java.awt.*;

import javax.swing.ImageIcon;

import org.holtz.zoe.Bug;
import org.holtz.zoe.Genotype;
import org.holtz.zoe.World;
/**
 * An <code>ImageIcon</code> that can draw a <code>Bug</code>.
 * @author Brian Holtz
 */
public class BugIcon extends ImageIcon {
    private static final long serialVersionUID = 201104051814L;

    private static final int MinDiameter = 3;

    public Bug bug;
    private boolean selected = false;

    public BugIcon( Bug theBug ) {
        bug = theBug;
    }
    public int getIconHeight() {
        return 4 + 3*bugDiameter();
    }
    public int getIconWidth() {
        return 4 + 3*bugDiameter();
    }
    public int bugDiameter() {
        int diameter = (int)Math.round(Math.max( bug.diameter, World.BugMinSize ));
        diameter = Math.max( diameter, BugIcon.MinDiameter );
        return diameter;
    }
    private void paintStomachPie( Graphics g, int xCorner, int yCorner ) {
        int x = xCorner + getIconWidth()/2;
        int y = yCorner + getIconHeight()/2;
        int diameter = bugDiameter();
        double strengthRatio = bug.strengthRatio();
        double strengthRadians = strengthRatio * Math.PI * 2;
        int strengthDegrees = (int)Math.toDegrees( strengthRadians );
        int stomachEdge = (int)Math.toDegrees(Math.PI - bug.heading - strengthRadians/2);
        g.fillArc(x - diameter/2, y - diameter/2, diameter, diameter,
            stomachEdge, strengthDegrees );
    }
    private void paintStomachLevel( Graphics g, int xCorner, int yCorner ) {
        int x = xCorner + getIconWidth()/2;
        int y = yCorner + getIconHeight()/2;
        int diameter = bugDiameter();
        int xOffset = (int)Math.round((Math.cos( bug.heading ) * diameter/2));
        int yOffset = (int)Math.round((Math.sin( bug.heading ) * diameter/2));
        int assX = x - xOffset;
        int assY = y - yOffset;
        Polygon thoraxBox = new Polygon();
        thoraxBox.addPoint(x + xOffset, y + yOffset);
        thoraxBox.addPoint(x - xOffset, y + yOffset);
        thoraxBox.addPoint(x - xOffset, y - yOffset);
        thoraxBox.addPoint(x + xOffset, y - yOffset);
        g.setClip(thoraxBox);
        g.fillArc(x - diameter/2, y - diameter/2, diameter, diameter,
                0, 360 );
        g.setClip(null);
    }

    public void paintIcon(Component comp, Graphics g, 
            int xCorner, int yCorner)
    {
        if (bug.isGone()) return;
        g.setColor( bug.color() );
        int x = xCorner + getIconWidth()/2;
        int y = yCorner + getIconHeight()/2;
        //int size = (int)Math.round(Math.max( bug.bug.size, Bug.MinSize ));
        int diameter = bugDiameter();
        if (selected) {
            g.drawRect( xCorner, yCorner, getIconWidth() - 1, getIconHeight() - 1 );
        }
        // thorax
        g.drawOval(x - diameter/2, y - diameter/2, diameter, diameter);
        paintStomachPie( g, xCorner, yCorner );
        // head
        if (bug.genotype() == Genotype.algae()) return;
        int headDiam = Math.min( diameter, 15 );
        headDiam = diameter;
        int headX = x + (int)(Math.cos( bug.heading ) * (diameter/2 + headDiam/2));
        int headY = y + (int)(Math.sin( bug.heading ) * (diameter/2 + headDiam/2));
        g.drawOval(headX - headDiam/2, headY - headDiam/2, headDiam, headDiam);
        // tail
        int assX = x - (int)Math.round((Math.cos( bug.heading ) * diameter/2));
        int assY = y - (int)Math.round((Math.sin( bug.heading ) * diameter/2));
        //int tailLen = (int)Math.round(Math.log( bug.age ) / Math.log( 5 ));
        int tailLen = (int)Math.round(Math.cbrt( bug.age )/4);
        int assX2 = assX - (int)Math.round((Math.cos( bug.heading ) * tailLen));
        int assY2 = assY - (int)Math.round((Math.sin( bug.heading ) * tailLen));
        //System.out.println( bug.age + " => " + tailLen );
        g.drawLine(assX, assY, assX2, assY2);
        // eye
        if (bug.lastSensed != null) g.setColor( bug.lastSensed.color() );
        double gaze = bug.heading + bug.gaze;
        int eyeX = headX + (int)Math.round((Math.cos( gaze ) * headDiam/4));
        int eyeY = headY + (int)Math.round((Math.sin( gaze ) * headDiam/4));
        int eyeRadius = (int)Math.round( (double)headDiam / 6 );
        if (! bug.isDead()) {
            g.fillOval( eyeX - eyeRadius, eyeY - eyeRadius, eyeRadius*2, eyeRadius*2);
        }
    }

    public void toggleSelectedXXX() {
        selected = ! selected;
    }
    public void select( boolean selected ) {
        this.selected = selected;
    }
    public boolean selected() {
        return selected;
    }
}
