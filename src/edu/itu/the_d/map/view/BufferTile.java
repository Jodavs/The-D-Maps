package edu.itu.the_d.map.view;

import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * A class to simply hold a {@link BufferedImage} and the position it should be drawn at by the {@link MapTileFactory}.
 * This is the format that the images produced by the {@link MapTileWorker MapTileWorkers} is stored in.
 * Copyright 2016 The-D
 */
public class BufferTile {
	public final BufferedImage image;
	public final int x, y;

	/**
	 * Creates a new BufferTile with the given {@link BufferedImage} and coordinates.
	 *
	 * @param image A {@link BufferedImage}.
	 * @param x     The x-coordinate to draw the image on.
	 * @param y     The y-coordinate to draw the image on.
	 */
	public BufferTile(BufferedImage image, int x, int y) {
		this.image = image;
		this.x = x;
		this.y = y;
	}

	/**
	 * Draw the {@link BufferTile#image} on the (x, y) coordinates of this BufferTile.
	 *
	 * @param g The {@link Graphics} to draw the image on.
	 */
	public void drawTile(Graphics2D g) {
		g.drawImage(image, x, y, null);
	}
}