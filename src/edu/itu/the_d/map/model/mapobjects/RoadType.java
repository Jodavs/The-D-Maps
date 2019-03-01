package edu.itu.the_d.map.model.mapobjects;

import java.awt.*;

/**
 * Enum used for determining the drawing characteristics of the road based on
 * the roadtype.
 * <p>
 * Copyright 2016 The-D
 */
public enum RoadType {
	MOTORWAY(100, 2.0f, 26, 130),
	TRUNK(500, 1.8f, 25, 110),
	LARGE(100, 2.0f, 24, 110),
	MEDIUM(1000, 1.8f, 23, 80),
	SMALL(2000, 1.4f, 22, 80),
	MINI(21000, 1.0f, 20, 50),
	RESIDENTIAL(20000, 1.0f, 21, 50),
	SERVICE(23000, 1.0f, 19, 20),
	CYCLEWAY(26000, 0.7f, 18, 50),
	FOOTPATH(26000, 0.62f, 17, 10),
	RAILWAY(12000, 0.4f, 100, -1),
	RIVER(5000, 2.0f, 16, 110),
	UNSPECIFIED(-1, 0, -1, 1);

	// Determines if the inner or outer stroke should be changed
	static boolean changeInnerStroke, changeOuterStroke;

	static float viewZoomLevel;

	float scale;
	int zoomLevel, z_index, defaultSpeed;

	// The basic stroke of the inner and outer part of the road
	BasicStroke innerStroke, outerStroke;

	/**
	 * @param zoomLevel    Shows at which zoomLevel the Road should be drawn
	 * @param scale        Gives a scaling constant
	 * @param z_index      Shows at which layer the Road should be drawn
	 * @param defaultSpeed If no speedtag has been found in the xml data, the Road has a default speed
	 */
	RoadType(int zoomLevel, float scale, int z_index, int defaultSpeed) {
		this.scale = scale;
		this.zoomLevel = zoomLevel;
		this.innerStroke = new BasicStroke(0.000064f * scale, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
		this.outerStroke = new BasicStroke(0.000072f * scale, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
		this.z_index = z_index;
		this.defaultSpeed = defaultSpeed; // Max speed, that is
	}

	/**
	 * Used to determine when to draw different objects at different zoom levels e.g. roads should be drawn on top of a park
	 *
	 * @return int
	 */
	public int getZoomLevel() {
		return zoomLevel;
	}

	/**
	 * Set the zoom level of the RoadType
	 * Used to calculate the scaling factor for the road
	 * <p>
	 * The method generates a number between 1 and 4 based on the zoom level
	 *
	 * @param zoom
	 */
	public static void setZoomLevel(float zoom) {
		viewZoomLevel = Math.max(1, Math.min(72000 / zoom, 4)); // Limit between 1 and 4
		changeInnerStroke = true;
		changeOuterStroke = true;
	}

	/**
	 * Used to determine which objects to draw at which layer e.g. bridges should be drawn above roads
	 *
	 * @return int
	 */
	public int getZIndex() {
		return z_index;
	}

	/**
	 * Gives the default speed of a road if no speedlimit is found in the xml data
	 *
	 * @return int
	 */
	public int getDefaultSpeed() {
		return defaultSpeed;
	}

	/**
	 * Set the basic inner stroke of the road if it's a moterway, trunket, large, medium or small road
	 *
	 * @return BasicStroke
	 */
	public BasicStroke getInnerStroke() {
		if (changeInnerStroke) {
			for (RoadType rt : values()) {
				// Only few types scale dynamically on zoom level.
				switch (rt) {
					case MOTORWAY:
					case TRUNK:
					case LARGE:
					case MEDIUM:
					case SMALL:
						rt.innerStroke = new BasicStroke(0.000064f * rt.scale * viewZoomLevel, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
						break;
				}
			}
			changeInnerStroke = false;
		}
		return innerStroke;
	}

	/**
	 * Set the basic outer stroke of the road if it's a moterway, trunket, large, medium or small road
	 *
	 * @return BasicStroke
	 */
	public BasicStroke getOuterStroke() {
		if (changeOuterStroke) {
			for (RoadType rt : values()) {
				// Only few types scale dynamically on zoom level.
				switch (rt) {
					case MOTORWAY:
					case TRUNK:
					case LARGE:
					case MEDIUM:
					case SMALL:
						rt.outerStroke = new BasicStroke(0.000072f * rt.scale * viewZoomLevel, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
						break;
				}
			}
			changeOuterStroke = false;
		}
		return outerStroke;
	}
}