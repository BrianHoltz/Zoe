package org.holtz.zoe;

import java.awt.Color;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Random;

import org.holtz.zoe.zoel.Operation;
import org.holtz.zoe.zoel.ZoelTokenizer;

/**
* The species of a <code>Bug</code>, consisting of a <code>GeneList</code> of <code>Genes</code>.
* @author Brian Holtz
*/
public class Genotype {
	private static int numEverCreated = 0;
	private static ArrayList<Genotype> founders;
	private static String extension = ".zoe";
	private static Genotype algae = algae();
	
	public int id = getNextId();
    public Point birthPlace;
	public String name;
	private Genotype parent;
	public Color color;
	public GeneList genes;
	public int numLiving = 0;
	
	private Genotype( GeneList theGenes, String theName ) {
		genes = theGenes;
		name = theName;
		if (name.endsWith( extension )) {
			name = name.substring( 0, name.length() - extension.length() );
		}
		color = World.color( name );
	}
	public Genotype( Genotype mom, Random random, Point where ) {
	    this( mom, null, random, where );
	}
	public Genotype( Genotype mom, Genotype dad, Random random, Point where ) {
		parent = mom;
		birthPlace = where;
		// If parents are same species, then just mutate.
		if (mom == dad) dad = null;
		if (dad == null) {
	        color = World.mutateColor( parent.color, random );
		    genes = new GeneList( mom.genes, random );
		} else {
            color = World.mutateColor( parent.color, dad.color );
	        //System.out.println( "Mom:\n" + mom.toString( "\n" ));
	        //System.out.println( "Dad:\n" + dad.toString( "\n" ));
            genes = new GeneList( mom.genes, dad.genes, random );
            //System.out.println( "Child:\n" + toString( "\n" ));
		}
		
	}
	public Genotype( Random random ) {
		color = World.color( random.nextInt() );
		genes = new GeneList( random, World.MaxGenesOfRandomSpecies );
		//System.out.println( "Random genotype:\n" + toString( "\n" ));
	}
	public static Genotype algae() {
	    if (algae != null) return algae;
	    GeneList geneList = new GeneList();
	    geneList.add( Gene.split() );
	    algae = new Genotype( geneList, "Algae" );
	    algae.color = Color.GREEN;
	    return algae;
	}
	private static void loadFounders() {
		if (founders != null) return;
		founders  = new ArrayList<Genotype>();
		if (! World.LoadBugFiles) return;
		File dir = new File("bugs");
		File[] children = dir.listFiles();
		if (children == null) return;
		for (int i=0; i<children.length; i++) {
			if (children[i].isDirectory()) continue;
			if (! children[i].getName().endsWith( extension )) continue;
			try {
				GeneList genes = GeneList.parse( new ZoelTokenizer( new FileReader( children[i] )));
				Genotype species = new Genotype( genes, children[i].getName() );
				founders.add( species );
			} catch (FileNotFoundException e) {
				e.printStackTrace(); System.exit(1);
			} catch (Exception e) {
				System.err.println( "Error parsing " + children[i].getName()
					+ ": "+ e.toString() ); System.exit(1);
			}
		}
		//for (Genotype species : founders) System.out.println( species.toString( "\n" ));
	}
	public static int getNextId() {
		return ++numEverCreated;
	}
	public static int getNumEverCreated() {
		return numEverCreated;
	}
	public static int getNumFounders() {
	    loadFounders();
	    return founders.size();
	}
	public static Genotype founder(int nth) {
	    loadFounders();
	    if (founders.size() < (nth+1)) return null;
	    return founders.get( nth );
	}
    public static Genotype random(Random random) {
        loadFounders();
        if (founders.size() > 0) return founders.get( random.nextInt( founders.size() ) );
        //if (random.nextBoolean()) return algae();
        return new Genotype( random );
    }
    public void addMember( Bug bug ) {
        if (birthPlace == null) {
            birthPlace = new Point( bug.x(), bug.y() );
        }
        numLiving++;
    }
	public String toString() {
		return toString( " ", null, null );
	}
	public String toString( String separator ) {
		return toString( separator, null, null );
	}
	public String toString( String statementSeparator, Operation currStmt, String cursor ) {
		String msg = "Species " + id;
		if (name != null) msg += " (" + name + ")";
		if (parent != null) {
			msg += " parent=" + parent.id;
		}
		if (! statementSeparator.startsWith("\n")) return msg;
		return msg + statementSeparator + genes.toString( statementSeparator, currStmt, cursor );
	}
}
