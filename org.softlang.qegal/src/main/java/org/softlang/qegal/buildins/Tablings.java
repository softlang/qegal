package org.softlang.qegal.buildins;

import java.util.LinkedHashMap;
import java.util.WeakHashMap;
import java.util.function.Function;

/**
 * Created by Johannes on 13.10.2017.
 */
public class Tablings {

    public static <K, V> V get(K key, WeakHashMap<K, V> table, Function<K, V> computation) {
        V value = table.remove(key);

        // Re-computation if necessary.
        if (value == null)
            value = computation.apply(key);

        // Append at beginning of table.
        table.put(key, value);

        // Removed oldest table entries. TODO: Check if this is necessary in case of to much content. Maybe track reuse.
//        if (table.size() > 100)
//            table.remove(table.get(table.size()));

        return value;
    }
}
