package edu.itu.the_d.map.datastructures;

import edu.itu.the_d.map.datastructures.algs4.MinPQ;
import edu.itu.the_d.map.model.mapobjects.MapObject;

import java.awt.geom.Rectangle2D;
import java.io.Serializable;
import java.util.List;
import java.util.Random;

/**
 * <p>
 * TwoDTree is a 2-dimensional kd-tree for storing {@link MapObject}'s. The class is not generic, instead opting for
 * a simplified implementation optimized for this specific application.
 * </p>
 * <p>
 * <p>
 * Each node in the tree is comprised of one integer array of positional information and a list of map objects. The
 * integer array defines a splitting coordinate and an offset coordinate, used for determining which subtree to traverse.
 * (<b>int[] {longitude, latitude, offset longitude, offset latitude}</b>). Instead of storing just a single map object
 * each node stores up to {@link MapObject##CUTOFF_NODE_SIZE} objects, thus decreasing the amount of nodes needed to store
 * a map.
 * </p>
 * <p>
 * <p>
 * <b>Tree construction:</b> The tree is constructed by using the {@link #constructFromList(List, List)} method.
 * Construction is done by using a Quicksort like partition method {@link #partition(List, List, int, int, int)}
 * which takes a depth as an extra parameter. The method picks a pivot and then places every lesser element to the left of this
 * pivot. In contrast with the regular partition method from a normal Quicksort implementation, this method compares either
 * the x-coordinate or the y-coordinate of the input depending on the depth parameter (alternating between the two dimensions).
 * Furthermore the method then compares every lesser objects maximum coordinate with the pivot's , adjusting the pivots offset
 * accordingly. For example, the picture below illustrates a 2d-tree where (A) splits the area along the x-axis. Because
 * line segment (B) has a smaller x-coordinate than (A), it is positioned in (A)'s left subtree. Now, because segment (B)
 * extends beyond the offset of line segment (A), (A)'s offset value on the x-axis is adjusted accordingly (shown by
 * the dotted line).
 * </p>
 * <p>
 * <pre>
 *     |--------|---.----|<br>
 *     |   (B) _|___.    |<br>
 *     |   ___/ |   .    |<br>
 *     |        |   .    |<br>
 *     |     (A)|_/ .    |<br>
 *     |--------|---.----|<br>
 * </pre>
 * <p>
 * <p>
 * After partitioning the list into two sublists, the {@link #constructFromList(List, List)} method recursively splits
 * the list on each pivot index until at certain minimum amount of objects is reached (defined by {@link #CUTOFF_NODE_SIZE}.
 * After the cutoff is reached all map objects in the current sub list is added to a single node, this time adjusting the coordinates
 * as well as the offsets.
 * </p>
 * <p>
 * <p>
 * <b>Range query:</b> To get data from the data structure, one must define a bounding box or range of objects that
 * should be returned. The TwoDTree then checks recursively through itself, adding a node if it intersects the bounding box
 * and checking the left and right sub node if the range of their subdivision of the trees area is within the bounding box (check
 * {@link #getRange(int, int, int, int, int)} for details).
 * </p>
 *
 * Copyright 2016 The-D
 */
public class TwoDTree implements Serializable {
	private static final long serialVersionUID = 111123123;

	// Constants for posinfo arrays
	/**
	 * Offset of longitude in bounding box arrays
	 */
	public static final int LON = 0;
	/**
	 * Offset of latitude in bounding box arrays
	 */
	public static final int LAT = 1;
	/**
	 * Offset of max coordinates in bounding box arrays (MAX+LON for max longitude) (MAX+LAT for max latitude).
	 */
	public static final int MAX = 2;

	// Number of dimensions (always 2 for this tree, but makes it easier to read the code)
	private static final int DIMENSIONS = 2;
	// List size to cut off the recursive generation of subtrees
	private static final int CUTOFF_NODE_SIZE = 1;

	// The root node of the tree
	private Node root;

	// Size of the tree (number of nodes)
	private int size;

	// Random object used for getting pivot indexes
	private static Random random = new Random();

	/**
	 * Constructs a new empty 2d-tree. Used in conjunction with {@link #constructFromList(List, List)}.
	 */
	public TwoDTree() {
	}

	/**
	 * <p>
	 * Constructs the data structure from two input lists, one containing map objects and the other containing the bounding
	 * box for the corresponding map object. The bounding box is represented as an integer array with four values
	 * (<b>int[] {longitude, latitude, offset longitude, offset latitude}</b>).
	 * </p>
	 * <p>
	 * <p>
	 * The method first partitions the lists into two and creates a root note with the map object and position information
	 * at the resulting pivot. Then it calls a private method {@link #constructFromList(Node, List, List, int, int, int)}
	 * which recursively partitions the lists and add's splitting nodes to the tree.
	 * </p>
	 *
	 * @param posinfo_list
	 * @param obj_list
	 * @see TwoDTree
	 */
	public void constructFromList(List<int[]> posinfo_list, List<MapObject> obj_list) {
		// Check if input lists are same size
		if (posinfo_list.size() != obj_list.size())
			throw new IllegalArgumentException("Input lists are not the same size");

		// Save the size of the lists
		size = posinfo_list.size();
		// Partition the lists and get the resulting pivot position. Set lo to 0 and hi to the size of the lists.
		// Start with depth 0
		int p = partition(posinfo_list, obj_list, 0, size, 0);

		// Create a new root node with the objects contained at the pivot position in the lists
		root = new Node(posinfo_list.get(p), obj_list.get(p));
		// Create new empty nodes for the left and right subtree
		root.left = new Node();
		root.right = new Node();
		// Construct the left subtree from all objects less than or equal to the pivot
		constructFromList(root.left, posinfo_list, obj_list, 0, p, 1);
		// Construct the right subtree from all objects strictly greater than the pivot
		constructFromList(root.right, posinfo_list, obj_list, p + 1, size, 1);
	}

	private void constructFromList(Node n, List<int[]> posinfo_list, List<MapObject> obj_list, int lo, int hi, int depth) {
		if (lo >= size || hi <= 0) return;

		// Check if the cutoff size is reached and the rest of the objects should be put in a single node.
		if (hi - lo < CUTOFF_NODE_SIZE) {
			int[] posinfo = posinfo_list.get(lo);
			for (int i = lo; i < hi; i++) {
				int[] cur_posinfo = posinfo_list.get(i);
				// Check if bounds of current object exceed the nodes bounds.
				// Used in range search to check if this nodes objects should be drawn.
				if (cur_posinfo[LON] < posinfo[LON])
					posinfo[LON] = cur_posinfo[LON];
				if (cur_posinfo[LAT] < posinfo[LAT])
					posinfo[LAT] = cur_posinfo[LAT];
				if (cur_posinfo[MAX + LON] > posinfo[MAX + LON])
					posinfo[MAX + LON] = cur_posinfo[MAX + LON]; // Check if lon_offset is greater than minimum
				if (cur_posinfo[MAX + LAT] > posinfo[MAX + LAT])
					posinfo[MAX + LAT] = cur_posinfo[MAX + LAT]; // Check if lat_offset is greater than minimum

				// Add map object to the node's object list
				n.add(obj_list.get(i));
			}
			// Set the node's bounds
			n.posinfo = posinfo;
		} else {
			// Partition the current subrange of the lists by using the partition method
			int p = partition(posinfo_list, obj_list, lo, hi, depth);

			// Set the nodes bounds and object to the pivot object
			n.posinfo = posinfo_list.get(p);
			n.add(obj_list.get(p));

			// Create new empty nodes for left and right subtree
			n.left = new Node();
			n.right = new Node();

			// Construct left subtree from all objects less than the pivot
			constructFromList(n.left, posinfo_list, obj_list, lo, p, depth + 1);
			// Construct right subtree from all objects bigger than the pivot
			constructFromList(n.right, posinfo_list, obj_list, p + 1, hi, depth + 1);
		}
	}

	/**
	 * <p>
	 * Partitions the two input list in two halves. The partitioning depends on the depth parameter, which for even
	 * values result in a partition along the x-axis of the inputs and for odd values result in a partition along
	 * the y-axis of the inputs.
	 * </p>
	 * <p>
	 * <p>
	 * The partitioning itself works by selecting a random pivot and placing it at the end of the list. Then the method
	 * iterates over the list, moving every item smaller than the pivot (in the given dimension) to the beginning of the
	 * list. Also, if the moved item's offset coordinates are bigger than the pivot's, the pivot's offsets are modified
	 * in order to keep track of how far right or below the pivot (depending on direction) nodes in the left subtree
	 * will go.
	 * </p>
	 * <p>
	 * <p>
	 * The method operates on two lists simultaneously, since each object in both belong together. This is done to
	 * eliminate the need for extra classes and method calls.
	 * </p>
	 * <p>
	 * <p>
	 * For more information on partitioning, check literature on Quicksort, which uses a partitioning method almost
	 * identical to this one, and certainly identical in overall principle.
	 * </p>
	 *
	 * @param posinfo_list input list with the map objects bounds
	 * @param obj_list     input list with the map objects themselves
	 * @param lo           the starting index of the sub-range of the lists to partition
	 * @param hi           the ending index of the sub-range of the lists to partition
	 * @param depth        the current node depth, used for determining the dimension to compare against (x for even depths, y for
	 *                     odd)
	 * @return the final position of the pivot element
	 */
	private int partition(List<int[]> posinfo_list, List<MapObject> obj_list, int lo, int hi, int depth) {
		// Get the current dimension
		int dimension = depth % DIMENSIONS;
		//if (hi == 0) hi = 1;

		// Set the pivot index
		int pivot = hi - 1;
		// Find a random pivot element and move it to the end of the list
		// Note: swapInList swaps elements in both input lists
		swapInList(posinfo_list, obj_list, hi - 1, lo+random.nextInt(hi-lo));

		// This variable represents the current index to put values less than the pivot in. It starts at the bottom
		// of the range to partition
		int i = lo;
		// Go through each element in the sub-range defined by lo and hi
		for (int j = lo; j < hi - 1; j++) {
			// Check if the current element is less than or equal to the pivot element
			// Either longitude or latitude (x or y) is chosen as comparison based on the current tree depth
			if (posinfo_list.get(j)[dimension] <= posinfo_list.get(pivot)[dimension]) {
				// Check if the offset of the current element is bigger than the pivot's
				// This is only done to elements in the left subtree, since only these might pass the coordinates of
				// the splitting nodes (see class docs for details)
				if (posinfo_list.get(j)[dimension + MAX] > posinfo_list.get(pivot)[dimension + MAX])
					posinfo_list.get(pivot)[dimension + MAX] = posinfo_list.get(j)[dimension + MAX];
				// Swap the element at position i with the current element, thus building the lesser elements from the beginning
				// of the partitioning range
				swapInList(posinfo_list, obj_list, i, j);
				i++;
			}
		}
		// Finally swap the pivot to its final location determined by i (index of the element after all elements less
		// than the pivot)
		swapInList(posinfo_list, obj_list, i, pivot);
		// Return this index for use in creating subtrees
		return i;
	}

	/**
	 * Simply swaps the elements at position k and i in both input lists.
	 *
	 * @param posinfo_list input list with the map objects bounds
	 * @param obj_list     input list with the map objects themselves
	 * @param k
	 * @param j
	 */
	private void swapInList(List<int[]> posinfo_list, List<MapObject> obj_list, int k, int j) {
		int[] posinfo_tmp = posinfo_list.get(k);
		MapObject obj_tmp = obj_list.get(k);

		posinfo_list.set(k, posinfo_list.get(j));
		obj_list.set(k, obj_list.get(j));
		posinfo_list.set(j, posinfo_tmp);
		obj_list.set(j, obj_tmp);
	}

	/**
	 * Same as {@link #getRange(int, int, int, int, int)} but without zoom parameter
	 * @param minlon minimum longitude of the bounding box
	 * @param minlat minimum latitude of the bounding box
	 * @param maxlon maximum longitude of the bounding box
	 * @param maxlat maximum latitude of the bounding box
	 * @return
	 */
	public MinPQ<MapObject> getRange(int minlon, int minlat, int maxlon, int maxlat) {
		return getRange(minlon, minlat, maxlon, maxlat, Integer.MAX_VALUE);
	}

	/**
	 * Public method for getting a certain range of objects in the tree. The range query itself is run by the private
	 * method {@link #getRange(MinPQ, Node, int[], int[], int, int)}.
	 * @param minlon minimum longitude of the bounding box
	 * @param minlat minimum latitude of the bounding box
	 * @param maxlon maximum longitude of the bounding box
	 * @param maxlat maximum latitude of the bounding box
	 * @param zoomLevel determines which objects to return based on presets in the respective map objects
	 * @return a minimum priority queue of map objects within the requested range
	 *
	 * @see MapObject
	 * @see MinPQ
	 */
	public MinPQ<MapObject> getRange(int minlon, int minlat, int maxlon, int maxlat, int zoomLevel) {
		MinPQ<MapObject> res = new MinPQ<>();
		getRange(res, root, new int[]{minlon, minlat}, new int[]{maxlon, maxlat}, 0, zoomLevel);
		return res;
	}

	/**
	 * Recursively searches the data structure for objects within the range passed to the method. If the current node's
	 * object/objects intersect the range they're added to the priority queue. Then if the minimum coordinate of the range
	 * is less than the maximum offset of the node, the left subtree is searched. Similarly, if the maximum coordinate of
	 * the range is greater than the minimum coordinate of the node, the right subtree is searched. This is done recursively
	 * until no matches are found or the bottom of the tree is reached.
	 * @param res the priority queue to add matching objects to. This is what is later returned to the caller by the public method calling this
	 * @param n the current node to use for comparisons
	 * @param pos_lower the lower coordinate of the range
	 * @param pos_upper the upper coordinate of the range
	 * @param depth the current depth - used for determining the splitting dimension
	 * @param zoomLevel the zoom level to return objects for (determined by each map object)
	 *
	 * @see MapObject
	 */
	private void getRange(MinPQ<MapObject> res, Node n, int[] pos_lower, int[] pos_upper, int depth, int zoomLevel) {
		// If node is null, just return (means bottom of tree has been reached)
		if (n == null) return;

		// Choose dimension based on current depth
		int d = depth % DIMENSIONS;

		// If the two rectangles intersect each other, add the node's objects to the priority queue
		if (pos_lower[LON] <= n.posinfo[MAX+LON] && pos_upper[LON] >= n.posinfo[LON] &&
				pos_lower[LAT] <= n.posinfo[MAX+LAT] && pos_upper[LAT] >= n.posinfo[LAT]) {
			n.appendToPQ(res, zoomLevel);
		}

		// If the lower coordinate of the query range is smaller than the maximum offset of the node, search the left subtree
		if (pos_lower[d] <= n.posinfo[d + MAX]) {
			getRange(res, n.left, pos_lower, pos_upper, depth + 1, zoomLevel);
		}
		// If the upper coordinate of the query range is bigger than the minimum offset of the node, search the right subtree
		if (pos_upper[d] >= n.posinfo[d]) {
			getRange(res, n.right, pos_lower, pos_upper, depth + 1, zoomLevel);
		}
	}

	/**
	 * @return the size of the tree
	 */
	public int size() {
		return size;
	}

	/**
	 * Class for storing nodes in the tree. Each node contains a bounds array, specifying the nodes splitting coordinates
	 * and offsets, and an array of map objects.
	 */
	private class Node implements Serializable {
		private static final long serialVersionUID = 111135;

		/**
		 * position information of the node, specified like so:
		 * int[] {longitude, latitude, offset longitude, offset latitude}
		 */
		int[] posinfo;

		/**
		 * Array of map objects contained in the node.
		 */
		MapObject[] obj_list;
		/**
		 * References to left and right subtree
		 */
		Node left, right;
		/**
		 * Number of elements in object list.
		 */
		private int N;

		/**
		 * Create new empty node
		 */
		public Node() {
			posinfo = new int[4];
			obj_list = new MapObject[1];
		}

		/**
		 * Create new node with position information and a single map object
		 * @param posinfo
		 * @param obj
		 */
		public Node(int[] posinfo, MapObject obj) {
			this.posinfo = posinfo;
			obj_list = new MapObject[]{obj};
			N = 1;
		}

		/**
		 * Private method for resizing the map object array.
		 * @param max
		 */
		private void resize(int max) {
			// Create new temporary array
			MapObject[] tmp = new MapObject[max];
			// Go through each element in the object array and copy its reference to the temporary one
			for (int i = 0; i < N; i++) {
				tmp[i] = obj_list[i];
			}
			// Set the node's object array to the temporary array
			obj_list = tmp;
		}

		/**
		 * Adding map objects to the list works like the add method of an array list.
		 * @param obj the object to add
		 */
		public void add(MapObject obj) {
			// Resize internal object array (to 2N) if it's capacity has been reached
			if (N == obj_list.length) resize(N * 2);
			// Add map object to the list
			obj_list[N++] = obj;
		}

		/**
		 * Add all map objects in the node to the specified priority queue. This is used by the 2d-tree's range search
		 * @param pq priority queue to add objects to.
		 * @param zoomLevel used for deciding which objects to draw at a certain level of zoom
		 */
		public void appendToPQ(MinPQ<MapObject> pq, int zoomLevel) {
			for (int i = 0; i < N; i++) {
				// If the object's zoom level is less than the specified minimum, add it to the priority queue
				if (obj_list[i].getZoomLevel() <= zoomLevel) pq.insert(obj_list[i]);
			}
		}
	}
}