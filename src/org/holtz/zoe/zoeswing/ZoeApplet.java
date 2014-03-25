package org.holtz.zoe.zoeswing;

import javax.swing.JApplet;

/**
 * A <code>JApplet</code> that displays a Zoe <code>ZoePanel</code>.
 * @author Brian Holtz
 */
public class ZoeApplet extends JApplet {
	private static final long serialVersionUID = 201110072325L;

	//Called when this applet is loaded into the browser.
    public void init() {
        //Execute a job on the event-dispatching thread; creating this applet's GUI.
        try {
        	javax.swing.SwingUtilities.invokeAndWait(new Runnable() {
                public void run() {
                	createGUI();
                }
            });
        } catch (Exception e) { 
            System.err.println("Could not initialize ZoeApplet: " + e.getCause());
        }
    }
    
    private void createGUI() {
        //Create and set up the content pane.
        ZoePanel zoePanel = new ZoePanel();
        zoePanel.setOpaque( true ); 
        zoePanel.setVisible( true );
        setContentPane( zoePanel );        
    }

	/**
	 * @param args
	 * java -classpath Zoe.jar org.holtz.zoe.zoeswing.ZoeApplet
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
