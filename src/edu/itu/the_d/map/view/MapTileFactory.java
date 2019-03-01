package edu.itu.the_d.map.view;

import edu.itu.the_d.map.model.Model;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * MapTileFactory to produce tiles for the {@link MapView}.
 * <p>
 * Copyright 2016 The-D
 */
public class MapTileFactory {
	private static final BasicStroke outerSketch = new BasicStroke(4);
	private static final BasicStroke innerSketch = new BasicStroke(2);
	private static boolean debug = false;

	public AtomicInteger activeWorkers;
	private int totalNumTilesX, totalNumTilesY;
	private int tileWidth, tileHeight;
	private int numThreads;

	private MapView mapViewRef;
	private ExecutorService executor;
	private List<BufferTile> bufferTileList;
	private MapTileWorker[] workers;
	private Set<Point2D> tileProcessSet;
	private List<BufferTile> zoomList;
	private AffineTransform zoomTransform;

	/**
	 * An overloaded method used to initialise the Factory with default values.
	 */
	public MapTileFactory(MapView mv, Model model) {
		this(5, 5, mv, model); // Default 5x5
	}

	/**
	 * Calculates the total width and height of all tiles, and their individual width and height based on parameters.
	 *
	 * @param numTilesX The number of tiles to divide the width of the {@link MapView} in.
	 * @param numTilesY The number of tiles to divide the height of the {@link MapView} in.
	 * @param mv        A reference to the {@link MapView}.
	 * @param model     A reference to the {@link Model}. Only used by the {@link MapTileWorker} to retrive data from the {@link edu.itu.the_d.map.datastructures.TwoDTree TwoDTree}.
	 * @see MapView
	 * @see MapTileWorker
	 */
	public MapTileFactory(int numTilesX, int numTilesY, MapView mv, Model model) {
		this.totalNumTilesX = numTilesX;
		this.totalNumTilesY = numTilesY;
		this.mapViewRef = mv;

		// Get the number of threads available to the VM. Should be 8 on a decent computer
		// We minus by one as to use all but one to work, and then have the last one to run smoothly for view
		numThreads = Runtime.getRuntime().availableProcessors() - 1;
		// Create a thread pool with size equal to the number of threads described above
		// This executor comes with useful methods, but is mainly used to ensure no more than numThreads threads are active at a time
		executor = Executors.newFixedThreadPool(numThreads);

		// Initialize the workers array with size equal to the number of threads
		workers = new MapTileWorker[numThreads];

		// Calculate the tileWidth. We chose to make it (1.5 * Width)/number of tiles in x
		tileWidth = (mv.w + mv.w / 2) / numTilesX;
		// Same calculation, just with height
		tileHeight = (mv.h + mv.h / 2) / numTilesY;

		// Initialize all the new workers
		for (int i = 0; i < workers.length; i++) {
			workers[i] = new MapTileWorker(model, mv, this);
		}

		// Initialize the bufferTileList that will hold all finished images produced by the workers
		bufferTileList = new ArrayList<>();

		// Initialize the tileProcessSet which will hold all the (x, y) coordinates of tiles JUST as they are added to workers
		// Hence this set holds the (x, y) coordinate of tiles that might not have been returned by the workers yet
		tileProcessSet = new HashSet<>();

		// Initialize an AtomicInteger that will be equal to the number of active workers
		activeWorkers = new AtomicInteger(0);
	}

	/**
	 * Tells whether or not the Factory is in debug mode.
	 *
	 * @return True if in debug mode, otherwise false.
	 */
	public static boolean getDebug() {
		return debug;
	}

	/**
	 * Sets the Factory in debug mode, effectively drawing boundingboxes and priorities of the individual tiles.
	 *
	 * @param b True to set Factory in debug mode, false to turn off debug mode.
	 */
	public static void setDebug(boolean b) {
		debug = b;
	}

	/**
	 * Given a width and height, calculates the new width and height of tiles.
	 *
	 * @param outerW The new total width of all tiles.
	 * @param outerH The new total height of all tiles.
	 */
	public void setNewBoundaries(int outerW, int outerH) {
		tileWidth = outerW / totalNumTilesX;
		tileHeight = outerH / totalNumTilesY;
	}

	/**
	 * Sets the Zoom Transform, used to draw the {@link MapTileFactory#zoomList} whilest repainting new tiles.
	 *
	 * @param zoomTransform The {@link AffineTransform} to use while drawing the {@link MapTileFactory#zoomList}.
	 */
	public void setZoomTransform(AffineTransform zoomTransform) {
		this.zoomTransform = zoomTransform;
	}

	/**
	 * Given some bounds, snaps them to fit the grid that the tiles are drawn by.
	 * The width and height of the bounds remain the same; The bounds only gets translated to snap to the grid.
	 *
	 * @param uBounds Unsnapped bounds.
	 * @return Bounds that have been snapped to the grid.
	 */
	public Bounds snapToGrid(Bounds uBounds) {
		// Calculate the sign of the bounds' x and y. Sets the sign to 1 if positive and -1 if negative.
		int signX = uBounds.x >= 0 ? 1 : -1;
		int signY = uBounds.y >= 0 ? 1 : -1;

		// Calculate the new x and y coordinates
		// 1. The INTEGER(!) division of x/width is equal to the number of tileWidth's in the x direction.
		// 2. Multiplying that number by the tileWidth gives the x coordinate of the corresponding tile.
		// 3. Lastly multiply by -1 if the bounds' x were negative and 1 if positive.
		int x = (Math.abs(uBounds.x) / tileWidth) * tileWidth * signX;
		if (signX == -1)
			x -= tileWidth; // If it's negative, we will have snapped to the right instead, so subtract a tile to even that out.
		// Repeat for y to snap y to the appropriate tileHeight.
		int y = (Math.abs(uBounds.y) / tileHeight) * tileHeight * signY;
		if (signY == -1) y -= tileHeight;

		return new Bounds(x, y, uBounds.width, uBounds.height);
	}


	/**
	 * Trashes all Tiles and stops all workers on their current work. This is used when all tiles should be repainted.
	 *
	 * @param setZoom True if the current tile state should be copied to the {@link MapTileFactory#zoomList}.
	 */
	public synchronized void trashCache(boolean setZoom) {
		// If there's not already a zoomList and we're told to set it
		if (zoomList == null && setZoom) {
			// Copy all current elements from the bufferTileList to the zoomList
			zoomList = new ArrayList<>(bufferTileList);
		}

		// Remove all data known about tiles
		tileProcessSet = new HashSet<>();
		bufferTileList = new ArrayList<>();

		// Stop all workers.
		for (MapTileWorker worker : workers) {
			worker.stopWorking();
		}
	}

	/**
	 * An overloaded method used to call {@link MapTileFactory#produceTilesForBounds(Bounds...)} with the bounds of the {@link MapView}.
	 */
	public void produceTilesForBounds() {
		produceTilesForBounds(mapViewRef.getViewBounds());
	}

	/**
	 * Given a list of boundaries, checks if the corresponding tiles of the bounds are already drawn.
	 * If they are not, then supply the workers with the <i>snapped</i> boundaries, which will then draw the tiles.
	 * Tiles in the centre of the screen are always prioritised by {@link Bounds#priority} and will therefore be drawn first.
	 *
	 * @param rawBoundsList An list containing boundaries to check for intersection with not yet drawn tiles.
	 * @see MapTileFactory#snapToGrid(Bounds)
	 * @see PriorityBlockingQueue
	 */
	public void produceTilesForBounds(Bounds... rawBoundsList) {
		// Creates a priority queue to hold the boundaries that should be given to the workers
		PriorityBlockingQueue<Bounds> tileBounds = new PriorityBlockingQueue<>(1024);

		// Loops through each raw boundary
		for (Bounds rawBounds : rawBoundsList) {
			if (rawBounds == null) continue;

			// Calculate the number of tiles to draw in the x-axis
			int tilesInX = rawBounds.width / tileWidth;
			// If the rawBounds does not divide by the tileWidth (if it gives a decimal), add an extra
			if (rawBounds.width % tileWidth > 0) tilesInX++;

			// Repeat for tiles in the y-axis
			int tilesInY = rawBounds.height / tileHeight;
			if (rawBounds.height % tileHeight > 0) tilesInY++;

			// Calculate the mean value of the number of tiles in x and y
			int midX = tilesInX / 2;
			int midY = tilesInY / 2;

			// Double for loop to loop through all tiles (tilesInX * tilesInY)
			for (int x = 0; x < tilesInX; x++) {
				for (int y = 0; y < tilesInY; y++) {
					// Calculate the draw-priority in the x-axis. See the report for details
					int xPriority = midX - Math.abs(midX - x);
					// Calculate the draw-priority in the y-axis. See the report for details
					int yPriority = midY - Math.abs(midY - y);

					// Get the boundaries corresponding to a single tile in the current (x, y) coordinate
					Bounds unSnappedTile = new Bounds(rawBounds.x + (x * tileWidth), rawBounds.y + (y * tileHeight), tileWidth, tileHeight);
					// Snap the boundaries to the grid using the snapToGrid method
					Bounds snappedTile = snapToGrid(unSnappedTile);
					// Set the draw-priority of the current tile to be the sum of the priority in x- and y-axis
					snappedTile.setPriority(xPriority + yPriority);

					// Create a point by the (x, y) coordinate of the tile
					Point2D.Float checkPoint = new Point2D.Float(snappedTile.x, snappedTile.y);

					// If the point is NOT in the tileProcessSet
					// The tileProcessSet is only used in these two lines to prevent drawing tiles that are already drawn.
					if (!tileProcessSet.contains(checkPoint)) {
						// Add it to the set
						tileProcessSet.add(checkPoint);
						// And add it to the priority queue
						tileBounds.add(snappedTile);
					}
				}
			}
		}

		// If there's nothing in the priority queue, simply return
		if (tileBounds.size() <= 0) return;

		// Go through all the tileBounds and add them equally distributed to the workers
		// "i = (i + 1) % workers.length" is simply equal to "i++", but is also bounded by the amount of workers
		for (int i = 0; !tileBounds.isEmpty(); i = (i + 1) % workers.length) {
			workers[i].supplyWork(executor, tileBounds.poll());
		}
	}

	/**
	 * Called by the {@link MapTileWorker} whenever it is done producing a new {@link BufferedImage}.
	 * The image will then be added to the {@link MapTileFactory#bufferTileList}, and the {@link MapView} will repaint.
	 *
	 * @param image The finished image produced by the {@link MapTileWorker}.
	 * @param posX  The x position to draw the image at on the {@link MapView#tile_transform}.
	 * @param posY  The y position to draw the image at on the {@link MapView#tile_transform}.
	 */
	public synchronized void addFinishedImage(BufferedImage image, int posX, int posY) {
		// Add the image and its coordinates to the bufferTileList
		bufferTileList.add(new BufferTile(image, posX, posY));

		// The last worker to call this will be 1. So if the last tile is done, do not paint the zoomList no more
		if (activeWorkers.get() == 1 && zoomList != null) {
			zoomList = null;
			mapViewRef.zoom_transform = null;
		}

		// Repaint the whole map
		mapViewRef.repaint();
	}

	/**
	 * Draws all the tiles in the {@link MapTileFactory#bufferTileList}.
	 * If there's a zoom in process, draw all the tiles ON TOP of the {@link MapTileFactory#zoomList}.
	 * The {@link MapTileFactory#zoomList} is the old tiles that are either zoomed in or out, and thus by drawing on
	 * top of it will make it look like tiles are simply refreshed.
	 * Below everything is a grid to make it look better when there's nothing yet where a tile is drawing.
	 *
	 * @param g The {@link Graphics2D} to draw on.
	 */
	public synchronized void drawTiles(Graphics2D g) {
		// Draw background sketches so there's something before the tiles are drawn
		for (Point2D p : tileProcessSet) {
			int tileX = (int) p.getX();
			int tileY = (int) p.getY();

			// Draw 5 inner-lines to create a grid
			g.setStroke(innerSketch);
			g.setColor(Color.WHITE);
			for (int r = tileHeight / 5; r < tileHeight; r += tileHeight / 5) {
				g.drawLine(tileX, tileY + r, tileX + tileWidth, tileY + r);
			}
			for (int c = tileWidth / 5; c < tileWidth; c += tileWidth / 5) {
				g.drawLine(tileX + c, tileY, tileX + c, tileY + tileWidth);
			}

			// Outline the grid by a bigger line for each tile
			g.setStroke(outerSketch);
			g.setColor(Color.LIGHT_GRAY);
			g.drawRect(tileX, tileY, tileWidth, tileHeight);
		}

		AffineTransform tileTransform = g.getTransform();
		// If there's a zoomList, then there's a zoom in process.
		// So draw the tiles in the list with the zoomTransform (zoomed correctly in)
		if (zoomList != null) {
			g.setTransform(zoomTransform);
			for (BufferTile bufferTile : zoomList) {
				bufferTile.drawTile(g);
			}
		}

		// Draw the bufferTileList on top of the tileTransform
		g.setTransform(tileTransform);
		// These are the newest tiles: The actual Tile Images one sees when everything is done
		for (BufferTile bufferTile : bufferTileList) {
			bufferTile.drawTile(g);
		}
	}

	/**
	 * Get the current tileWidth.
	 *
	 * @return The width of each individual tile.
	 */
	public int getTileWidth() {
		return tileWidth;
	}

	/**
	 * Get the current tileHeight.
	 *
	 * @return The height of each individual tile.
	 */
	public int getTileHeight() {
		return tileHeight;
	}
}
