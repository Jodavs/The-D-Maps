package edu.itu.the_d.map.model.mapobjects;

import java.awt.*;
import java.io.Serializable;
import java.util.List;

/**
 * Drawable map object
 * <p>
 * Copyright 2016 The-D
 */
public class Road extends MapObject implements Serializable {
	private static final long serialVersionUID = 118;

	private String name; // The name of the road
	private List<Long> refs; // A list of all the points, represented by a long, on the road
	private RoadType type; // The RoadType

	/**
	 * Constructs a Road object from a specified road type. The priority and the z_index
	 * of the resulting object is determined by its type.
	 *
	 * @param lat  Latitude of the object
	 * @param lon  Longitude of the object
	 * @param type The type of road @see #RoadType
	 * @param path The path used for drawing the road on screen
	 */
	public Road(int lat, int lon, RoadType type, Shape path, List<Long> refs, String name) {
		super(lat, lon, path, type.getZIndex());
		this.type = type;
		this.refs = refs;
		this.name = name;
	}

	/**
	 * Should be drawn above everything else and therefore has a high z-index
	 */
	public void setBridge() {
		z_index = 1000;
	}

	/**
	 * Returns the name of the road
	 *
	 * @return String
	 */
	public String getName() {
		return name;
	}

	/**
	 * Returns a list of all the points, given by a Long, on the road
	 *
	 * @return List<Long>
	 */
	public List<Long> getRefs() {
		return refs;
	}

	/**
	 * @return RoadType
	 */
	public RoadType getType() {
		return type;
	}

	/**
	 * @return an integer representing the zoomLevel of the RegionType
	 */
	public int getZoomLevel() {
		return type.getZoomLevel();
	}

	/**
	 * Get the color of the RoadType and draws it.
	 * The method is synchronized due to multiple threads trying to call it
	 *
	 * @param g the graphics context to draw to - supplied by the caller
	 */
	synchronized public void drawOutline(Graphics2D g) {
		ColorObject colorObj = ColorTheme.getColorObject(type);
		if (colorObj != null && colorObj.getDrawColor() != null) {
			g.setStroke(type.getOuterStroke());
			g.setColor(colorObj.getDrawColor());
			g.draw(path);
		}
	}

	/**
	 * Get the color of the RoadType and fills it.
	 * The method is synchronized due to multiple threads trying to call it
	 *
	 * @param g the graphics context to draw to - supplied by the caller
	 */
	synchronized public void drawFill(Graphics2D g) {
		ColorObject colorObj = ColorTheme.getColorObject(type);
		if (colorObj != null && colorObj.getFillColor() != null) {
			g.setStroke(type.getInnerStroke());
			g.setColor(colorObj.getFillColor());
			g.draw(path); // Yeah, we acturally draws here instead of filling.
		}
	}

}
