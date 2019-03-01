package edu.itu.the_d.map.datastructures;

import edu.itu.the_d.map.datastructures.algs4.Bag;
import edu.itu.the_d.map.datastructures.nongeneric_maps.IdMap;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Iterator;

/**
 * A modified undirected graph over all ways in the {@link edu.itu.the_d.map.model.mapobjects.Road roads} originally taken from
 * <i>Algorithms, 4th Edition</i> by Robert Sedgewick and Kevin Wayne.
 * <p>
 *
 * @author Robert Sedgewick
 * @author Kevin Wayne
 */
public class WayUndirectedGraph implements Iterable<WayEdge>, Serializable {
	public static final long serialVersionUID = 1212331;

	private final int V;
	private Bag<WayEdge>[] adj;

	private long[] nodeIDs;
	private IdMap indexMap;

	/**
	 * Generate a graph based on an {@link IdMap}. No Vertices will be connected by edges after this operation.
	 *
	 * @param indexMap
	 */
	public WayUndirectedGraph(IdMap indexMap) {
		nodeIDs = indexMap.getAllKeys();
		// Sort the array
		Arrays.sort(nodeIDs);

		if (nodeIDs.length < 0) throw new IllegalArgumentException("Number of vertices must be nonnegative");

		// Maps the Long ID to the index
		this.indexMap = indexMap;
		this.V = nodeIDs.length;

		// Create a bag for each Vertice that will act as it's adjacency list.
		adj = (Bag<WayEdge>[]) new Bag[V];
		for (int v = 0; v < V; v++) {
			adj[v] = new Bag<>();
		}
	}

	/**
	 * @return The number of vertices in the graph.
	 */
	public int V() {
		return V;
	}

	/**
	 * Adds the given edge to the graph. The {@link WayEdge} is undirected, so it will be added to the adjacency list of both Vertices at the end of the {@link WayEdge}.
	 *
	 * @param e The {@link WayEdge} to add to the graph.
	 */
	public void addEdge(WayEdge e) {
		int v = e.fromV; // Get the vertex at the fromV end
		int w = e.toV; // Get the vertex at the toV end
		// Add the edge to both the vertices' adjacency lists.
		adj[v].add(e);
		adj[w].add(e);
	}

	/**
	 * Given a vertex, return all the edges that are connected to that vertex.
	 *
	 * @param v The vertex to get the {@link WayEdge WayEdges} for.
	 * @return An Iterator of {@link WayEdge WayEdges}.
	 */
	public Iterable<WayEdge> adj(int v) {
		return adj[v];
	}

	/**
	 * An iterator that will iterate over all the edges in the graph.
	 * <b>Unused</b>
	 *
	 * @return The iterator that will iterate over all the edges in the graph.
	 */
	public Iterator<WayEdge> iterator() {
		return new Iterator<WayEdge>() {
			int currentVertex = 1;
			Iterator<WayEdge> currentEdgeIterator = adj(0).iterator();

			@Override
			public boolean hasNext() {
				while (!currentEdgeIterator.hasNext() && currentVertex < V)
					currentEdgeIterator = adj(currentVertex++).iterator();
				return currentVertex < V;
			}

			@Override
			public WayEdge next() {
				return currentEdgeIterator.next();
			}
		};
	}

	/**
	 * Given an ID returns the corresponding index in the graph.
	 *
	 * @param ID The ID to find the index for.
	 * @return The Index that corresponds to the ID.
	 */
	public int getIndex(long ID) {
		return indexMap.getGraphVertexIndex(ID);
	}

	/**
	 * Given an index in the graph, return the corresponding ID.
	 *
	 * @param index The index in the graph.
	 * @return The ID that corresponds to the Index.
	 */
	public long getID(int index) {
		return nodeIDs[index];
	}
}