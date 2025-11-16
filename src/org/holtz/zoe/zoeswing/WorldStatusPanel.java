package org.holtz.zoe.zoeswing;

import java.awt.Component;
import java.util.Date;

import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.holtz.zoe.Bug;
import org.holtz.zoe.Genotype;
import org.holtz.zoe.World;
/**
 * A <code>JPanel</code> that displays current statistics for a Zoe <code>World</code>.
 * @author Brian Holtz
 */
public class WorldStatusPanel extends JPanel {
    private static final long serialVersionUID = 201110071925L;

    private WorldPanel worldPanel;

    private long hertz = 0;
    private Date lastSampleTime = new Date();
    private int lastSampleCycle = 0;

    private JLabel worldLabel, bugLabel;

    public WorldStatusPanel( WorldPanel wp ) {
        worldPanel = wp;
        setLayout( new BoxLayout(this, BoxLayout.Y_AXIS ));
        bugLabel = new JLabel();
        bugLabel.setAlignmentX( Component.CENTER_ALIGNMENT );
        bugLabel.setToolTipText( "Properties of selected bug." );
        add( bugLabel );
        worldLabel = new JLabel();
        worldLabel.setAlignmentX( Component.CENTER_ALIGNMENT );
        worldLabel.setToolTipText( "World cycle." );
        add( worldLabel );
    }

    public void updateStats(World world) {
        if (world == null) return;
        Date now = new Date();
        int numLive = world.numLive();
        long sampleLength = now.getTime() - lastSampleTime.getTime();
        if (sampleLength > 5000 ) {
            try {
                double sampleSecs = sampleLength / 1000.0;
                int sampleCycles = world.cycle - lastSampleCycle;
                hertz = Math.round( sampleCycles / sampleSecs );
                lastSampleTime = now;
                lastSampleCycle = world.cycle;
            } catch (ArithmeticException ex) {}
        }
        long running = now.getTime() - world.start.getTime();
        long hours = running / (1000 * 60 * 60);
        running -= hours * 1000 * 60 * 60;
        double mins = running / (1000 * 60.0);
        String worldText = "";
        worldText += "World " + world.seed + ": " + world.cycle
            + " / " + hours + String.format( "h %.1f", mins ) + "m = "
            + hertz * numLive / 1000 + "Khz";
        Genotype top = world.topSpecies();
        worldText += "    " + numLive + " alive";
        if (top != null) worldText += " [" + top.numLiving + " are species " + top.id + "]";
        worldText += " + " + world.numDead() + " dead, "
            + world.numSpecies() + " + "
            + (Genotype.getNumEverCreated() - world.numSpecies()) + " extinct"
            ;
        double strength = world.strength();
        double mass = world.mass();
        worldText += "    "
            + String.format( "Energy %.0f =", strength + mass )
            + String.format( "$%.0f", strength)
            + String.format( " + %.0fg", mass)
            + String.format( " (%.0f photons)", world.energyEverPhotosynthesized);
        worldLabel.setText( worldText );
        String bugText = "Selected Bug";
        if (worldPanel.selectedBug == null ) {
            bugText += ": none";
        } else {
            Bug bug = worldPanel.selectedBug.bug;
            bugText += " " + bug.toString( " ", false );
            bugText += "  Descendants: " + bug.descendentsCount();
            bugText += "  Ancestors: " + bug.genealogy( 5 );
        }
        bugLabel.setText( bugText );
    }

}
