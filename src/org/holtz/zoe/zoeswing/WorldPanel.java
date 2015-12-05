package org.holtz.zoe.zoeswing;

import java.awt.AlphaComposite;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics2D;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.Observable;
import java.util.Observer;

import javax.swing.JPanel;

import org.holtz.zoe.Bug;
import org.holtz.zoe.World;

/**
 * A <code>JPanel</code> that can display a Zoe <code>World</code>.
 * @author Brian Holtz
 */
public class WorldPanel extends JPanel implements Observer, MouseMotionListener, MouseListener, ComponentListener {
	private static final long serialVersionUID = 201110192012L;
	public ZoePanel parent;
	public World world;
	public BugLabel selectedBug;
	
	public WorldPanel( ZoePanel zp ) {
		setLayout( null );
		parent = zp;
		addComponentListener(this);
	}

	public void createWorld() {
		world = new World( getSize() );
		world.addObserver(this);
	}

    public void add( Bug bug ) {
        BugLabel bugLabel = new BugLabel( bug, this );
        bug.setContext( bugLabel );
        add( bugLabel, BorderLayout.CENTER );
    	bugLabel.addMouseMotionListener(this);
    	bugLabel.addMouseListener(this);
    	bugLabel.repaint();
    }

	@Override
	public void update(Observable theWorld, Object newObj) {
		// java.util.Observer only knows Object
		if (newObj instanceof Bug) {
			add( (Bug)newObj );
		}
	}

    @Override
    public void mouseDragged(MouseEvent e) {
    	Component dragged = e.getComponent();
    	if (dragged instanceof BugLabel) {
    		BugLabel bugLabel = (BugLabel)dragged;
            bugLabel.bug.setXY( e.getX() + bugLabel.getX(),
            		e.getY() + bugLabel.getY() );
        	parent.worldStatusPanel.updateStats();
    	} else {
			System.err.println( "Unexpected Drag Event: " + e.toString() );
		}
    }

    @Override
    public void mouseMoved(MouseEvent e) {}

	@Override
	public void mouseClicked(MouseEvent e) {
		Component clicked = e.getComponent();
		if (clicked instanceof BugLabel) {
			BugLabel bug = (BugLabel)clicked;
			if (selectedBug == null) {
				selectedBug = bug;
				bug.select( true );
				bug.repaint();
		        Graphics2D g = (Graphics2D)bug.getParent().getGraphics();
		        AlphaComposite ac = 
		            AlphaComposite.getInstance(AlphaComposite.SRC_OVER,0.2f); 
		        g.setComposite(ac); 
	            g.setColor(Color.gray);
	            g.fillRect(10, 15, 900, 600);
			} else if (e.getClickCount() == 1) {
				if (selectedBug == bug) {
					selectedBug.select( false );
					selectedBug.repaint();
					selectedBug = null;
				} else {
					selectedBug.select( false );
					selectedBug.repaint();
					bug.select( true );
					bug.repaint();
					selectedBug = bug;
				}
			}
			if (e.getClickCount() == 2) {
				new BugFrame( bug.bug );
			}
			parent.worldStatusPanel.updateStats();
		} else {
			System.err.println( "Unexpected Click Event: " + e );
		}
	}

	@Override
	public void mouseEntered(MouseEvent arg0) {}

	@Override
	public void mouseExited(MouseEvent arg0) {}

	@Override
	public void mousePressed(MouseEvent arg0) {}

	@Override
	public void mouseReleased(MouseEvent arg0) {}

	@Override
	public void componentHidden(ComponentEvent arg0) {}

	@Override
	public void componentMoved(ComponentEvent arg0) {}

	@Override
	public void componentResized(ComponentEvent arg0) {
		if (world == null) return;
		world.resize( getSize() );
	}
	
	@Override
	public void componentShown(ComponentEvent arg0) {}
	
}
