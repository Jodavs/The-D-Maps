package edu.itu.the_d.map.utils;

import java.awt.geom.Point2D;


public class Haversine {

	/**
	 * Calculates the distance in meters from one point in lon+lat to another point in lon+lat.
	 * Code taken http://www.movable-type.co.uk/scripts/latlong.html and then converted to Java.
	 *
	 * @param from the Point2D object with lon+lat coordinates to calculate distance from
	 * @param to   the Point2D object with lon+lat coordinates to calculate distance to
	 * @return a double indicating the distance from the first point to the other.
	 */
	public static double distanceInMeters(Point2D from, Point2D to) {
		double lat1 = from.getX();
		double lat2 = to.getX();
		double lon1 = from.getY();
		double lon2 = to.getY();

		int R = 6371000; // metres
		double φ1 = Math.toRadians(lat1);
		double φ2 = Math.toRadians(lat2);
		double Δφ = Math.toRadians(lat2 - lat1);
		double Δλ = Math.toRadians((lon2 - lon1));

		double a = Math.sin(Δφ / 2) * Math.sin(Δφ / 2) +
				Math.cos(φ1) * Math.cos(φ2) *
						Math.sin(Δλ / 2) * Math.sin(Δλ / 2);
		double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

		return R * c;
	}

}
