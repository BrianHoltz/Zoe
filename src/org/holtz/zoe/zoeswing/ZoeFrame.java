package org.holtz.zoe.zoeswing;
import javax.swing.JFrame;

import org.holtz.zoe.World;
/**
 * A <code>JFrame</code> to display a <code>ZoePanel</code> view of a Zoe <code>World</code>.
 * @author Brian Holtz
 */
public class ZoeFrame extends JFrame 
{
	private static final long serialVersionUID = 201104031104L;

	ZoeFrame() {
		setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );
		setTitle( "Zoe" );
		add( new ZoePanel() );
		pack();
		setVisible(true);
		if (World.SizeWorldToScreen) {
			setExtendedState( getExtendedState() | JFrame.MAXIMIZED_BOTH);
		}
	}

	/**
	 * @param args
	 * java -classpath Zoe.jar org.holtz.zoe.zoeswing.ZoeFrame
	 */
    public static void main(String[] args) {
        //Schedule a job for the event-dispatching thread:
        //creating and showing this application's GUI.
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
            	new ZoeFrame();
            }
        });
    }
}
