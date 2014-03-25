package org.holtz.zoe.zoeswing;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;

import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.Timer;

import org.holtz.zoe.Bug;
import org.holtz.zoe.World;
import org.holtz.zoe.ZObject;
/**
 * A <code>JPanel</code> that can display a Zoe <code>WorldPanel</code> and its status.
 * @author Brian Holtz
 */
public class ZoePanel extends JPanel
	implements ActionListener
{
	private static final long serialVersionUID = 201104051508L;
	
	private WorldPanel worldPanel;
	private Timer runTimer;
	public boolean running = false;
	private ZObject clipboard;
	
	public WorldStatusPanel worldStatusPanel;

	private JMenuItem worldPropsMenuItem;
	private JMenuItem runNextCycleMenuItem, runGoMenuItem, runStopMenuItem, runNextBugMenuItem;
	private JMenuItem cutMenuItem, copyMenuItem, propertiesMenuItem, 
	    selectMotherMenuItem, selectFatherMenuItem, selectMateMenuItem,
	    selectFirstChildMenuItem, selectNextSiblingMenuItem,
	    selectNoneMenuItem;
	
	public ZoePanel() {
		super( new BorderLayout() );
		World.initProperties();
        this.setOpaque(true);
        if (World.SizeWorldToScreen) {
        	setPreferredSize( Toolkit.getDefaultToolkit().getScreenSize() );
        } else {
        	setPreferredSize( new Dimension( World.Width, World.Height ));
        }
        
        add(createMenuBar(), BorderLayout.NORTH);
        
        worldPanel = new WorldPanel( this );
        add( worldPanel, BorderLayout.CENTER );
        
		worldStatusPanel = new WorldStatusPanel( worldPanel );
		add( worldStatusPanel, BorderLayout.SOUTH );
		
		running = World.AutoStart;
        runTimer = new Timer( World.MinMilliSecsPerTurn, this );
        runTimer.start(); 
	}
	
    private JMenuBar createMenuBar() {
        JMenuBar menuBar = new JMenuBar();
        JMenu menu;
        JMenuItem menuItem;
        
        menu = new JMenu("World");
        menu.setMnemonic(KeyEvent.VK_W);
        menu.getAccessibleContext().setAccessibleDescription(
        	"Load, save, or inspect the World");
        menuBar.add(menu);
        /*
        menuItem = new JMenuItem("Open", KeyEvent.VK_O);
        menuItem.setAccelerator(KeyStroke.getKeyStroke(
        		KeyEvent.VK_O, ActionEvent.CTRL_MASK));
        menuItem.getAccessibleContext().setAccessibleDescription(
        	"Restore the World");
        menu.add(menuItem);

        menuItem = new JMenuItem("Save", KeyEvent.VK_S);
        menuItem.getAccessibleContext().setAccessibleDescription(
        	"Save the World");
        menu.add(menuItem);

        menuItem = new JMenuItem("Save As...", KeyEvent.VK_A);
        menuItem.getAccessibleContext().setAccessibleDescription(
        	"Fork the World");
        menu.add(menuItem);
		*/
        menuItem = new JMenuItem("Properties...", KeyEvent.VK_P);
        menuItem.getAccessibleContext().setAccessibleDescription(
        	"Change the World");
        menuItem.addActionListener(this);
        menu.add(menuItem);
        worldPropsMenuItem = menuItem;

        menu = new JMenu("Edit");
        menu.setMnemonic(KeyEvent.VK_E);
        menu.getAccessibleContext().setAccessibleDescription(
        	"Cut, copy, paste, find, inspect entities in the World");
        menuBar.add(menu);

        menuItem = new JMenuItem("Cut", KeyEvent.VK_U);
        menuItem.setAccelerator(KeyStroke.getKeyStroke( KeyEvent.VK_X, 
        		Toolkit.getDefaultToolkit().getMenuShortcutKeyMask() ));
        menuItem.getAccessibleContext().setAccessibleDescription(
        	"Cut the selected Bug or item");
        menuItem.addActionListener(this);
        menu.add(menuItem);
        cutMenuItem = menuItem;
        /*
        menuItem = new JMenuItem("Copy", KeyEvent.VK_C);
        menuItem.setAccelerator(KeyStroke.getKeyStroke(
        		KeyEvent.VK_C, ActionEvent.CTRL_MASK));
        menuItem.getAccessibleContext().setAccessibleDescription(
        	"Copy the selected Bug or item");
        menu.add(menuItem);
        menuItem.addActionListener(this);
        copyMenuItem = menuItem;

        menuItem = new JMenuItem("Paste", KeyEvent.VK_P);
        menuItem.setAccelerator(KeyStroke.getKeyStroke(
        		KeyEvent.VK_V, ActionEvent.CTRL_MASK));
        menuItem.getAccessibleContext().setAccessibleDescription(
        	"Paste the Bug or item");
        menu.add(menuItem);
        menuItem.addActionListener(this);
        //pasteMenuItem = menuItem;

        menuItem = new JMenuItem("Find...", KeyEvent.VK_F);
        menuItem.setAccelerator(KeyStroke.getKeyStroke(
        		KeyEvent.VK_F, ActionEvent.CTRL_MASK));
        menuItem.getAccessibleContext().setAccessibleDescription(
        	"Find a Bug or Species");
        menu.add(menuItem);

        menuItem = new JMenuItem("Find Next", KeyEvent.VK_N);
        menuItem.setAccelerator(KeyStroke.getKeyStroke(
        		KeyEvent.VK_G, ActionEvent.CTRL_MASK));
        menuItem.getAccessibleContext().setAccessibleDescription(
        	"Find the next item");
        menu.add(menuItem);
		*/
        menuItem = new JMenuItem("Properties", KeyEvent.VK_R);
        menuItem.setAccelerator(KeyStroke.getKeyStroke( KeyEvent.VK_I,
        		Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        menuItem.getAccessibleContext().setAccessibleDescription(
        	"Show properties of the selected item");
        menuItem.addActionListener(this);
        menu.add(menuItem);
        propertiesMenuItem = menuItem;

        menuItem = new JMenuItem("Select Mother", KeyEvent.VK_M);
        menuItem.setAccelerator(KeyStroke.getKeyStroke( KeyEvent.VK_M, 
                Toolkit.getDefaultToolkit().getMenuShortcutKeyMask() ));
        menuItem.getAccessibleContext().setAccessibleDescription(
            "Select the mother of the selected bug");
        menuItem.addActionListener(this);
        menu.add(menuItem);
        selectMotherMenuItem = menuItem;

        menuItem = new JMenuItem("Select Father", KeyEvent.VK_F);
        menuItem.setAccelerator(KeyStroke.getKeyStroke( KeyEvent.VK_F, 
                Toolkit.getDefaultToolkit().getMenuShortcutKeyMask() ));
        menuItem.getAccessibleContext().setAccessibleDescription(
            "Select the father (if any) of the selected bug");
        menuItem.addActionListener(this);
        menu.add(menuItem);
        selectFatherMenuItem = menuItem;

        menuItem = new JMenuItem("Select Mate", KeyEvent.VK_H);
        menuItem.setAccelerator(KeyStroke.getKeyStroke( KeyEvent.VK_H, 
                Toolkit.getDefaultToolkit().getMenuShortcutKeyMask() ));
        menuItem.getAccessibleContext().setAccessibleDescription(
            "Select the last mate (if any) of the selected bug");
        menuItem.addActionListener(this);
        menu.add(menuItem);
        selectMateMenuItem = menuItem;

        menuItem = new JMenuItem("Select First Child", KeyEvent.VK_K);
        menuItem.setAccelerator(KeyStroke.getKeyStroke( KeyEvent.VK_K, 
                Toolkit.getDefaultToolkit().getMenuShortcutKeyMask() ));
        menuItem.getAccessibleContext().setAccessibleDescription(
            "Select the first living child of the selected bug");
        menuItem.addActionListener(this);
        menu.add(menuItem);
        selectFirstChildMenuItem = menuItem;

        menuItem = new JMenuItem("Select Next Sibling", KeyEvent.VK_S);
        menuItem.setAccelerator(KeyStroke.getKeyStroke( KeyEvent.VK_S, 
                Toolkit.getDefaultToolkit().getMenuShortcutKeyMask() ));
        menuItem.getAccessibleContext().setAccessibleDescription(
            "Select the next living sibling of the selected bug");
        menuItem.addActionListener(this);
        menu.add(menuItem);
        selectNextSiblingMenuItem = menuItem;

        menuItem = new JMenuItem("Select None", KeyEvent.VK_O);
        menuItem.setAccelerator(KeyStroke.getKeyStroke( KeyEvent.VK_O, 
                Toolkit.getDefaultToolkit().getMenuShortcutKeyMask() ));
        menuItem.getAccessibleContext().setAccessibleDescription(
        	"De-select the selected item");
        menuItem.addActionListener(this);
        menu.add(menuItem);
        selectNoneMenuItem = menuItem;

        menu = new JMenu("Run");
        menu.setMnemonic(KeyEvent.VK_R);
        menu.getAccessibleContext().setAccessibleDescription(
        	"Start, stop, or step the World simulation");
        menuBar.add(menu);

        menuItem = new JMenuItem("Go", KeyEvent.VK_G);
        menuItem.setAccelerator(KeyStroke.getKeyStroke(
        		KeyEvent.VK_DOWN, 0));
        menuItem.getAccessibleContext().setAccessibleDescription(
        	"Run the World");
        menuItem.addActionListener(this);
        menu.add(menuItem);
        runGoMenuItem = menuItem;

        menuItem = new JMenuItem("Stop", KeyEvent.VK_S);
        menuItem.setAccelerator(KeyStroke.getKeyStroke(
        		KeyEvent.VK_UP, 0));
        menuItem.getAccessibleContext().setAccessibleDescription(
        	"Stop the World");
        menuItem.addActionListener(this);
        menu.add(menuItem);
        runStopMenuItem = menuItem;

        menuItem = new JMenuItem("Next Cycle", KeyEvent.VK_N);
        menuItem.setAccelerator(KeyStroke.getKeyStroke(
        		KeyEvent.VK_RIGHT, 0));
        menuItem.getAccessibleContext().setAccessibleDescription(
        	"Run the World until the beginning of the next cycle");
        menuItem.addActionListener(this);
        menu.add(menuItem);
        runNextCycleMenuItem = menuItem;

        menuItem = new JMenuItem("Next Bug", KeyEvent.VK_B);
        menuItem.setAccelerator(KeyStroke.getKeyStroke(
        		KeyEvent.VK_LEFT, 0));
        menuItem.getAccessibleContext().setAccessibleDescription(
        	"Run the World until the beginning of the next bug's turn");
        menuItem.addActionListener(this);
        menu.add(menuItem);
        runNextBugMenuItem = menuItem;

        return menuBar;
    }
    
    private void selectOrBeep( Bug bug ) {
        if (bug == null) {
            Toolkit.getDefaultToolkit().beep(); 
            return;
        }
        if (worldPanel.selectedBug != null) {
            worldPanel.selectedBug.select( false );
            worldPanel.selectedBug.repaint();
        }
        clipboard = bug;
        worldPanel.selectedBug = (BugLabel)clipboard.getContext();
        worldPanel.selectedBug.select( true );
        worldPanel.selectedBug.repaint();
        worldStatusPanel.updateStats();
    }

    public void actionPerformed(ActionEvent e) {
    	Object source = e.getSource();
    	World world = worldPanel.world;
    	if (source == runNextCycleMenuItem) {
    		running = false;
        	world.nextWorldCycle();
        	worldStatusPanel.updateStats();
    	} else if (source == runNextBugMenuItem ) {
    		world.nextBugCycle();
			worldStatusPanel.updateStats();
    	} else if (source == runGoMenuItem ) {
    		running = true;
    	} else if (source == runStopMenuItem ) {
    		running = false;
    	} else if (source == runTimer) {
    		if (world == null) {
    			worldPanel.createWorld();
    			worldStatusPanel.updateStats();
    		}
    		if (running && world != null) {
    			world.nextWorldCycle();
    			worldStatusPanel.updateStats();
    			if (world.numLive() == 0) {
    				running = false;
    			}
    		}
    	} else if (source == propertiesMenuItem) {
    		if (worldPanel.selectedBug != null) {
    			new BugFrame( worldPanel.selectedBug.bug );
    		}
    	} else if (source == cutMenuItem) {
    		if (worldPanel.selectedBug != null) {
    			clipboard = worldPanel.selectedBug.bug;
    			worldPanel.selectedBug.bug.disappear();
                worldPanel.selectedBug.repaint();
            	worldPanel.selectedBug = null;
                worldStatusPanel.updateStats();
    		}
    	} else if (source == copyMenuItem) {
    		if (worldPanel.selectedBug != null) {
    			clipboard = worldPanel.selectedBug.bug;
    		}
    	} else if (source == copyMenuItem) {
    		if (clipboard != null) {
    		}
    	} else if (source == worldPropsMenuItem) {
    		File propsFile = new File( "Zoe.properties" );
    		try {
				Runtime.getRuntime().exec(
					new String[] {"/usr/bin/open", propsFile.getAbsolutePath()} );
			} catch (IOException e1) {
				System.err.println( "Cannot open Zoe.properties: " + e1.toString());
			}
    	} else if (source == selectMotherMenuItem) {
    	    selectOrBeep( worldPanel.selectedBug == null ? null : worldPanel.selectedBug.bug.mother );
        } else if (source == selectFatherMenuItem) {
            selectOrBeep( worldPanel.selectedBug == null ? null : worldPanel.selectedBug.bug.father );
        } else if (source == selectMateMenuItem) {
            selectOrBeep( worldPanel.selectedBug == null ? null : worldPanel.selectedBug.bug.lastMate );
        } else if (source == selectFirstChildMenuItem) {
            selectOrBeep( worldPanel.selectedBug == null ? null : worldPanel.selectedBug.bug.nextDescendant( null ));
        } else if (source == selectNextSiblingMenuItem) {
            selectOrBeep( worldPanel.selectedBug == null ? null : worldPanel.selectedBug.bug.nextSibling());
    	} else if (source == selectNoneMenuItem) {
    	    if (worldPanel.selectedBug != null) {
    	        worldPanel.selectedBug.select( false );
    	        worldPanel.selectedBug.repaint();
    	    }
    	    worldPanel.selectedBug = null;
            worldStatusPanel.updateStats();
    	} else {
    		System.err.println( e );
    	}
    }
}
