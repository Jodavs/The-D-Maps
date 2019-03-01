package edu.itu.the_d.map.view;

/**
 * A inexpensive class used to contain integer boundaries
 * <p>
 * Copyright 2016 The-D
 */
public class Bounds implements Comparable<Bounds> {
	// Public fields. They're final, so they cannot be changed once set in the constructor
	// And there's not needed any getters, hence making it less expensive.
	public final int x, y, width, height;

	// Not all bounds use this parameter, but it is used to determine which bounds that should be drawn first (by tiles)
	public int priority;

	/**
	 * Create a new boundary from the given x, y, width and height.
	 *
	 * @param x      The x coordinate.
	 * @param y      The y coodrinate.
	 * @param width  The width of the boundary.
	 * @param height The height of the boundary. Downwards is positive height.
	 */
	public Bounds(int x, int y, int width, int height) {
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
	}


	/**
	 * Print the boundaries in a readable String.
	 *
	 * @return The boundaries as a readable String.
	 */
	@Override
	public String toString() {
		return "[x=" + x + ", y=" + y + " | w=" + width + ", h=" + height + "]";
	}

	/**
	 * Set the priority of these boundaries. Used to determine the order in which the {@link MapTileFactory} will draw them.
	 * High priority means drawn first. Low priority means drawn last.
	 *
	 * @param priority The priority of the boundary. A high priority is first, a low priority is last.
	 * @see MapTileFactory
	 */
	public void setPriority(int priority) {
		this.priority = priority;
	}

	/**
	 * Compare the boundary by their priority. <b>Do note that this compareTo is REVERSED</b>: It will return -1 if this object
	 * is smaller and 1 if the other object is bigger. This is because it is used on a minimum priority queue, but higher priorities should be drawn first.
	 *
	 * @param other The object to compare with.
	 * @return 1 if this object has LOWER priority than the other and -1 if this object has LARGER priority than the other. 0 if they have equal priority.
	 * @see java.util.concurrent.PriorityBlockingQueue
	 */
	@Override
	public int compareTo(Bounds other) {
		// Yes this should be opposite, but it's used to take the HIGHEST priority, using a MINIMUM priority queue. (Therefore it's opposite)
		if (priority < other.priority) return 1; // We're smaller, so return 1 (We will be drawn AFTER other)
		if (priority > other.priority) return -1; // We're higher, so return -1 (We will be drawn BEFORE other)
		return 0; // Equal
	}
}
