package edu.itu.the_d.map.view;

import edu.itu.the_d.map.datastructures.algs4.MinPQ;
import edu.itu.the_d.map.model.Model;
import edu.itu.the_d.map.model.mapobjects.ColorTheme;
import edu.itu.the_d.map.model.mapobjects.MapObject;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.util.Queue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.PriorityBlockingQueue;

/**
 * A Class used to produce BufferedImages for the {@link MapTileFactory} to draw on the {@link MapView}.
 * <p>
 * Copyright 2016 The-D
 */
public class MapTileWorker implements Runnable {
	private MapTileFactory factory;
	private Model modelRef;
	private MapView mapViewRef;

	private Queue<Bounds> boundsQueue;
	private volatile boolean isWorking;

	/**
	 * Set up MapTileWorker for production. Basically takes references to objects which is needed during the work.
	 *
	 * @param modelRef   A reference to the {@link Model}.
	 * @param mapViewRef A reference to the {@link MapView}.
	 * @param factory    A reference to the {@link MapTileFactory} to which this Worker belongs.
	 */
	public MapTileWorker(Model modelRef, MapView mapViewRef, MapTileFactory factory) {
		this.modelRef = modelRef;
		this.mapViewRef = mapViewRef;
		this.factory = factory;

		// Initialize the priority queue of this worker
		boundsQueue = new PriorityBlockingQueue<>(512);
	}

	/**
	 * Given some new bounds, adds them to the {@link MapTileWorker#boundsQueue queue} of this worker.
	 * If this worker is not already working, then start a new thread for it.
	 *
	 * @param executor A reference to the {@link MapTileFactory#executor} that keeps track of the Workers' threads.
	 * @param bounds   The bounds to supply this worker with.
	 */
	public void supplyWork(ExecutorService executor, Bounds bounds) {
		// Add the given bounds to the queue
		boundsQueue.add(bounds);
		// Start a new thread for this worker if it's not working
		if (!isWorking) {
			isWorking = true;
			executor.execute(this);
		}
	}

	/**
	 * Clear the {@link MapTileWorker#boundsQueue queue} of the worker, effectively stopping its thread.
	 */
	public void stopWorking() {
		boundsQueue.clear();
	}

	/**
	 * The threads run method which produces one {@link BufferedImage} per item in it's {@link MapTileWorker#boundsQueue queue}.
	 */
	@Override
	public void run() {
		// Increment the number of active workers by one (this worker is now active)
		factory.activeWorkers.addAndGet(1);
		//isWorking = true;
		Bounds bounds;
		// Get the next bounds from the queue
		while ((bounds = boundsQueue.poll()) != null) {
			// Create a new image in with tile dimensions
			// Note: A new image is produced PER boundary which might be very costy when there's many images. This is cleaned by the Garbage Collector whenever we zoom though.
			BufferedImage image = new BufferedImage(factory.getTileWidth(), factory.getTileHeight(), BufferedImage.TYPE_INT_RGB);
			// Get the graphics of the image to paint on
			Graphics2D g2d = image.createGraphics();

			// Get the actual map data from the KdTree.
			// The Map Transform is used to receive the data.
			MinPQ<MapObject> mapObjects = modelRef.getDataInRange(
					mapViewRef.inverse(bounds.x, bounds.y), // x, y
					mapViewRef.inverse(bounds.x + bounds.width, bounds.y + bounds.height), // x+width, y+height
					(int) mapViewRef.getZoomLevel()); // Zoomlevel

			// Enable antialiasing
			g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

			if (ColorTheme.themeCode == 0) g2d.setColor(new Color(0xB3D1FF));
			else if (ColorTheme.themeCode == 1) g2d.setColor(new Color(0x003466));
			else g2d.setColor(new Color(0x222222));
			// Fill the background
			g2d.fillRect(0, 0, image.getWidth(), image.getHeight());

			// Save the current tileTransform for later use
			AffineTransform tileTransform = g2d.getTransform();

			// Make a copy of the map_transform
			AffineTransform mapTransform = new AffineTransform(mapViewRef.map_transform);

			// Translate the mapTransform to match with the bounds (They're created respective to the tileTransform)
			mapTransform.preConcatenate(AffineTransform.getTranslateInstance(-bounds.x, -bounds.y));
			// Now use this mapTransform
			g2d.setTransform(mapTransform);

			// Draw the outlines for all the data
			for (MapObject mapObject : mapObjects) {
				mapObject.drawOutline(g2d);
			}

			// Then draw the fills for all the data
			for (MapObject mapObject : mapObjects) {
				mapObject.drawFill(g2d);
			}

			// If debug mode is on, outline the image and display the draw-priority (z-index) in the centre
			if (MapTileFactory.getDebug()) {
				g2d.setTransform(tileTransform); // This is what the tileTransform variable was saved for
				g2d.setColor(Color.RED);
				g2d.setStroke(new BasicStroke(4));
				g2d.drawRect(0, 0, factory.getTileWidth(), factory.getTileHeight());

				g2d.drawString("" + bounds.priority, factory.getTileWidth() / 2, factory.getTileHeight() / 2);
			}

			// Dispose the graphics of this image as to release the resources it uses
			// This makes it so that nothing can be drawn to the image anymore, but we're done with it for good anyhow
			g2d.dispose();

			// Finally hand over the produced image to the factory
			factory.addFinishedImage(image, bounds.x, bounds.y);
		}

		// If the queue is empty (and this part of the code is reached), it is no longer working
		isWorking = false;
		// So decrease the number of active workers
		factory.activeWorkers.addAndGet(-1);
	}
}
