package edu.itu.the_d.map.model.mapobjects;

import java.awt.*;
import java.io.Serializable;
import java.util.EnumMap;
import java.util.Map;

/**
 * ColorTheme to keep track of which colors to draw {@link MapObject MapObjects}. Uses the Singleton pattern.
 * <p>
 * Copyright 2016 The-D
 */
public final class ColorTheme implements Serializable {
	public static final long serialVersionUID = 5838;
	public static int themeCode = 0;
	private static transient ColorTheme instance = new ColorTheme();
	private Map<RoadType, ColorObject> roadMap;
	private Map<RegionType, ColorObject> regionMap;

	private ColorTheme() {
		roadMap = new EnumMap<>(RoadType.class);
		regionMap = new EnumMap<>(RegionType.class);
	}

	/**
	 * Sets the ColorTheme to default
	 */
	public static void setDefaultTheme() {
		themeCode = 0;
		// Roads
		setColor(RoadType.MOTORWAY, new Color(0xeec779), new Color(0xB7995D));
		setColor(RoadType.TRUNK, new Color(0xEEA76D), new Color(0xB7995D));
		setColor(RoadType.LARGE, new Color(0xeec779), new Color(0xB7995D));
		setColor(RoadType.MEDIUM, new Color(0xf6e79e), new Color(0xB1A56C));
		setColor(RoadType.SMALL, new Color(0xb5b5b5), new Color(0x595959));
		setColor(RoadType.MINI, new Color(0xb5b5b5), new Color(0xA3966E));
		setColor(RoadType.RESIDENTIAL, Color.WHITE, Color.DARK_GRAY);
		setColor(RoadType.SERVICE, Color.WHITE, Color.DARK_GRAY);
		setColor(RoadType.FOOTPATH, new Color(0xc1bcaf), new Color(0xD5D4C8));
		setColor(RoadType.CYCLEWAY, new Color(0xc1bcaf), new Color(0xD5D4C8));
		setColor(RoadType.RAILWAY, new Color(0x728995), new Color(0x566C78));
		setColor(RoadType.RIVER, new Color(0xB3D1FF), null);
		setColor(RoadType.UNSPECIFIED, null, null);

		// Regions
		setColor(RegionType.COASTLINE, new Color(0xe9e5dc), null);
		setColor(RegionType.WATER, new Color(0xB3D1FF), null);
		setColor(RegionType.LAKE, new Color(0xB3D1FF), null);
		setColor(RegionType.PARK, new Color(0xCADFAA), null);
		setColor(RegionType.FOREST, new Color(0xCADFAA), null);
		setColor(RegionType.PARKING, null, null);
		setColor(RegionType.BUILDING, new Color(0xB3B2AB), null);
		setColor(RegionType.UNIVERSITY, new Color(0xAAAEBC), null);
		setColor(RegionType.SCRUB, new Color(0xBBDFAF), null);
		setColor(RegionType.GRASS, new Color(0xCADFAA), null);
		setColor(RegionType.GRASS_LIGHTER, new Color(0xD8EDB7), null);
		setColor(RegionType.GRASS_DARKER, new Color(0xB6CB95), null);
		setColor(RegionType.RESIDENTIAL, new Color(0xd8c8a4), null);
		setColor(RegionType.FARMLAND, new Color(0xE9DEC7), null);
		setColor(RegionType.UNSPECIFIED, null, null);
	}

	public static void setBatmanTheme() {
		themeCode = 2;
		// Roads
		setColor(RoadType.MOTORWAY, new Color(0xdd7d7d), new Color(0xdd7d7d));
		setColor(RoadType.TRUNK, new Color(0xE68A5C), null);
		setColor(RoadType.LARGE, new Color(0xeec779), null);
		setColor(RoadType.MEDIUM, new Color(0xf6e79e), null);
		setColor(RoadType.SMALL, new Color(0x9c9c9c), null);
		setColor(RoadType.MINI, new Color(0xCBBE9E), new Color(0xA3966E));
		setColor(RoadType.RESIDENTIAL, new Color(0xb5b5b5), null);
		setColor(RoadType.SERVICE, new Color(0xb5b5b5), null);
		setColor(RoadType.FOOTPATH, new Color(0xb5b5b5), null);
		setColor(RoadType.CYCLEWAY, new Color(0xb5b5b5), null);
		setColor(RoadType.RAILWAY, new Color(0x728995), new Color(0x566C78));
		setColor(RoadType.UNSPECIFIED, null, null);

		// Regions
		setColor(RegionType.COASTLINE, new Color(0x5a5a5a), null);
		setColor(RegionType.WATER, new Color(0x222222), null);
		setColor(RegionType.LAKE, new Color(0x222222), null);
		setColor(RegionType.PARK, new Color(0x5a5a5a), null);
		setColor(RegionType.FOREST, new Color(0x617461), null);
		setColor(RegionType.PARKING, new Color(0x868686), null);
		setColor(RegionType.BUILDING, new Color(0x2e2e2e), null);
		setColor(RegionType.UNIVERSITY, new Color(0x2e2e2e), null);
		setColor(RegionType.SCRUB, new Color(0x566356), null);
		setColor(RegionType.GRASS, new Color(0x517851), null);
		setColor(RegionType.GRASS_LIGHTER, new Color(0x517851), null);
		setColor(RegionType.GRASS_DARKER, new Color(0x313b31), null);
		setColor(RegionType.RESIDENTIAL, new Color(0x4c4c4c), null);
		setColor(RegionType.FARMLAND, new Color(0x676767), null);
		setColor(RegionType.UNSPECIFIED, null, null);
	}


	public static void setNyanTheme() {
		themeCode = 1;
		// Roads
		setColor(RoadType.MOTORWAY, new Color(0xfe0000), null);
		setColor(RoadType.TRUNK, new Color(0xfe0000), null);
		setColor(RoadType.LARGE, new Color(0xff9900), null);
		setColor(RoadType.MEDIUM, new Color(0xfeff00), null);
		setColor(RoadType.SMALL, new Color(0x33ff00), null);
		setColor(RoadType.MINI, new Color(0x0099ff), null);
		setColor(RoadType.RESIDENTIAL, new Color(0x0099ff), null);
		setColor(RoadType.SERVICE, new Color(0x0099ff), null);
		setColor(RoadType.FOOTPATH, new Color(0x0099ff), null);
		setColor(RoadType.CYCLEWAY, new Color(0x0071C6), null);
		setColor(RoadType.RAILWAY, new Color(0x004FC6), null);
		setColor(RoadType.UNSPECIFIED, null, null);

		// Regions
		setColor(RegionType.COASTLINE, new Color(0xff99ff), null);
		setColor(RegionType.WATER, new Color(0xffcb99), null);
		setColor(RegionType.LAKE, new Color(0xff3296), null);
		setColor(RegionType.PARK, new Color(0x0099ff), null);
		setColor(RegionType.FOREST, new Color(0xff3296), null);
		setColor(RegionType.PARKING, new Color(0xffcb99), null);
		setColor(RegionType.BUILDING, new Color(0x6734ff), null);
		setColor(RegionType.UNIVERSITY, new Color(0x999999), null);
		setColor(RegionType.SCRUB, new Color(0xff3296), null);
		setColor(RegionType.GRASS, new Color(0xff3296), null);
		setColor(RegionType.GRASS_LIGHTER, new Color(0xff3296), null);
		setColor(RegionType.GRASS_DARKER, new Color(0xff3296), null);
		setColor(RegionType.RESIDENTIAL, new Color(0xffcb99), null);
		setColor(RegionType.FARMLAND, new Color(0xFFA9FE), null);
		setColor(RegionType.UNSPECIFIED, null, null);
	}

	/**
	 * Maps the given RoadType to the two given colors
	 *
	 * @param type      The RoadType which act as a valA for a map
	 * @param fillColor The Color to fill this type with
	 * @param drawColor The Color to draw this type with
	 */
	public static void setColor(RoadType type, Color fillColor, Color drawColor) {
		instance.roadMap.put(type, new ColorObject(fillColor, drawColor));
	}

	/**
	 * Maps the given RegionType to the two given colors
	 *
	 * @param type      The RegionType which act as a valA for a map
	 * @param fillColor The Color to fill this type with
	 * @param drawColor The Color to draw this type with
	 */
	public static void setColor(RegionType type, Color fillColor, Color drawColor) {
		instance.regionMap.put(type, new ColorObject(fillColor, drawColor));
	}

	/**
	 * @param type The RoadType to get the colors for
	 * @return A {@link ColorObject ColorObject} with the type's associated Colors
	 */
	public static ColorObject getColorObject(RoadType type) {
		return instance.roadMap.get(type);
	}

	/**
	 * @param type The RegionType to get the colors for
	 * @return A {@link ColorObject ColorObject} with the type's associated Colors
	 */
	public static ColorObject getColorObject(RegionType type) {
		return instance.regionMap.get(type);
	}
}
