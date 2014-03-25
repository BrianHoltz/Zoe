package org.holtz.zoe.zoeswing;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Point;
import java.util.Observable;
import java.util.Observer;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import org.holtz.zoe.Bug;
/**
 * A <code>JPanel</code> displaying properties of a <code>Bug</code>.
 * @author Brian Holtz
 */
public class BugPanel extends JPanel implements Observer {
	private static final long serialVersionUID = 201109171500L;
	public Bug bug;
	public BugLabel bugLabel;
	JTextArea textArea;
	JScrollPane scrollPane;

	public BugPanel( Bug theBug ) {
        super(new GridBagLayout());
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.gridwidth = GridBagConstraints.REMAINDER;
        constraints.fill = GridBagConstraints.BOTH;
        constraints.weightx = 0;
        constraints.weighty = 0;
        Insets insets = new Insets( 7, 7, 7, 7 );
        constraints.insets = insets;
        
		bug = theBug;
		bug.addObserver( this );
		add( bugLabel = new BugLabel( bug, this ), constraints );
		bugLabel.setToolTipText( "What the bug looks like" );

        textArea = new JTextArea(40, 25);
        textArea.setEditable(false);
        scrollPane = new JScrollPane(textArea);
        constraints.weightx = 1;
        constraints.weighty = 1;
        add(scrollPane, constraints);
        
        update( bug, this );
	}

	@Override
	public void update(Observable arg0, Object arg1) {
        Point lastPos = scrollPane.getViewport().getViewPosition();
        if (lastPos.x != 0 || lastPos.y != 0) {
        	return;
        }
		textArea.setText("Bug ");
		textArea.append( bug.toString( "\n", false ) + "\n\n" );
        textArea.append( bug.descendents( "\n" ) + "\n\n");
        textArea.append( "Ancestors:\n" + bug.genealogy( "\n" ) + "\n\n" );
        textArea.setCaretPosition( 0 );
	}
}
