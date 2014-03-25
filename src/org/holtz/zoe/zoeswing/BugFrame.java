package org.holtz.zoe.zoeswing;
import javax.swing.JFrame;

import org.holtz.zoe.Bug;
/**
 * A <code>JFrame</code> to hold a <code>BugPanel</code> of information about a <code>Bug</code>.
 * @author Brian Holtz
 */
public class BugFrame extends JFrame
{
	private static final long serialVersionUID = 201109171431L;
	
	private BugPanel bugPanel;
	private Bug bug;

	public BugFrame( Bug theBug ) {
		bug = theBug;
		setDefaultCloseOperation( JFrame.DISPOSE_ON_CLOSE );
		bugPanel = new BugPanel( bug );
		add( bugPanel );
		setTitle( "Bug Properties" );
		if (bug != null) {
			setTitle( "Bug " + bug.id + " Properties" );
		}
		pack();
		setVisible(true);
	}
}
