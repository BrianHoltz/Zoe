package org.holtz.zoe;

import java.io.Serializable;
import java.util.ArrayDeque;
import java.util.Deque;
import org.holtz.zoe.zoel.ZoelVM;

/**
 * The expression of a <code>Bug</code>'s <code>Genotype</code> is a 
 * <code>PheneList</code> of <code>Phene</code>s.
 * @author Brian Holtz
 */
public class Phenotype implements Serializable {
    protected PheneList phenes;
    protected Bug bug;
    /**
     * A stack of Phene activations.
     */
    protected Deque<Phene> activePhenes = new ArrayDeque<Phene>();

    public Phenotype( Bug b ) {
        bug = b;
        phenes = new PheneList( bug );
    }

    // Pick a Phene to run and run it
    private void nextOrig() {
        Phene phene2Run = pickPhene2Run();
        if (phene2Run == null) return;
        if (activePhenes.peekFirst() != phene2Run) {
            activePhenes.addFirst( phene2Run );
        }
        if (phene2Run.Do() != ZoelVM.Turn.Continues) {
            activePhenes.removeFirst();
        }
    }

    public void next() {
        for (Phene phene: phenes) {
            if ((phene == activePhenes.peekFirst()) || phene.when()) {
                if (phene != activePhenes.peekFirst()) {
                    activePhenes.addFirst( phene );
                }
                ZoelVM.Turn doResult = phene.Do();
                if (doResult != ZoelVM.Turn.Continues) {
                    activePhenes.removeFirst();
                }
                if (doResult == ZoelVM.Turn.Finished) {
                    return;
                }
            }
        }
    }

    // Return the first phene in the run queue, or any higher-priority
    // phene that has become runnable.
    private Phene pickPhene2Run() {
        Phene activePhene = activePhenes.peekFirst();
        for (Phene phene : phenes) {
            if (phene == activePhene) {
                // Phenes are ordered by priority, so if we've gotten
                // to the active phene, then none of he remaining phenes
                // have enough priority to interrupt it.
                return activePhene;
            }
            if (phene.when()) {
                activePhene = phene;
                break;
            }
        }
        return activePhene;
    }
    /*
    public Phene pickPhene2RunProbabilistically() {
        recalculateExcitements();
        double eligibleExcitement = 0;
        Phene activePhene = activePhenes.peekFirst();
        for (Phene phene : phenes) {
            // Phenes cannot interrupt higher-priority phenes
            if (phene == activePhene) break;
            eligibleExcitement += phene.excitement();
        }
        if (eligibleExcitement == 0) return activePhene;
        double excitement2Skip = bug.random().nextDouble() * eligibleExcitement;
        for (Phene phene : phenes) {
            excitement2Skip -= phene.excitement();
            if (excitement2Skip <= 0) return phene;
        }
        return activePhene;
    }

    private void recalculateExcitements() {
        Phene activePhene = activePhenes.peekFirst();
        int priority = 0;
        for (Phene phene : phenes) {
            // Phenes cannot interrupt higher-priority phenes
            if (phene == activePhene) break;
            if (phene.when()) {
                phene.excite( priority );
            } else {
                phene.dampen( priority );
            }
            if (World.Trace) System.out.println( "Recalculated: " + phene.toString( "\n" ));
            priority++;
        }
    }
    */
    public String toString() {
        return toString( " " );
    }

    public String toString( String separator ) {
        String msg = bug.genotype.toString();
        return msg + separator + phenes.toString( separator );
    }
}
