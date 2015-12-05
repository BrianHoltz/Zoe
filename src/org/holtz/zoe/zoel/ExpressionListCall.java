package org.holtz.zoe.zoel;

/**
* An entry in the <code>CallStack</code> of a <code>Bug</code> storing execution state for a <code>StatementList</code>.
* @author Brian Holtz
*/
public class ExpressionListCall implements CallRecord {
    private ExpressionList expressions;
    private int nextStatement = 0;

    public ExpressionListCall( ExpressionList theStatements ) {
        expressions = theStatements;
    }

    public Expression next() {
        if (nextStatement >= expressions.size()) return null;
        return expressions.get( nextStatement++ );
    }
    public void repeat() {
        if (nextStatement <= 0) return;
        --nextStatement;
    }
    public ExpressionList expressions() {
        return expressions;
    }
    public int indexOfNext() {
        return nextStatement;
    }
    public Expression peek() {
        if (nextStatement >= expressions.size()) return null;
        return expressions.get( nextStatement );
    }
    public Expression peekBack() {
        if (nextStatement-1 < 0) return null;
        return expressions.get( nextStatement-1 );
    }
    public void add( Expression expr ) {
        expressions.add( nextStatement, expr );
    }

    public String toString() {
        return "nextStatement=" + nextStatement + " in " + expressions.toString();
    }
}
