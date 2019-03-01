package edu.itu.the_d.map.datastructures;

import edu.itu.the_d.map.model.mapobjects.RoadType;

/**
 * The enumerates to specify which kind of transportation the dijkstra algorithm should consider when finding a path.
 * <p>
 * <p>
 * A flag is given in decimal, but is only treated binary. Hence the flag sequence of [1, 2, 4, 8, 16, 32, ...]
 * contains only integers that directly represents a bit-place (1 = 0001, 2 = 0010, 4 = 0100, 8 = 1000, ...).
 * </p>
 * <p>
 * <p>
 * EXAMPLE
 * Considering a 6 bit integer, the number 7 in binary will be 000111. This number can represent the FLAGS of a given wayEdge.
 * The forward flag of the bicycle, 4, which in binary is 000100, can then be compared with the FLAGS.
 * This is done by the AND logic gate:
 * Result of AND operation: 7      & 4      = 4
 * 000111 & 000100 = 0001000
 * Hence the flag 4 is said to be within the FLAGS, which in terms give the bicycle permission to travel forward on this wayEdge.
 * </p>
 * <p>
 * Copyright 2016 The-D
 */
public enum VehicleType {
	CAR(1, 2),
	BICYCLE(4, 8),
	WALK(16, 32),
	UNKNOWN(-1, -1);

	int forwardFlag, backwardFlag;

	/**
	 * An enum with two unique flags used to determine if the given Vehicle Type can tavel forwards and/or backwards.
	 *
	 * @param forwardFlag  The flag used for forward direction.
	 * @param backwardFlag The flag used for backward direction.
	 */
	VehicleType(int forwardFlag, int backwardFlag) {
		this.forwardFlag = forwardFlag;
		this.backwardFlag = backwardFlag;
	}

	/**
	 * Given a VehicleType and a RoadType, return true if the VehicleType is allowed on the given Road.
	 *
	 * @param vehicle The {@link VehicleType} to check.
	 * @param road    The {@link RoadType} to check.
	 * @return True of the given {@link VehicleType} is allowed on the given {@link RoadType}.
	 */
	public static boolean isAllowedType(VehicleType vehicle, RoadType road) {
		// Switch through the VehicleType
		switch (vehicle) {
			case CAR:
				// If it's a car, it is not allowed on the following roads
				switch (road) {
					case FOOTPATH:
					case CYCLEWAY:
					case UNSPECIFIED:
						return false;
				}
				// If this is reached, then the Car is allowd on the given road
				return true;
			case BICYCLE:
			case WALK:
				// If we walk or are on a bicycle, just check if we're on a Motorway.
				switch (road) {
					case MOTORWAY:
						return false;
				}
				// If we're not on a motorway, we are allowed on the given road.
				return true;
			default:
				return false;
		}
	}

	/**
	 * Get the forward flag for the Vehicle Type.
	 *
	 * @return The forwardFlag, which represents a bit.
	 */
	public int getForwardFlag() {
		return forwardFlag;
	}

	/**
	 * Get the backward flag for the Vehicle Type.
	 *
	 * @return The backwardFlag, which represents a bit.
	 */
	public int getBackwardFlag() {
		return backwardFlag;
	}
}
