package org.holtz.zoe;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.ListIterator;
import java.util.Random;

import org.holtz.zoe.zoel.Operation;
import org.holtz.zoe.zoel.Randomizer;
import org.holtz.zoe.zoel.ZoelTokenizer;

/**
 * A list of <code>Gene</code>s.
 * 
 * @author Brian Holtz
 */
public class GeneList extends ArrayList<Gene> implements Serializable {

    private static final long serialVersionUID = 201111110751L;
    
    public GeneList() {
        super();
    }

    public GeneList( Random random, int maxGenes ) {
        for( int n = 1 + random.nextInt( maxGenes ); n > 0; n--) {
            add( new Gene( random ));
        }
        ensureFertility();
        assert( size() > 0 );
    }

    public GeneList(GeneList mom, Random random) {
        copy( mom );
        assert( size() > 0 );
        mutate( random );
        ensureFertility();
        assert( size() > 0 );
    }

    private enum Recombination {
        Interleave,
        Crossover,
        Swap
    }
    
    public GeneList( GeneList mom, GeneList dad, Random random ) {
        switch (Randomizer.next( random, Recombination.class )) {
            case Interleave:
                interleave( mom, dad, random );
                break;
            case Crossover:
                crossover( mom, dad, random );
                break;
            case Swap:
                borrowOneGeneFromDad( mom, dad, random );
                break;
        }
        ensureFertility();
        assert( size() > 0 );
    }

    private void interleave( GeneList mom, GeneList dad, Random random ) {
        boolean nextFromMom = random.nextBoolean();
        ListIterator<Gene> dadItr = dad.listIterator();
        ListIterator<Gene> momItr = mom.listIterator();
        while (dadItr.hasNext() && momItr.hasNext()) {
            if (nextFromMom) {
                add( momItr.next() );
                dadItr.next();
            } else {
                add( dadItr.next() );
                momItr.next();
            }
            nextFromMom = ! nextFromMom;
            if (! momItr.hasNext()) {
                while (dadItr.hasNext()) {
                    add( dadItr.next() );
                }
            } else if (! dadItr.hasNext()) {
                while (momItr.hasNext()) {
                    add( momItr.next() );
                }
            }
        }
        assert( size() > 0 );
    }
    
    private void crossover( GeneList mom, GeneList dad, Random random ) {
        if (random.nextBoolean()) {
            crossover( dad, mom, random );
            return;
        }
        double splicePoint = random.nextDouble();
        // Add all dad's genes up to the splice point
        int dadGenes2Copy = (int)Math.round( splicePoint * dad.size());
        dadGenes2Copy = Math.min( dadGenes2Copy, 1 );
        ListIterator<Gene> itr = dad.listIterator();
        while (itr.hasNext() && dadGenes2Copy > 0) {
            Gene gene = itr.next();
            add( gene );
            dadGenes2Copy--;
        }
        // Add all mom's genes after the splice point
        int momGenes2Copy = (int)Math.round( (1 - splicePoint) * mom.size());
        itr = mom.listIterator( mom.size() - momGenes2Copy );
        while (itr.hasNext()) {
            Gene gene = itr.next();
            add( gene );
        }
        assert( size() > 0 );
    }

    private void borrowOneGeneFromDad( GeneList mom, GeneList dad, Random random ) {
        copy( mom );
        ListIterator<Gene> itr = dad.listIterator( random.nextInt( dad.size()) );
        Gene gene2Borrow = itr.next();
        itr = this.listIterator( random.nextInt( size()));
        itr.add( gene2Borrow );
    }
 
    private void copy(GeneList template) {
        for (Gene gene : template) {
            add( gene );
        }
    }
    
    private enum Mutation {
        Insert,
        Delete,
        Translocate,
        Modify
    }

    private void mutate( Random random ) {
        assert( size() > 0 );
        int mySize = size();
        if (mySize < 1) {
            System.out.println("mySize=" + mySize);
        }
        int pos2Mutate = random.nextInt( mySize );
        ListIterator<Gene> itr = listIterator( pos2Mutate );
        Gene gene = itr.next();
        switch (Randomizer.next( random, Mutation.class )) {
            case Delete:
                if (size() > 1) {
                    itr.remove();
                    return;
                }
                // fall through
            case Translocate:
                if (size() > 1) {
                    itr.remove();
                    itr = listIterator( random.nextInt( size() ));
                    itr.add( gene );
                    return;
                }
                // fall through
            case Insert:
                itr.add( new Gene( random ));
                return;
            case Modify:
                itr.remove();
                gene = new Gene( random );
                itr.add( gene );
                return;
        }
        assert( size() > 0 );
    }
    
    private void ensureFertility() {
        /*
        for (Gene gene : this) {
            if (gene.isFertile()) return;
        }
        add( Gene.split() );
        */
    }
    
    public static GeneList parse(ZoelTokenizer zoelTokenizer) throws Exception {
        GeneList genes = new GeneList();
        while (zoelTokenizer.nextToken() != ZoelTokenizer.TT_EOF) {
            zoelTokenizer.pushBack();
            genes.add( Gene.parse( zoelTokenizer ) );
        }
        return genes;
    }

    public String toString( String statementSeparator ) {
        return toString( statementSeparator, null, null );
    }
    
    public String toString(
        String statementSeparator, 
        Operation currStmt,
        String cursor )
    {
        String msg = "";
        String separator = "";
        for (Gene gene : this) {
            msg += separator + gene.toString( statementSeparator, currStmt, cursor );
            separator = statementSeparator;
        }
        return msg;
    }
}
