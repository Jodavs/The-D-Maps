package edu.itu.the_d.map.datastructures;

import edu.itu.the_d.map.datastructures.algs4.IndexMinPQ;
import edu.itu.the_d.map.datastructures.algs4.Stack;
import edu.itu.the_d.map.model.Model;

import java.awt.geom.Point2D;

/**
 * A class to calculate shortest path routes for the specific {@link WayUndirectedGraph}.
 * Base Dijkstra algorithm is taken from: <i>Algorithms, 4th Edition</i> by Robert Sedgewick and Kevin Wayne.
 * <p>
 * Copyright 2016 The-D
 */
public class Dijkstra {
    public static final int EUCLID = 0, FASTEST = 1;
    private static int weightType = EUCLID; // Default to shortest
    private static VehicleType vt = VehicleType.CAR;
    private double[] distTo;          // distTo[v] = distance  of shortest s->v path
    private WayEdge[] edgeTo;            // edgeTo[v] = last edge on shortest s->v path
    private IndexMinPQ<Double> pq;    // priority queue of vertices
    private WayUndirectedGraph graph;
    private int p;
    private Model m;
    private Point2D targetPoint;

    /**
     * Computes a shortest-paths tree from the source vertex <tt>s</tt> to every
     * other vertex in the edge-weighted graph <tt>G</tt>.
     *
     * @param G the edge-weighted digraph
     * @param s the source vertex
     * @param p the target vertex
     * @throws IllegalArgumentException if an edge weight is negative
     * @throws IllegalArgumentException unless 0 &le; <tt>s</tt> &le; <tt>V</tt> - 1
     */
    public Dijkstra(WayUndirectedGraph G, int s, int p, Model m) {
        // For Bicyle and Walk only use shortest distance
        if (vt == VehicleType.BICYCLE || vt == VehicleType.WALK) weightType = EUCLID;

        this.m = m;
        this.graph = G;
        this.p = p;
        targetPoint = m.objectMap.get(graph.getID(p)); /* 0 */

        distTo = new double[G.V()];
        edgeTo = new WayEdge[G.V()];
        for (int v = 0; v < G.V(); v++) /* 3 */
            distTo[v] = Double.POSITIVE_INFINITY;
        distTo[s] = 0.0;

        // Relax vertices in order of distance from s
        pq = new IndexMinPQ<>(G.V());
        pq.insert(s, distTo[s]);
        while (!pq.isEmpty()) { /* 4 */
            int v = pq.delMin();
            for (WayEdge e : G.adj(v)) { /* 5 */
                relax(e, v);
                // Stop when target vertex is found
                if (e.other(v) == p) return; /* 6 */
            }
        }
    }

    /**
     * Set the type of the vehicle, which determines the allowed {@link WayEdge#dirFlags}
     * @param vt VehicleType
     */
    public static void setVehicleType(VehicleType vt) {
        Dijkstra.vt = vt;
    }

    /**
     * Return the type of vehicle, which determines the allowed {@link WayEdge#dirFlags}
     * @return vt VehicleType
     */
    public static VehicleType getVehicleType() {
        return vt;
    }


    /**
     * Set the weight of the edge to either EUCLID (shortest) or fastest
     * @param type
     */
    public static void setWeightType(int type) {
        if (type != EUCLID && type != FASTEST)
            throw new IllegalArgumentException("Invalid weighttype: " + type);
        weightType = type;
    }

    // Relax edge e and update pq if changed
    private void relax(WayEdge e, int v) {
        int w = e.other(v);

        // Is the direction we're going along this edge equal to the forward direction of the edge?
        boolean isForward = v == e.fromV;

        // Return if we do not have the priviliges to go this direction with this vehicle type
        if ((isForward && !e.isForwardAllowed(vt)) || (!isForward && !e.isOncomingAllowed(vt))) return;

        // Fastest
        if (weightType == FASTEST) {
            if (distTo[w] > distTo[v] + e.timeWeight) {
                distTo[w] = distTo[v] + e.timeWeight;
                edgeTo[w] = e;

                if (pq.contains(w)) pq.decreaseKey(w, distTo[w]);
                else pq.insert(w, distTo[w]);
            }
        }
        // Shortest
        else {
            if (distTo[w] > distTo[v] - h(v) + e.euclidWeight + h(w)) {
                distTo[w] = distTo[v] - h(v) + e.euclidWeight + h(w);
                edgeTo[w] = e;
                if (pq.contains(w)) pq.decreaseKey(w, distTo[w]);
                else pq.insert(w, distTo[w]);
            }
        }
    }

    private double h(int v) {
        Point2D from = m.objectMap.get(graph.getID(v));
        return from.distance(targetPoint);
    }

    /**
     * Returns the length of a shortest path between the source vertex <tt>s</tt> and
     * vertex <tt>v</tt>.
     *
     * @param v the destination vertex
     * @return the length of a shortest path between the source vertex <tt>s</tt> and
     * the vertex <tt>v</tt>; <tt>Double.POSITIVE_INFINITY</tt> if no such path
     */
    public double distTo(int v) {
        return distTo[v];
    }

    /**
     * Returns true if there is a path between the source vertex <tt>s</tt> and
     * vertex <tt>v</tt>.
     *
     * @param v the destination vertex
     * @return <tt>true</tt> if there is a path between the source vertex
     * <tt>s</tt> to vertex <tt>v</tt>; <tt>false</tt> otherwise
     */
    public boolean hasPathTo(int v) {
        return distTo[v] < Double.POSITIVE_INFINITY;
    }

    /**
     * Returns a shortest path between the source vertex <tt>s</tt> and vertex <tt>v</tt>.
     *
     * @param v the destination vertex
     * @return a shortest path between the source vertex <tt>s</tt> and vertex <tt>v</tt>;
     * <tt>null</tt> if no such path
     */
    public Iterable<WayEdge> pathTo(int v) {
        if (!hasPathTo(v)) return null;
        Stack<WayEdge> path = new Stack<>();
        int x = v;
        for (WayEdge e = edgeTo[v]; e != null; e = edgeTo[x]) {
            path.push(e);
            x = e.other(x);
        }
        return path;
    }
}