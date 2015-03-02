package org.holtz.zoe.zoel;

import java.util.ArrayList;
import java.util.ListIterator;
import java.util.Random;

/**
 * A list of <code>Expressions</code> of a <code>Bug</code>, constituting a block statement in a <code>Zoel</code> program.
 * @author Brian Holtz
 */
public class ExpressionList extends Expression {
    public static final boolean NoActionOperators = false;
    
	private ArrayList<Expression> expressions = new ArrayList<Expression>();

	public ExpressionList() {
	}

    public ExpressionList( Random random ) {
        this( random, true );
    }
    
    public ExpressionList( Random random, boolean actionsAllowed ) {
        expressions.add( new RegisterReference( random ));
        if (random.nextBoolean()) return;
        if (random.nextBoolean()) {
            expressions.add( new Operation( Operator.Not ));
            return;
        }
        Operator theOp = random.nextBoolean() ? Operator.And : Operator.Or;
		expressions.add( new Operation( theOp, new RegisterReference( random )));
		/*
		for( int n = random.nextInt( maxExpressions-1 ); n > 0; n--) {
			ExpressionListCall pivot = randomPosition( random );
			pivot.add( Expression.random( random, actionsAllowed ) );
		}
		*/
	}
	
	@Override
	public ExpressionList copy() {
		return new ExpressionList( this );
	}
	
	// Makes a deep copy so that it can mutate separately
	public ExpressionList( ArrayList<Expression> expressions2Copy ) {
		expressions = new ArrayList<Expression>();
		for (Expression expression2Copy: expressions2Copy) {
			expressions.add( expression2Copy.copy() );
		}
	}

	// Makes a deep copy so that it can mutate separately
	public ExpressionList( ExpressionList theExpressions ) {
		this( theExpressions.expressions );
	}

	// Makes a deep copy so that it can mutate separately
	public ExpressionList( ExpressionList mom, ExpressionList dad, Random random ) {
		if (random.nextDouble() > 0.5) {
			crossOver( mom, dad );
		} else {
			crossOver( dad, mom );
		}
	}

    public int size() {
        return expressions.size();
    }
    
    @Override
    public boolean isFertile() {
        for (Expression expr : expressions) {
            if (expr.isFertile()) return true;
        }
        return false;
    }

    public Expression get( int index ) {
        return expressions.get( index );
    }
    
    public ExpressionList add( int index, Expression expr ) {
        expressions.add( index, expr );
        return this;
    }
    
	private void crossOver( ExpressionList parent1, ExpressionList parent2 ) {
		expressions = new ArrayList<Expression>();
		int expressions2Copy = 1 + parent1.expressions.size() / 2;
		for (Expression expression2Copy : parent1.expressions) {
			if (expressions2Copy == 0) break;
			expressions.add( expression2Copy.copy() );
			expressions2Copy--;
		}
		int expressions2Skip = parent2.expressions.size() / 2;
		for (Expression expression2Copy : parent2.expressions) {
			if (expressions2Skip > 0) {
				expressions2Skip--;
				continue;
			}
			expressions.add( expression2Copy.copy() );
		}
	}
	
	public ExpressionList add( Expression Expression ) {
		expressions.add( Expression );
		return this;
	}
	
	@Override
	public int totalStatements() {
		int total = 0;
		for (Expression expression : expressions) {
			total += 1 + expression.totalStatements();
		}
		return total;
	}
	
	@Override
	public ExpressionListCall nthStatement( Integer[] nth ) {
		ExpressionListCall itr = new ExpressionListCall( this );
		for (Expression expression : expressions) {
			if (nth[0] == 0) return itr;
			itr.next();
			nth[0]--;
			ExpressionListCall found = expression.nthStatement( nth );
			if (found != null) return found;
		}
		return null;
	}
	
	public ExpressionListCall randomPosition( Random random ) {
		int nth = random.nextInt( totalStatements() );
		Integer[] ary = new Integer[1];
		ary[0] = nth;
		//System.out.println( "nth=" + nth + " of " + totalStatements() + " in:" );
		//System.out.println( toString( "\n" ));
		ExpressionListCall found = nthStatement( ary );
		//System.out.println( "is: " + found.toString() );
		return found;
	}

    private enum Mutation {
        Insert,
        Delete,
        Translocate,
        Replace
    }

    private void mutate( int nth, Random random, boolean actionOperatorsAllowed ) {
		ListIterator<Expression> itr = expressions.listIterator( nth );
		Expression expression = itr.next();
		switch (Randomizer.next( random, Mutation.class )) {
			case Insert:
				expression = Expression.random( random, actionOperatorsAllowed );
				itr.add( expression );
				return;
			case Delete:
				itr.remove();
				return;
			case Translocate:
				itr.remove();
				itr = expressions.listIterator( random.nextInt( expressions.size() ));
				itr.add( expression );
				return;
			case Replace:
				itr.remove();
				expression = Expression.random( random, actionOperatorsAllowed );
				itr.add( expression );
				return;
		}
	}

	public String toString() {
		return toString( " ", null, null );
	}
	public String toString( String separator ) {
		return toString( separator, null, null );
	}
	public String toString( String stmtSeparator, Expression currExpr, String cursor ) {
		if (stmtSeparator == null) stmtSeparator = "";
		String msg = "";
		msg += "{";
		String newSeparator = stmtSeparator;
		if (stmtSeparator.contains("\n")) newSeparator += "   ";
		String comma = "";
		for (Expression expression : expressions) {
			msg += comma + newSeparator 
			    + expression.toString( newSeparator, currExpr, cursor );
			comma = ",";
		}
		msg += stmtSeparator + "}";
		return msg;
	}
	public static ExpressionList parse( ZoelTokenizer zoelTokenizer ) throws Exception {
		int token = zoelTokenizer.nextToken();
		if (token != '{') {
			throw new Exception( "ExpressionList does not start with '{': "
					+ zoelTokenizer.toString());
		}
		ExpressionList Expressions = new ExpressionList();
		while (true) {
			if (zoelTokenizer.nextToken() == '}') break;
			if (zoelTokenizer.ttype != ',') zoelTokenizer.pushBack();
			Expressions.add( Expression.parse( zoelTokenizer ));
		}
		return Expressions;
	}
}
