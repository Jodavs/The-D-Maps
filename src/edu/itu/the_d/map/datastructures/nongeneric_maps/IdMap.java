package edu.itu.the_d.map.datastructures.nongeneric_maps;

import edu.itu.the_d.map.model.mapobjects.Road;
import edu.itu.the_d.map.utils.Debugger;
import edu.itu.the_d.map.utils.User;

import java.awt.geom.Point2D;
import java.io.Serializable;
import java.util.NoSuchElementException;

/**
 * Lavet af Troels. Er et ikke-generisk hash-map der mapper longs til en række værdier der bruges i forbindelse med
 * nodes.
 */
public class IdMap implements Serializable {
    public static final long serialVersionUID = 201602187;

    private int MASK;
    private Node[] tab;

    private int N = 0;

    public IdMap(int capacity) {
        if (capacity < 2) capacity = 2;
        tab = new Node[capacity];
        MASK = tab.length - 1;
    }

    private int hash(long key) {
        return (Long.hashCode(key) & 0x7fffffff) % MASK;
    }

    public void put(long key, float x, float y) {
        int h = hash(key);
        tab[h] = new Node(key, x, y, N++, tab[h]); // Also adds graph index
    }

    public Point2D get(long key) {
        return (Point2D) getNode(key);
    }

    private Node getNode(long key) {
        for (Node n = tab[hash(key)]; n != null; n = n.next) {
            if (n.key == key) return n;
        }
        return null;
    }

    public int getGraphVertexIndex(long key) {
        for (Node n = tab[hash(key)]; n != null; n = n.next) {
            if (n.key == key) return n.graph_vertex_index;
        }
        throw new NoSuchElementException("No such key " + key + " in IdMap");
    }

    public void setRoad(long key, Road road) {
        getNode(key).road = road;
    }

    public Road getRoad(long key) {
        return getNode(key).road;
    }

    public long[] getAllKeys() {
        long[] res = new long[N];
        int res_index = 0;
        int max_depth = 0;
        for (int i=0; i<=MASK; i++) {
            int depth = 0;
            for (Node n = tab[i]; n != null; n = n.next) {
                depth++;
                res[res_index++] = n.key;
            }
            if (depth > max_depth) max_depth = depth;
        }

        Debugger.print(User.AESK, "idmapdepth: " + max_depth);
        //System.out.println(res.length);

        return res;
    }

    private class Node extends Point2D.Float {
        public static final long serialVersionUID = 20160216;
        Node next;
        long key;
        int graph_vertex_index;
        Road road;

        Node(long _key, float x, float y, int id, Node _next) {
            super(x, y);
            key = _key;
            graph_vertex_index = id;
            next = _next;
        }
    }
}
