package org.holtz.zoe;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * A list of <code>Phene</code>s.
 * @author Brian Holtz
 */
public class PheneList extends ArrayList<Phene> implements Serializable {

    public PheneList(Bug bug) {
        for (Gene gene : bug.genotype.genes) {
            add( new Phene( bug, gene ));
        }
    }

    private static final long serialVersionUID = 201111152047L;

    public String toString(String separator) {
        String msg = "";
        String sep2Use = "";
        for (Phene phene : this) {
            msg += sep2Use + phene.toString( separator );
            sep2Use = separator;
        }
        return msg;
    }
}
