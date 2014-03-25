package org.holtz.zoe.zoeswing;
import java.awt.Dimension;
import java.util.Observable;
import java.util.Observer;
import javax.swing.JLabel;

import org.holtz.zoe.Joule;
/**
 * A <code>JLabel</code> on which a <code>Joule</code> can be drawn and clicked.
 * @author Brian Holtz
 */
public class JouleLabel extends JLabel implements Observer {
	private static final long serialVersionUID = 201104151125L;
	
	private WorldPanel worldPanel;
	private JouleIcon icon;
	public Joule joule;

	public JouleLabel( Joule theJoule, WorldPanel wp ) {
		joule = theJoule;
		worldPanel = wp;
		joule.addObserver( this );
		icon = new JouleIcon( this );
		setIcon( icon );
        Dimension size = getPreferredSize();
        setBounds( (int)Math.round(joule.x()) - icon.getIconWidth()/2, 
        		   (int)Math.round(joule.y()) - icon.getIconHeight()/2, 
        		   size.width, size.height);
        repaint();
	}
	
	public void toggleSelected() {
		icon.toggleSelected();
	}
	
	@Override
	public void update(Observable arg0, Object arg1) {
        Dimension size = getPreferredSize();
        setBounds( (int)Math.round(joule.x()) - icon.getIconWidth()/2, 
        		   (int)Math.round(joule.y()) - icon.getIconHeight()/2, 
        		   size.width, size.height);
		repaint();
		if (joule.isGone()) {
			worldPanel.remove( this );
			worldPanel.world.remove( joule );
		}
	}
}
