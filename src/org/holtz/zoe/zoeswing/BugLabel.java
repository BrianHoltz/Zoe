package org.holtz.zoe.zoeswing;
import java.awt.Dimension;
import java.util.Observable;
import java.util.Observer;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.holtz.zoe.Bug;
/**
 * A <code>JLabel</code> on which a <code>Bug</code> can be drawn and clicked.
 * @author Brian Holtz
 */
public class BugLabel extends JLabel implements Observer {
    private static final long serialVersionUID = 201104051701L;

    public Bug bug;

    private JPanel parentPanel;
    private BugIcon icon;

    public BugLabel( Bug theBug, JPanel container ) {
        bug = theBug;
        parentPanel = container;
        bug.addObserver( this );
        icon = new BugIcon( bug );
        setIcon( icon );
        Dimension size = getPreferredSize();
        setBounds( (int)Math.round(bug.x()) - icon.getIconWidth()/2, 
                   (int)Math.round(bug.y()) - icon.getIconHeight()/2,
                   size.width, size.height);
        repaint();
    }

    public void toggleSelectedXXX() {
        icon.toggleSelectedXXX();
    }
    public void select( boolean selected ) {
        icon.select( selected );
    }
    public boolean selected() {
        return icon.selected();
    }
    
    @Override
    public void update(Observable arg0, Object arg1) {
        repaint(); // repaints BugLabel in BugPanel
        if (! (parentPanel instanceof WorldPanel)) {
            return;
        }
        Dimension size = getPreferredSize();
        int x = (int)Math.round(bug.x()) - icon.getIconWidth()/2;
        int y = (int)Math.round(bug.y()) - icon.getIconHeight()/2;
        setBounds( x, y, size.width, size.height );
        if (bug.isGone()) {
            parentPanel.remove( this );
        }
    }
}
