package edu.itu.the_d.map.datastructures.nongeneric_maps;

/**
 * Lavet af Troels
 */

import java.io.Serializable;

/**
 * Non generic hash-map for mapping longs to Strings
 */
public class NameMap implements Serializable {
    public static final long serialVersionUID = 201602187;

    private int MASK;
    private Node[] tab;

	/**
     * Create new map with the desired capacity. This is really the capacity of the internal array used for storage,
     * the map itself can store an unlimited amount of objects no matter the size of capacity. As with all hash maps,
     * the capacity is always a trade-off between memory usage and node retrieval time.
     * @param capacity
     */
    public NameMap(int capacity) {
        tab = new Node[capacity];
        MASK = tab.length - 1;
    }

	/**
     * Convert a key to an index in the tab array
     * @param key the key to hash
     * @return an index in the tab array
     */
    private int hash(long key) {
        return (Long.hashCode(key) & 0x7fffffff) % MASK;
    }

	/**
     * Put a new entry into the hash map. This method does not check for duplicates (which will then become unreachable).
     * @param key key to insert at
     * @param name value to insert
     */
    public void put(long key, String name) {
        int h = hash(key);
        tab[h] = new Node(key, name, tab[h]);
    }

	/**
     * Get the value associated with the specified key
     * @param key
     * @return the value associated with the specified key
     */
    public String get(long key) {
        for (Node n = tab[hash(key)]; n != null; n = n.next) {
            if (n.key == key) return n.name;
        }
        return null;
    }

    private class Node {
        public static final long serialVersionUID = 20160216;
        Node next;
        long key;
        String name;

        public Node(long _key, String name, Node _next) {
            this.name = name;
            key = _key;
            next = _next;
        }
    }
}
