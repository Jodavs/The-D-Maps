package edu.itu.the_d.map.model.mapobjects;

import java.awt.*;

/**
 * Interface for drawable map objects
 * <p>
 * Copyright 2016 The-D
 */
public interface Drawable {
	/**
	 * Draw the outline of the object. This is used for drawing outlines of line segments
	 * (eg. {@link Road}) as a two-step process, thus avoiding creating a polygon of the outline.
	 *
	 * @param g the graphics context to draw to - supplied by the caller
	 */
	void drawOutline(Graphics2D g);

	/**
	 * Draw the fill of the object.
	 *
	 * @param g the graphics context to draw to - supplied by the caller
	 */
	void drawFill(Graphics2D g);

	/**
	 * Get the z-index. This is used to determine the order to draw the objects in.
	 *
	 * @return an integer specifying the z-index of the map object
	 */
	int zIndex();

	/**
	 *
	 * The zoomLevel is used to decide when we draw different objects like small roads
	 *
	 * @return the current zoomLevel which is given by the AffineTransform
	 */

	int getZoomLevel();
}