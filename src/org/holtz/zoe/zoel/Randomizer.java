package org.holtz.zoe.zoel;

import java.util.Random;

/**
 * A class that can return a random element of an enumerated type.
 * @author Brian Holtz
 */
public class Randomizer {
    
    public static <E extends Enum<E>> E next(Random random, Class<E> clazz) {
        E[] values = clazz.getEnumConstants();
        return values[random.nextInt( values.length )];
    }

}
