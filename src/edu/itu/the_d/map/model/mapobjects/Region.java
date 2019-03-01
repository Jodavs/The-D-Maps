package edu.itu.the_d.map.model.mapobjects;

import java.awt.*;
import java.io.Serializable;

/**
 * Drawable map object
 * <p>
 * Copyright 2016 The-D
 */
public class Region extends MapObject implements Serializable {
	private static final long serialVersionUID = 119;
	private RegionType type; // RegionType

	/**
	 * Constructs an Region object from a specified area type and a z index. The priority
	 * of the resulting object is determined by its type.
	 *
	 * @param lat  latitude of the object
	 * @param lon  longitude of the object
	 * @param type the type of road @see #RoadType
	 */
	public Region(int lat, int lon, RegionType type, Shape path) {
		super(lat, lon, path, type.getZIndex());
		this.type = type;
	}

	/**
	 * @Return RegionType
	 */
	public RegionType getType() {
		return type;
	}

	/**
	 * @return an integer representing the zoomLevel of the RegionType
	 */
	public int getZoomLevel() {
		return type.getZoomLevel();
	}

	/**
	 * Get the color of the RegionType and fills it.
	 * The regions are filled so they are drawn below roads even though the methods are named wrong
	 * The method is synchronized due to multiple threads trying to call it
	 *
	 * @param g the graphics context to draw to - supplied by the caller
	 */
	synchronized public void drawOutline(Graphics2D g) {
		// This is actually the region's fill
		ColorObject colorObj = ColorTheme.getColorObject(type);
		if (colorObj != null && colorObj.getFillColor() != null) {
			g.setColor(colorObj.getFillColor());
			g.fill(path);
		}
	}

	/**
	 * The regions are filled so they are drawn below roads even though the methods are named wrong
	 * The body of the method is therefore empty due to regions not having outlines
	 * <p>
	 * The method is synchronized due to multiple threads trying to call it
	 *
	 * @param g the graphics context to draw to - supplied by the caller
	 */
	synchronized public void drawFill(Graphics2D g) {

	}
}
