package org.holtz.zoe.zoel;


/**
 * An entry in the <code>CallStack</code> of a <code>Bug</code> storing execution state for a <code>Statement</code>.
 * @author Brian Holtz
 */
public class OperationCall implements CallRecord {
	public Operation operation;
	private boolean argEvaluated = false;
	
	public OperationCall( Operation theOperation ) {
		operation = theOperation;
	}

    public void repeat() {
        argEvaluated = false;
    }
	
	public String toString() {
		return operation.op + (argEvaluated ? "" : "()" );
	}

	@Override
	public Operation peek() {
		return operation;
	}
}
