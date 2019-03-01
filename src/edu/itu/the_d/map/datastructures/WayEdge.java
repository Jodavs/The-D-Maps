package edu.itu.the_d.map.datastructures;

import java.io.Serializable;

/**
 * An undirected edge that can store multiple weights and one byte to describe the {@link WayEdge#dirFlags}.
 * Is used by the {@link WayUndirectedGraph} and {@link Dijkstra} for path finding.
 */
public class WayEdge implements Serializable {
    public static final long serialVersionUID = 123551;

    public final double timeWeight, euclidWeight;
    public final int fromV, toV;
    public final float length;
    private final byte dirFlags;

    /**
     * Is used in the WayUndirectedGraph instead of the given edges
     *
     * @param fromV        The vertex that has the start position
     * @param toV          The vertex that has the end position
     * @param speedWeight  Informs that the edge is weighted by speed
     * @param euclidWeight Informs that the edge is weighted by distance
     * @param dirFlags     Informs whether a car, bike and a pedestrian is allowed to walk both forward and backwards.
     */
    public WayEdge(int fromV, int toV, float speedWeight, float euclidWeight, float length, byte dirFlags) {
        this.fromV = fromV;
        this.toV = toV;
        this.timeWeight = speedWeight;
        this.euclidWeight = euclidWeight;
        this.length = length;
        this.dirFlags = dirFlags;
    }

    /**
     * Given one Vertex at the end of this WayEdge, return the other end of the WayEdge.
     *
     * @param vertex One end of this WayEdge.
     * @return The other end of this WayEdge.
     */
    public int other(int vertex) {
        if (vertex == fromV) return toV;
        else if (vertex == toV) return fromV;
        else throw new IllegalArgumentException("Illegal endpoint");
    }

    /**
     * Given a {@link VehicleType}, return true of the Vehicle is allowed to travel forward on this Edge.
     * Forward is simply the direction that this edge was drawn in.
     * Read {@link VehicleType} for more information about the {@link WayEdge#dirFlags}.
     *
     * @param v The Vehicle to check with.
     * @return True if the Vehicle is allowed to go forward on this WayEdge. False otherwise.
     */
    public boolean isForwardAllowed(VehicleType v) {
        return (dirFlags & v.getForwardFlag()) != 0;
    }

    /**
     * Given a {@link VehicleType}, return true of the Vehicle is allowed to travel backward on this Edge.
     * Backward is simply the <i>opposite</i> direction that this edge was drawn in.
     * Read {@link VehicleType} for more information about the {@link WayEdge#dirFlags}.
     *
     * @param v The Vehicle to check with.
     * @return True if the Vehicle is allowed to go backwards on this WayEdge. False otherwise.
     */
    public boolean isOncomingAllowed(VehicleType v) {
        return (dirFlags & v.getBackwardFlag()) != 0;
    }
}
