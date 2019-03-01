package edu.itu.the_d.map.model.mapobjects;

import java.awt.*;
import java.io.Serializable;

/**
 * A color object that contains a fill color and a draw color
 * <p>
 * Copyright 2016 The-D
 */
public class ColorObject implements Serializable {
	public static final long serialVersionUID = 5838;
	private Color fillColor, drawColor;

	/**
	 * Contains a ColorObject with a fillColor and a drawColor
	 *
	 * @param fillColor
	 * @param drawColor
	 */
	public ColorObject(Color fillColor, Color drawColor) {
		this.fillColor = fillColor;
		this.drawColor = drawColor;
	}

	/**
	 * Gets the fillColor of the ColorObject, which fills the object
	 *
	 * @return fillColor
	 */
	public Color getFillColor() {
		return fillColor;
	}

	/**
	 * Gets the drawColor of the ColorObject, which outlines the object
	 *
	 * @return drawColor
	 */
	public Color getDrawColor() {
		return drawColor;
	}
}
