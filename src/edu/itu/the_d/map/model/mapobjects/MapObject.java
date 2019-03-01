package edu.itu.the_d.map.model.mapobjects;

import java.awt.*;
import java.awt.geom.Path2D;
import java.io.Serializable;

/**
 * Drawable map object
 * <p>
 * Z-index should be grouped as follows (0 is lowest): 0 - Ocean, 1 - Land, 2-20 Areas, 21-40 Roads.
 * <p>
 * Copyright 2016 The-D
 */
abstract public class MapObject implements Drawable, Comparable<MapObject>, Serializable {
	public static final long serialVersionUID = 5838;

	protected int lat; // Latitude
	protected int lon; // Longitude

	protected int z_index; // Z-index that shows at which layer the map object should be drawn.
	protected Shape path; // The path of the map object

	/**
	 * Used to construct an empty MapObject, in which the variables values are set later
	 */
	public MapObject() {
	}

	/**
	 * Simple constructor for map objects that takes a priority and z-index as parameters.
	 *
	 * @param lat     latitude of the object
	 * @param lon     longitude of the object
	 * @param z_index the z_index of the object
	 * @see Drawable#zIndex() zIndex
	 */
	public MapObject(int lat, int lon, Shape path, int z_index) {
		this.lat = lat;
		this.lon = lon;
		this.path = path;
		this.z_index = z_index;
	}

	/**
	 * Constructor with position
	 *
	 * @param lat latitude of the object
	 * @param lon longitude of the object
	 */
	public MapObject(int lat, int lon, Path2D.Float path) {
		this(lat, lon, path, 0);
	}

	/**
	 * Get the z-index.
	 *
	 * @return an integer specifying the z-index of the map object
	 */
	public int zIndex() {
		return z_index;
	}

	/**
	 * Get the latitude
	 *
	 * @return an integer specifying the latitude of the map object
	 */
	public int getLat() {
		return lat;
	}

	/**
	 * Get the longitude
	 *
	 * @return an integer specifying the longitude of the map object
	 */
	public int getLon() {
		return lon;
	}

	/**
	 * CompareTo method from {@link java.lang.Comparable} interface
	 * Compares which z_index is higher.
	 *
	 * @return -1 If the two map objects' z-index are the same
	 */
	public int compareTo(MapObject other) {
		if (z_index < other.z_index) return -1;
		if (z_index > other.z_index) return 1;
		return 0;
	}

	/**
	 * Used to print out the latitude and longitude
	 *
	 * @return a string with "latitude, longitude"
	 */
	public String toString() {
		return "latitude: " + lat + ", longitude: " + lon;
	}
}
