package org.holtz.zoe.zoeswing;

import javax.swing.JFrame;
import org.holtz.zoe.World;

/**
 * A <code>JFrame</code> to hold a <code>SpeciesScoreBoardPanel</code>.
 * @author Brian Holtz
 */
public class SpeciesScoreBoardFrame extends JFrame {

    private static final long serialVersionUID = 201203242220L;
    private World world;
    
    //private SpeciesScoreBoardPanel speciesScoreBoardPanel;

    public SpeciesScoreBoardFrame( World theWorld ) {
        world = theWorld;
        setDefaultCloseOperation( JFrame.DISPOSE_ON_CLOSE );
        //bugPanel = new BugPanel( bug );
        //add( bugPanel );
        setTitle( "Species ScoreBoard" );
        pack();
        setVisible(true);
    }
}
