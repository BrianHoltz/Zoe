package org.holtz.zoe;

import java.io.Serializable;
import java.util.Random;

import org.holtz.zoe.zoel.ExpressionList;
import org.holtz.zoe.zoel.Operation;
import org.holtz.zoe.zoel.Operator;
import org.holtz.zoe.zoel.ZoelTokenizer;

/**
 * The unit of heredity of a Zoe <code>Bug</code>, consisting of <code>Zoel</code> actions and the <code>Zoel</code> conditions under which to fire them.
 * @author Brian Holtz
 */
public class Gene implements Serializable {
    protected int id = getNextId();
    protected ExpressionList when;
    protected ExpressionList action;
    private static Gene split = split();

    private static int numEverCreated = 0;

    protected Gene( ExpressionList w, ExpressionList a ) {
        when = w;
        action = a;
    }

    public Gene( Random random ) {
        when = new ExpressionList( random, ExpressionList.NoActionOperators );
        //System.out.println( "Random when:\n" + when.toString("\n"));
        action = new ExpressionList();
        action.add( new Operation( random, true ));
        for( int n = random.nextInt( 2 ); n > 0; n--) {
            action.add( new Operation( random, true ));
        }
    }

    public boolean isFertile() {
        return action.isFertile();
    }

    public static Gene parse( ZoelTokenizer zoelTokenizer ) throws Exception {
        if (zoelTokenizer.nextToken() != ZoelTokenizer.TT_WORD
            || (    ! zoelTokenizer.sval.equals( "When" )
                 && ! zoelTokenizer.sval.equals( "Do" )))
        {
            throw new Exception( "Gene must start with 'When' or 'Do': "
                    + zoelTokenizer.toString());
        }
        ExpressionList when = null;
        if (zoelTokenizer.sval.equals( "When" )) {
            when = ExpressionList.parse( zoelTokenizer );
        } else {
            zoelTokenizer.pushBack();
        }
        if (zoelTokenizer.nextToken() != ZoelTokenizer.TT_WORD
            || ! zoelTokenizer.sval.equals( "Do" ))
        {
            throw new Exception( "Gene must include a 'Do': "
                    + zoelTokenizer.toString());
        }
        ExpressionList action = ExpressionList.parse( zoelTokenizer );
        return new Gene( when, action );
    }
    
    public static Gene split() {
        if (split != null) return split;
        Operation spawn = new Operation( Operator.Split );
        ExpressionList doList = new ExpressionList();
        doList.add( spawn );
        split = new Gene( null, doList );
        return split;
    }

    public String toString(String statementSeparator ) {
        return toString( statementSeparator, null, null );
    }
    public String toString(String statementSeparator, Operation currStmt, String cursor) {
        String msg = "";
        String newSeparator = statementSeparator;
        if (statementSeparator.contains("\n")) newSeparator += "   ";
        if (when != null) {
            msg += "When " + when.toString( statementSeparator, currStmt, cursor ) + " ";
        }
        msg += "Do " + action.toString( statementSeparator, currStmt, cursor );
        return msg;
    }

    private static int getNextId() {
        return ++numEverCreated;
    }
}
