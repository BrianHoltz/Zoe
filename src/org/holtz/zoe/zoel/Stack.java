package org.holtz.zoe.zoel;

import java.util.ArrayList;
/**
 * A stack that can forget its deepest elements and can have a default element when empty.
 * @author Brian Holtz
 */
public class Stack<T> extends ArrayList<T> {
	
	public T val2PopWhenEmpty = null;

	private static final long serialVersionUID = 201109301716L;

	public void push(T o) {
        add(o);
    }

	public void push(T o, int maxSize) {
        push(o);
		while (size() > maxSize) {
			remove( 0 );
		}
    }

    public T pop() {
    	if (empty()) return val2PopWhenEmpty;
        return remove(size() - 1);
    }

    public boolean empty() {
        return size() == 0;
    }

    public T peek() {
    	if (empty()) return val2PopWhenEmpty;
        return get(size() - 1);
    }

    public T peekBottom() {
    	if (empty()) {
    		if (val2PopWhenEmpty != null) return val2PopWhenEmpty;
    		return null;
    	}
        return get( 0 );
    }
    
    public void poke(T o) {
    	if (empty()) {
    		add( o );
    	} else {
    		pop();
    		add( o );
    	}
    }
    
    public String toString( int n ) {
    	if (size() <= n) return toString();
    	String msg = size() + ": [...";
    	while (n > 0) {
    		msg += " " + get( size() - n ).toString();
    		n--;
    	}
    	return msg + "]";
    }

}
