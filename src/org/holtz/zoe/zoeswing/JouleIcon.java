package org.holtz.zoe.zoeswing;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import javax.swing.ImageIcon;

import org.holtz.zoe.World;

/**
 * An <code>ImageIcon</code> that can draw a <code>Joule</code>.
 * 
 * @author Brian Holtz
 */
public class JouleIcon extends ImageIcon {
    private static final long serialVersionUID = 201104151212L;

    public JouleLabel         joule;
    private boolean           selected         = false;

    public JouleIcon(JouleLabel theJoule) {
        joule = theJoule;
    }

    public int getIconHeight() {
        return 2 + (int) Math.round( Math.sqrt( World.MaxJoule ) );
    }

    public int getIconWidth() {
        return getIconHeight();
    }

    public void paintIcon(Component jouleLabel, Graphics g, int xCorner,
            int yCorner) {
        assert jouleLabel instanceof JouleLabel : "Not JouleLabel: "
                + jouleLabel;
        g.setColor( Color.red );
        int size = (int) Math.round( Math.sqrt( joule.joule.joules ) );
        if (size == 0) size = 1;
        int x = xCorner + getIconWidth() / 2;
        int y = yCorner + getIconHeight() / 2;
        g.fillRect( x - size / 2, y - size / 2, size, size );
        if (selected) {
            g.drawRect( xCorner, yCorner, getIconWidth() - 1,
                    getIconHeight() - 1 );
        }
    }

    public void toggleSelected() {
        selected = !selected;
    }
}
