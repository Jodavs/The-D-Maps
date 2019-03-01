package edu.itu.the_d.map.model.mapobjects;

/**
 * Enum used for determining the drawing characteristics of the road based on the roadtype.
 * <p>
 * Copyright 2016 The-D
 */
public enum RegionType {
	COASTLINE(0, 1),
	WATER(0, 8), LAKE(500, 8),
	PARK(5000, 5), FOREST(100, 3),
	FARMLAND(500, 4),
	GRASS(5000, 3), GRASS_LIGHTER(5000, 4), GRASS_DARKER(5000, 4),
	PARKING(25000, 5),
	ARCADE(40000, 5),
	BEACH(2000, 5),
	FISHING(10000, 5),
	ICE_HOCKEY(40000, 5),
	MARINA(500, 5),
	SCRUB(1000, 5),
	ROCK(0, 5),
	SAND(1000, 5),
	ICE(0, 5),
	RESIDENTIAL(1600, 5),
	BUILDING(40000, 7), UNIVERSITY(25000, 7),
	UNSPECIFIED(-1, -1);

	int zoomLevel, z_index;

	/**
	 * The parameters of the constructor indicates the zoomLevel of the RegionType and the z_index
	 *
	 * @param zoomLevel Shows at which zoomlevel the Region should be drawn
	 * @param z_index   Shows at which layer it should be drawn. Coastline e.g. should be drawn below anything else
	 */
	RegionType(int zoomLevel, int z_index) {
		this.zoomLevel = zoomLevel;
		this.z_index = z_index;
	}

	/**
	 * Used to draw different objects at different zoom levels e.g. small roads
	 *
	 * @return the zoom level
	 */
	public int getZoomLevel() {
		return zoomLevel;
	}

	/**
	 * Used to draw different objects at different layers e.g bridges above roads
	 *
	 * @return the z-index
	 */
	public int getZIndex() {
		return z_index;
	}
}