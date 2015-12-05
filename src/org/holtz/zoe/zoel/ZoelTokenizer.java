package org.holtz.zoe.zoel;

import java.io.Reader;
import java.io.StreamTokenizer;

/**
 * A tokenizer for the programming language encoded in the <code>Genotype</code> of a <code>Bug</code>.
 *
 * @see Operator
 * @see Register
 */
public class ZoelTokenizer extends StreamTokenizer {
    public final static char StatementTerminator = ',';

    public ZoelTokenizer( Reader text ) {
        super( text );
        parseNumbers();
        slashStarComments( true );
        slashSlashComments( true );
    }

}
