package org.holtz.zoe.zoel;

import org.holtz.zoe.Point;
import org.holtz.zoe.World;

/**
 * A virtual machine that executes Zoel code for a <code>ZoelVMHost</code>.
 * @author Brian Holtz
 */
public class ZoelVM {
	/**
	 * Whether a ZoelVM has executed a turn-ending Operation.
	 */
	public enum Turn { 
		Continues,
        Finished,
        Exited
	}
	
	private ZoelVMHost host;
	private Stack<CallRecord> callStack = new Stack<CallRecord>();
	private Stack<Literal> dataStack = new Stack<Literal>();

	public ZoelVM( ZoelVMHost h, ExpressionList main ) {
		host = h;
		dataStack.val2PopWhenEmpty = new Number(0);
		callStack.push( new ExpressionListCall( main ));
	}

	public ZoelVM poke( Literal val ) {
		dataStack.poke( val );
		return this;
	}

	private ZoelVM poke( boolean val ) {
		int num = val ? 1 : 0;
		dataStack.poke( new Number(num) );
		return this;
	}

    private ZoelVM poke( double val ) {
        dataStack.poke( new Number(val) );
        return this;
    }

	public Literal peek() {
		return dataStack.peek();
	}
	
	public Literal pop() {
		return dataStack.pop();
	}
	
	public ZoelVM push( double val ) {
		return push( new Number(val) );
	}

	public ZoelVM push( boolean val ) {
		int num = val ? 1 : 0;
		return push( new Number(num) );
	}

	public ZoelVM push( Literal val ) {
		dataStack.push( val, host.maxDataSize() );
		return this;
	}
	
	private Turn evaluate( ExpressionList arg ) {
		CallRecord call = new ExpressionListCall( arg );
		callStack.push( call );
		return Turn.Continues;
	}

	private Turn evaluate( Literal arg ) {
		dataStack.poke( arg  );
		return Turn.Continues;
	}

	private Turn evaluate( Expression arg ) {
		if (arg == null) return Turn.Continues;
		if (World.Trace) {
			System.out.println( host.tracePrefix() + " > " 
				+ String.format( "%-30.30s", arg.toString() )
				+ "\t" + dataStack.toString( 5 ) );
		}
		Turn result = Turn.Finished;
		if (arg instanceof ExpressionList )    result = evaluate( (ExpressionList)     arg );
		if (arg instanceof Operation )         result = execute((Operation) arg);
		if (arg instanceof Literal )           result = evaluate( (Literal)           arg );
		if (arg instanceof Number )            result = evaluate( (Number)            arg );
		if (arg instanceof RegisterReference ) result = evaluate( host.get( (RegisterReference)arg ));
        if (World.Trace) {
            System.out.println( host.tracePrefix() + " < " 
                + String.format( "%-30.30s", arg.toString() )
                + "\t" + dataStack.toString( 5 ) );
        }
		return result;
	}

	/**
	 * Execute an operator. Any arguments of the operator have already
	 * been evaluated as necessary and pushed onto stack.
	 */
    @SuppressWarnings("null")
    private Turn execute( Operator operator ) {
        Literal right = null;
        Literal left = null;
        if (operator.isBinary()) {
            right = dataStack.pop();
            left = dataStack.peek();
        }
        CallRecord parentCall;
    	switch (operator) {
	    	case While:
	    	    // If we got here then the loop condition was true,
	    	    // so repeat the While expression.
                callStack.peek().repeat();
                break;
            case Push:
                // Any arg is already pushed to stack.
                // Since "Push arg" is equivalent to "arg",
                // Push could be removed from Zoel for simplicity.
                break;
            case Pop:
                dataStack.pop();
                break;
            case Set:
                Literal val2Set = dataStack.pop();
                host.put( dataStack.peek(), val2Set );
                break;
            case Get:
                dataStack.push( host.get( dataStack.pop() ));
                break;
            case IfThen:
                // We only got here if the if-condition was true.
                // TfThen's arg has already been evaluated,
                // so the only thing to do here is skip any subsequent Else.
                parentCall = callStack.peek();
                if (parentCall instanceof ExpressionListCall) {
                    ExpressionListCall parentList = (ExpressionListCall)parentCall;
                    Expression nextExpr = parentList.peek();
                    if (nextExpr != null && nextExpr instanceof Operation) {
                        Operation nextOperation = (Operation)nextExpr;
                        if (nextOperation.op == Operator.Else) {
                            parentList.next();
                        }
                    }
                }
                break;
            // The only differences between Else and Or are:
            // 1) Else can be skipped by a previous IfThen
            // 2) Or converts stack top into boolean.
            case Else:
                break;
            case Or:
                poke( left.isTrue() || right.isTrue() );
                break;
            // The only differences between IfThen and And are:
            // 1) IfThen can skip a subsequent Else
            // 2) And converts stack top into boolean.
            case And:
                poke( left.isTrue() && right.isTrue() );
                break;
            case Equals:
                poke( left.toNumber() == right.toNumber() );
                break;
            case GreaterThan:
                poke( left.toNumber() > right.toNumber() );
                break;
            case LessThan:
                poke( left.toNumber() < right.toNumber() );
                break;
            case Plus:
                poke( left.toNumber() + right.toNumber() );
                break;
            case Minus:
                Point rightP = Point.parse( right.toString() );
                Point leftP  = Point.parse(  left.toString() );
                if (rightP != null && leftP != null) {
                    poke( rightP.range( leftP, host.world() ));
                } else {
                    poke( left.toNumber() - right.toNumber() );
                }
                break;
            case Modulus:
                // prevent divide by zero
                if (right.toNumber() == 0) {
                    poke( new Number(0) );
                } else {
                    poke( left.toNumber() % right.toNumber() );
                }
                break;
            case Times:
                poke( left.toNumber() * right.toNumber() );
                break;
            case DividedBy:
                // prevent divide by zero
                if (right.toNumber() == 0) {
                    poke( new Number(0) );
                } else {
                    poke( left.toNumber() / right.toNumber() );
                }
                break;
            case Not:
                poke( ! dataStack.peek().isTrue() );
                break;
	    	case Random:
	    		double modulus = dataStack.pop().toNumber();
	    		if (modulus % 1 == 0) {
	    		    // Modulus is itself integer, so yield integer
	    		    int intModulus = (int)Math.round( modulus );
	    		    int val = 0;
	    		    // nextInt() fails if arg is negative
	    		    if (intModulus > 0) {
	    		        val = host.random().nextInt( intModulus );
	    		    } else if (intModulus < 0) {
	    		        val = - host.random().nextInt( - intModulus );
	    		    }
	    		    dataStack.push( new Number( val ));
	    		} else {
                    // Argument is real number, so yield real number
	                dataStack.push( new Number( host.random().nextDouble() * modulus ));
	    		}
	    		break;
            case Negate:
                poke( - dataStack.peek().toNumber() );
                break;
            case AbsoluteVal:
                poke( Math.abs( dataStack.peek().toNumber() ));
                break;
            case Print:
                System.out.println( host.tracePrefix() + " says: " + dataStack.pop() );
                break;
            case EndTurn:
                break;
	    	default:
	    		return host.execute( operator, dataStack.peek() );
    	}
        return operator.whetherTurnContinues();
	}

    /**
     * Begin execution of an Operation.
     * If it has no arg, execute its Operator.
     * If it has an arg, push the Operator onto the call stack and
     * evaluate the arg (if necessary).
     */
	private Turn execute( Operation operation ) {
		if (operation.arg == null) {
			evaluate( host.implicitArgOf( operation.op ) );
			return execute( operation.op );
		}
		// Operation has an argument.
		// Some operators can short-circuit arg evaluation.
		switch (operation.op) {
			case IfThen:
			case While:
			case And:
			    // If the dataStack top is false, do not eval arg
				if (! dataStack.peek().isTrue()) {
					// Any subsequent Else will see what IfThen saw
					// on the top of the dataStack and do the
					// right thing -- i.e. fall out of this switch
					// statement and evaluate its arg below.
					// IfThen will only execute() if the if-condition
					// was true. When it execute()s, it knows to skip
					// any subsequent Else statement, because that
					// Else could be confounded by how the stack was
					// changed by evaluation of IfThen's arg.
					return Turn.Continues;
				}
				break;
			case Else:
			case Or:
			    // If the dataStack top is true, do not eval arg
				if (dataStack.peek().isTrue()) return Turn.Continues;
				break;
			default:
			    break;
		}
		// Binary operators need to make room on stack for arg
		if (operation.op.isBinary()) {
		    dataStack.push( new Number( 0 ));
		}
		// Push a reminder to finish this statement after arg is evaluated
		CallRecord call = new OperationCall( operation );
		callStack.push( call );
		return evaluate( operation.arg );
	}

	// Resume and complete execution of an Operation
	private Turn execute( OperationCall operationCall ) {
		if (World.Trace) {
			System.out.println( host.tracePrefix() + " << " 
				+ String.format( "%-30.30s", operationCall.operation.toString() )
				+ "\t" + dataStack.toString( 5 ) );
		}
		// Pop the call stack first because e.g. IfThen wants to manipulate parent.
		callStack.pop();
		return execute( operationCall.operation.op );
	}

    // Evaluate the next expression in an ExpressionList
	private Turn execute( ExpressionListCall expressionListCall ) {
		Expression expression = expressionListCall.next();
		// Defend against empty StatementLists
		if (expression == null) {
			callStack.pop();
			return Turn.Continues;
		}
		Turn result = evaluate( expression );
		// If the StatementList is the top of the stack and it's done,
		if (callStack.peek() == expressionListCall && expressionListCall.peek() == null) {
			// then pop it to save a execute()
			callStack.pop();
		}
		return result;
	}
	
	private Turn step() {
		CallRecord callRecord = callStack.peek();
		if (callRecord == null) return Turn.Exited;
		if (callRecord instanceof ExpressionListCall) return execute( (ExpressionListCall)callRecord );
		if (callRecord instanceof OperationCall) return execute( (OperationCall)callRecord );
		return Turn.Continues;
	}

	/**
	 * Step until turn ends
	 * @return Continues if steps > host.maxStepsPerTurn()
	 *         Finished if step() returned Finished
     *         Exited if the program ended
	 */
	public Turn next() {
		int steps = 0;
		Turn result;
    	while ((result = step()) == Turn.Continues) {
    		steps++;
    		if (steps > host.maxStepsPerTurn()) {
    			return Turn.Continues;
    		}
    	}
    	return result;
	}
	
	// Return the Statement that our program counter points at/before/after
	private Expression executionCursor( String[] cursorOut ) {
		CallRecord call = callStack.peek();
		Expression nextExpr;
		if (call instanceof OperationCall) {
			nextExpr = ((OperationCall)call).peek();
			cursorOut[0] = "======";
			return nextExpr;
		}
		nextExpr = call.peek();
		cursorOut[0] = ">>>>>";
		if (nextExpr != null) return nextExpr;
		ExpressionListCall gCall = (ExpressionListCall)call;
		nextExpr = gCall.peekBack();
		cursorOut[0] = "<<<<<";
		return nextExpr;
	}

	public String toString( String statementSeparator ) {
		String msg = "Stack: ";
		msg += dataStack.toString( 5 ) + "\n";
		String cursor[] = new String[1];
		Expression nextExpr = executionCursor( cursor );
		CallRecord outerMostScope = callStack.peekBottom();
		if (! (outerMostScope instanceof ExpressionListCall)) return msg;
		ExpressionList outerBlock = ((ExpressionListCall)outerMostScope).expressions();
		msg += outerBlock.toString( statementSeparator, nextExpr, cursor[0] ) + "\n";
		return msg;
	}
	
}
