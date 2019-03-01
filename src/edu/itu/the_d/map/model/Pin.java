package edu.itu.the_d.map.model;

import java.awt.geom.Point2D;
import java.io.Serializable;

/**
 * Pin class for creating pins.
 * Copyright 2016 The-D
 */
public class Pin implements Serializable {
	public static final long serialVersionUID = 5838;
	private Point2D location;
	private String name;
	private long id;

	/**
	 * Constructor
	 *
	 * @param name     String
	 * @param location Point2D
	 * @param id       long
	 */
	Pin(String name, Point2D location, long id) {
		this.id = id;
		this.name = name;
		this.location = location;
	}

	/**
	 * Constructor
	 *
	 * @param name     String
	 * @param location Point2D
	 */
	Pin(String name, Point2D location) {
		this.name = name;
		this.location = location;
	}

	/**
	 * Constructor
	 *
	 * @param location Point2D
	 */
	public Pin(Point2D location) {
		this.location = location;
	}

	/**
	 * Get the ID of the pin
	 *
	 * @return long
	 */
	public long getId() {
		return id;
	}

	/**
	 * Set the ID of the pin
	 *
	 * @param id long
	 */
	public void setId(long id) {
		this.id = id;
	}

	/**
	 * Get the location of the pin
	 *
	 * @return Point2D
	 */
	public Point2D getLocation() {
		return location;
	}

	/**
	 * Set location of a pin
	 *
	 * @param location Point2D
	 */
	public void setLocation(Point2D location) {
		this.location = location;
	}

	/**
	 * Get the name of the pin
	 *
	 * @return String
	 */
	public String getName() {
		return name;
	}

	/**
	 * Set name of pin
	 *
	 * @param name String
	 */
	public void setName(String name) {
		this.name = name;
	}
}
