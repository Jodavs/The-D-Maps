package edu.itu.the_d.map.model;

import edu.itu.the_d.map.dataprocessing.Dispatcher;
import edu.itu.the_d.map.datastructures.*;
import edu.itu.the_d.map.datastructures.algs4.MinPQ;
import edu.itu.the_d.map.datastructures.nongeneric_maps.IdMap;
import edu.itu.the_d.map.model.mapobjects.AddressSearcher;
import edu.itu.the_d.map.model.mapobjects.MapObject;
import edu.itu.the_d.map.model.mapobjects.Road;
import edu.itu.the_d.map.utils.*;
import edu.itu.the_d.map.view.View;

import java.awt.*;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Observable;

/**
 * <p>
 * Model of all the data contained in the map. This class is used extensively by all classes accessing any persistent
 * data related to the map. This includes the paths and shapes for drawing, the address objects for searching, the graph
 * for route finding, and a slew of other secondary data structures supporting these.
 * </p>
 * <p>
 * Copyright 2016 The-D
 */
public class Model extends Observable implements Serializable {
	public final static long serialVersionUID = 12342112;

	// Pin images
	public static BufferedImage pin_to_image = ImageLoader.loadImage("pin_to.png", 56, false);
	public static BufferedImage pin_from_image = ImageLoader.loadImage("pin_from.png", 56, false);
	public static BufferedImage pin_nn_image = ImageLoader.loadImage("pin.png", 56, false);

	// Location of temporary pins
	public Point2D.Float pinLocation_nn = new Point2D.Float(0, 0);
	public Point2D.Float pinLocation_from = new Point2D.Float(0, 0);
	public Point2D.Float pinLocation_to = new Point2D.Float(0, 0);

	// Geo location reference
	public GeoLocation geoLocation;

	// Pin offsets
	public int pinXOffset = 19;
	public int pinYOffset = 50;

	// Correction for the latitude of the loaded map
	public float lonfactor;

	// Address searcher object
	public AddressSearcher addressSearcher;

	// List of coastlines are held in a separate data structure because of their size
	public List<Pair<int[], MapObject>> coastlines;

	// Map of all the nodes
	public IdMap objectMap;

	// Font reference for drawing text on the map itself
	public Font primaryFont;

	// List of user added pins
	public ArrayList<Pin> pinList = new ArrayList<>();

	// Bounding coordinates of the map
	float minlon, minlat, maxlon, maxlat;

	// 2d-tree with all the map objects (except coastlines)
	private TwoDTree data;
	// Graph for finding paths between addresses and nodes
	private WayUndirectedGraph graph;

	// Path of current route shown on the map
	private Path2D.Float routePath;

	/**
	 * Creates empty model
	 */
	public Model() {
		data = new TwoDTree(); // Init a new KdTree with 2 dimensions (longitude, latitude)
		addressSearcher = new AddressSearcher();
		geoLocation = new GeoLocation();
		coastlines = new ArrayList<>();
		objectMap = new IdMap(50);
	}

	/**
	 * Constructs a model by loading data from the file specified by the parameter.
	 * Supported file formats are ".osm.xml", ".zip", and ".obj".
	 *
	 * @param filename a (relative) path to the file
	 */
	public static Model createModel(String filename) {
		// Turn on the loading view
		LoadingView.turnOn();

		// Load default obj file if no path is given
		if (filename == null) {
			LoadingView.setInfoMsg("Constructing Map directly from default .obj file...");
			Model m = load(ResourceLoader.load("resources/default.osm.obj"));
			LoadingView.turnOff();
			return m;
		}
		// If file is an .obj file, load it directly to the model
		if (filename.endsWith(".obj")) {
			LoadingView.setInfoMsg("Constructing Map directly from .obj file...");
			try {
				return load(new FileInputStream(filename));
			} catch (FileNotFoundException e) {
				throw new RuntimeException(e);
			}
		}

		// Else create a new model from the specified file
		Model model = new Model();
		try {
			// Open font for drawing text on the map
			InputStream file = ResourceLoader.load("resources/font.ttf");
			model.primaryFont = Font.createFont(Font.TRUETYPE_FONT, file).deriveFont(16f);
			// Add the font to the graphics env. to make it accessible everywhere
			GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
			ge.registerFont(Font.createFont(Font.TRUETYPE_FONT, file));
		} catch (IOException | FontFormatException e) {
			e.printStackTrace();
		}
		LoadingView.setInfoMsg("Initializing parsing environment...");

		// File object for getting size information
		File sizeGet = new File(filename);
		// Size of file
		long fileSize = sizeGet.length();
		// Multiplying by compression ratio found in some test osm files
		long fileSizeZip = (long) (fileSize * 13.7f);
		// Hand crafted ratio from file size to number of tags
		long nodeCount = (long) (fileSize / 234f / 4f);
		long nodeCountZip = (long) (fileSizeZip / 234f / 4f);

		// Branch to set different size values if the file is a zip file
		if (filename.endsWith(".zip")) {
			model.objectMap = new IdMap((int) nodeCountZip);
			Debugger.print(User.AESK, "IDMAP SIZE: " + nodeCountZip);
			System.out.println("File size in bytes: " + fileSizeZip);
			Dispatcher.constructModelFromOSMFile(filename, fileSizeZip, model);
		} else {
			model.objectMap = new IdMap((int) nodeCount);
			Debugger.print(User.AESK, "IDMAP SIZE: " + nodeCount);
			System.out.println("File size in bytes: " + fileSize);
			Dispatcher.constructModelFromOSMFile(filename, fileSize, model);
		}
		LoadingView.turnOff();
		return model;
	}

	/**
	 * Method for loading a serialized .obj file.
	 *
	 * @param inputStream input stream to load (must be a .obj file)
	 * @return a model with data loaded directly from the binary file (skips dataprocessing)
	 */
	private static Model load(InputStream inputStream) {
		try {
			ObjectInputStream in = new ObjectInputStream(new BufferedInputStream(inputStream));
			return (Model) in.readObject();
		} catch (IOException | ClassNotFoundException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Change the pin image depending on the current color-theme.
	 *
	 * @param pin integer that is either 0, 1, or 2 depending on the theme
	 */
	public void setPinImage(int pin) {
		if (pin == 0) {
			pinXOffset = 19;
			pinYOffset = 50;
			pin_to_image = ImageLoader.loadImage("pin_to.png", 56, false);
			pin_from_image = ImageLoader.loadImage("pin_from.png", 56, false);
			pin_nn_image = ImageLoader.loadImage("pin.png", 56, false);
		} else if (pin == 1) {
			pinXOffset = 66 / 2;
			pinYOffset = 23;
			pin_nn_image = ImageLoader.loadImage("pin_batman.png", 66, false);
			pin_from_image = ImageLoader.loadImage("pin_batman.png", 66, false);
			pin_to_image = ImageLoader.loadImage("pin_batman.png", 66, false);
		} else if (pin == 2) {
			pinXOffset = 56 / 2;
			pinYOffset = 34;
			pin_nn_image = ImageLoader.loadImage("pin_nyan.png", 56, false);
			pin_from_image = ImageLoader.loadImage("pin_nyan.png", 56, false);
			pin_to_image = ImageLoader.loadImage("pin_nyan.png", 56, false);
		} else {
			throw new IllegalArgumentException("Illegal value for pin: " + pin + " - can only be (0, 1, or 2)");
		}
	}

	/**
	 * Get the minimum longitude of the map.
	 *
	 * @return the minimum longitude of the map
	 */
	public float getMinlon() {
		return minlon;
	}

	/**
	 * Set the minimum longitude of the map.
	 *
	 * @param minlon the new minimum longitude
	 */
	public void setMinlon(float minlon) {
		this.minlon = minlon;
	}

	/**
	 * Get the minimum latitude of the map.
	 *
	 * @return the minimum latitude of the map
	 */
	public float getMinlat() {
		return minlat;
	}

	/**
	 * Set the minimum latitude of the map.
	 *
	 * @param minlat the new minimum latitude
	 */
	public void setMinlat(float minlat) {
		this.minlat = minlat;
	}

	/**
	 * Get the maximum longitude of the map.
	 *
	 * @return the maximum longitude of the map
	 */
	public float getMaxlon() {
		return maxlon;
	}

	/**
	 * Set the maximum longitude of the map.
	 *
	 * @param maxlon the new maximum longitude
	 */
	public void setMaxlon(float maxlon) {
		this.maxlon = maxlon;
	}

	/**
	 * Get the maximum latitude of the map.
	 *
	 * @return the maximum latitude of the map
	 */
	public float getMaxlat() {
		return maxlat;
	}

	/**
	 * Set the maximum latitude of the map.
	 *
	 * @param maxlat the new maximum latitude
	 */
	public void setMaxlat(float maxlat) {
		this.maxlat = maxlat;
	}

	/**
	 * Get the graph representing the map
	 *
	 * @return the graph
	 */
	public WayUndirectedGraph getGraph() {
		return graph;
	}

	/**
	 * Set the graph to a new value
	 *
	 * @param graph the reference to the new graph
	 */
	public void setGraph(WayUndirectedGraph graph) {
		this.graph = graph;
	}

	/**
	 * Get the path of the current route
	 *
	 * @return the route as a path object
	 */
	public Path2D.Float getRoutePath() {
		return routePath;
	}

	/**
	 * Set the path of the current route
	 *
	 * @param routePath the route as a path object
	 */
	private void setRoutePath(Path2D.Float routePath) {
		this.routePath = routePath;
	}

	/**
	 * Notify observers that the model has changed.
	 */
	public void dirty() {
		setChanged();
		notifyObservers();
	}

	/**
	 * @param fromID The position of the start node
	 * @param toID   The position of the target node
	 * @return {@link List list of strings} which shows the route list
	 */
	public List<String> generateDijkstra(long fromID, long toID) {

		// A list of the route list
		List<String> routeList = new ArrayList<>();
		List<Double> distList = new ArrayList<>();
		distList.add(0d);

		// Generates Dijkstra given the graph, the source, the target point and model as parameters
		Dijkstra dijkstra = new Dijkstra(graph, graph.getIndex(fromID), graph.getIndex(toID), this);

		// The path represented as a Path2D.Float
		Path2D.Float path = new Path2D.Float();

		// If it doesn't have a path to the target point, it throws a NoPathFoundException
		if (!dijkstra.hasPathTo(graph.getIndex(toID))) {
			throw new NoPathFoundException(String.format("No path to from %d to %d.", fromID, toID));
		}

		// A boolean used to move to the first edge
		boolean first = true;

        /* An integer that represents the last vertex at the starting position of the edge
         * Set lastFromV to the ID from the starting position of the edge
         */
		int lastFromV = graph.getIndex(fromID);

		// Index to the current road to add distances to
		int currentDistIndex = 0;

		// Holds the combined distance between nodes on the current road
		double currentDist = 0;

		// Run through all the edges
		for (WayEdge e : dijkstra.pathTo(graph.getIndex(toID))) {
			// If it's the first edge, initializes from to the lastFromV and moves to its location
			if (first) {
				Point2D from = objectMap.get(graph.getID(lastFromV));
				pinLocation_from.setLocation(from);
				path.moveTo(from.getX(), from.getY());
				first = false;
			}

			// Add edge length to current road distance
			currentDist += e.length;

			// The name of the vertex at the start of the edge
			String fromName = null;

            /* Try to set fromName to name of the road of lastFromV, which is an integer that represents
             * the last vertex at the starting position of the edge
             */

			try {
				fromName = objectMap.getRoad(graph.getID(lastFromV)).getName();
			} catch (NullPointerException ex) {
			}

			// The other end of the edge e, stored in an integer toV
			int toV = e.other(lastFromV);

			// The name of the vertex at the end position of the edge
			String toName = null;

			// Try to get the name of the road at the end position of the edge
			try {
				toName = objectMap.getRoad(graph.getID(toV)).getName();
			} catch (NullPointerException ex) {
			}

            /* If the name of the vertices at the starting and end positions are the same
             * and the route list doesn't already contain the name of the end position:
             * Add the name to the route list
             */
			if (fromName != null && fromName.equals(toName) && !routeList.contains(toName)) {
				routeList.add(toName);
				// Because we start a new road, set the current distance to the first edge's length
				currentDist = e.length;
				// Just add a new entry to the distance list
				distList.add(0d);
				// increment the dist index
				currentDistIndex++;
			}
			// Set the current distance index to whatever value currentDist holds (the accumulated length of edges on the current road)
			distList.set(currentDistIndex, currentDist);

			// Store the Point2D at the end position of the edge
			Point2D to = objectMap.get(graph.getID(toV));
			pinLocation_to.setLocation(to);

			// Line to the position of the Point2D at the end position of the edge
			path.lineTo(to.getX(), to.getY());

			// Set the vertex at the end position of the edge to start starting position before going through the next edge
			lastFromV = toV;
		}

		// Set the route path
		setRoutePath(path);

		// Loop that constructs the route strings with road names and distances
		for (int i = 1; i < distList.size(); i++) {
			// Get the length of the current road (segment really)
			double d = distList.get(i);
			String distString;
			// Format either as meters or kilometers depending on size
			if (d > 1000) {
				distString = String.format("%.1f km", d / 1000);
			} else {
				distString = String.format("%d m", (int) d);
			}
			// Modify existing string in route list
			routeList.set(i - 1, "Kør " + distString + " ad " + routeList.get(i - 1));
		}

		// Return the list of the names of the path
		return routeList;
	}

	/**
	 * Set current route to null
	 */
	public void clearRoutePath() {
		setRoutePath(null);
	}

	/**
	 * Get a reference to the model's 2d-tree
	 *
	 * @return
	 */
	public TwoDTree getTreeRef() {
		return data;
	}

	/**
	 * Get all data within the map bounds from the 2d-tree
	 *
	 * @return a priority queue with all map data
	 * @see MinPQ
	 * @see TwoDTree
	 * @see MapObject
	 */
	public synchronized MinPQ<MapObject> getAllData() {
		return getDataInRange(minlon, minlat, maxlon, maxlat, Integer.MAX_VALUE);
	}

	/**
	 * Get data within the range specified by the two point parameters and below the specified zoom level.
	 *
	 * @param lower     lower coordinate of range
	 * @param upper     upper coordinate of range
	 * @param zoomLevel zoom level to return objects for
	 * @return a priority queue with map objects in the specified range
	 * @see MinPQ
	 * @see TwoDTree
	 * @see MapObject
	 */
	public synchronized MinPQ<MapObject> getDataInRange(Point2D lower, Point2D upper, int zoomLevel) {
		return getDataInRange(lower.getX(), lower.getY(), upper.getX(), upper.getY(), zoomLevel);
	}

	/**
	 * Get data within the range specified by the two point parameters and below the specified zoom level.
	 *
	 * @param minlon the minimum bounding longitude
	 * @param minlat the maximum bounding latitude
	 * @param maxlon the minimum bounding longitude
	 * @param maxlat the maximum bounding latitude
	 * @return a priority queue with map objects in the specified range
	 * @see MinPQ
	 * @see TwoDTree
	 * @see MapObject
	 */
	private synchronized MinPQ<MapObject> getDataInRange(double minlon, double minlat, double maxlon, double maxlat, int zoomLevel) {
		// Convert map coordinates to tree coordinates
		int int_minlon = (int) (minlon * Math.pow(10, 7));
		int int_minlat = (int) -(maxlat * Math.pow(10, 7));
		int int_maxlon = (int) (maxlon * Math.pow(10, 7));
		int int_maxlat = (int) -(minlat * Math.pow(10, 7));

		// Get data from the 2d-tree
		MinPQ<MapObject> res = data.getRange(int_minlon, int_minlat, int_maxlon, int_maxlat, zoomLevel);
		// Also add coastlines
		// These are not contained in the 2d-tree since many of them are extremely large and thus would make the structure less effective
		for (Pair<int[], MapObject> segment : coastlines) {
			int[] posinfo = segment.valA;
			if (posinfo[TwoDTree.LON] <= int_maxlon || posinfo[TwoDTree.LON + TwoDTree.MAX] >= int_minlon
					|| posinfo[TwoDTree.LAT] <= int_maxlat || posinfo[TwoDTree.LAT + TwoDTree.MAX] >= int_minlat) {
				res.insert(segment.valB);
			}
		}

		return res;
	}

	/**
	 * Computes the dot product AB x AC, used by the nearestNeighbor method
	 * to find line distance.
	 *
	 * @param pointA of type double[]
	 * @param pointB of tyoe double[]
	 * @param pointC of type double[]
	 * @return the dot product
	 */
	private double dotProduct(double[] pointA, double[] pointB, double[] pointC) {
		double[] AB = new double[2];
		double[] BC = new double[2];
		AB[0] = pointB[0] - pointA[0];
		AB[1] = pointB[1] - pointA[1];
		BC[0] = pointC[0] - pointB[0];
		BC[1] = pointC[1] - pointB[1];
		double dot = AB[0] * BC[0] + AB[1] * BC[1];

		return dot;
	}

	/**
	 * Computes the cross product AB x AC, used by the nearestNeighbor method
	 * to find line distance.
	 *
	 * @param pointA of type double[]
	 * @param pointB of type double[]
	 * @param pointC of type double[]
	 * @return the cross product.
	 */
	private double crossProduct(double[] pointA, double[] pointB, double[] pointC) {
		double[] AB = new double[2];
		double[] AC = new double[2];
		AB[0] = pointB[0] - pointA[0];
		AB[1] = pointB[1] - pointA[1];
		AC[0] = pointC[0] - pointA[0];
		AC[1] = pointC[1] - pointA[1];
		double cross = AB[0] * AC[1] - AB[1] * AC[0];

		return cross;
	}

	/**
	 * Computes distance from A to B
	 *
	 * @param pointA of type double[]
	 * @param pointB of type double[]
	 * @return distance
	 */
	private double distance(double[] pointA, double[] pointB) {
		double d1 = pointA[0] - pointB[0];
		double d2 = pointA[1] - pointB[1];

		return Math.sqrt(d1 * d1 + d2 * d2);
	}

	/**
	 * Computes the distance from AB to C.
	 * If isSegment is true, AB is a segment, not line.
	 *
	 * @param pointA    of type double[]
	 * @param pointB    of type double[]
	 * @param pointC    of type double[]
	 * @param isSegment of type boolean
	 * @return distance
	 */
	private double linetoP(double[] pointA, double[] pointB, double[] pointC, boolean isSegment) {
		double dist = crossProduct(pointA, pointB, pointC) / distance(pointA, pointB);
		if (isSegment) {
			double dot1 = dotProduct(pointA, pointB, pointC);
			if (dot1 > 0) {
				return Double.MAX_VALUE; //distance(pointB, pointC);
			}
			double dot2 = dotProduct(pointB, pointA, pointC);
			if (dot2 > 0) {
				return Double.MAX_VALUE; //distance(pointA, pointC);
			}
		}
		return Math.abs(dist);
	}

	/**
	 * Simplified nearest neighbor without vehicle type.
	 *
	 * @param p a point clicked
	 * @return nearest ID of node in a Road.
	 * @see #nearestNeighbor(Point2D, VehicleType)
	 */
	public Pin nearestNeighbor(Point2D p) {
		return nearestNeighbor(p, null);
	}

	/**
	 * Finds the nearest MapObject to a point p.
	 *
	 * @param p a point clicked
	 * @return nearest ID of node in a Road.
	 */
	public Pin nearestNeighbor(Point2D p, VehicleType vehicle) {
		double x = p.getX();
		double y = p.getY();

		/**
		 * .000357 coordinate constant that approximately equals 500 meters.
		 * We use this constant to get all map data within a range of 500 meters
		 * from the specified point..
		 */
		int searchMinLon = (int) ((x - .005000 * lonfactor) * Math.pow(10, 7));
		int searchMaxLon = (int) ((x + .005000 * lonfactor) * Math.pow(10, 7));
		int searchMaxLat = (int) -((y - .005000) * Math.pow(10, 7));
		int searchMinLat = (int) -((y + .005000) * Math.pow(10, 7));


		double[] pointC = new double[]{x, y};

		// Get all objects in a range around the search point
		MinPQ<MapObject> closeObjects = data.getRange(searchMinLon, searchMinLat, searchMaxLon, searchMaxLat, Integer.MAX_VALUE);

		// Initialize variables for later use
		long nearestID = 0;
		double nearestDist = Integer.MAX_VALUE;
		String name = null;
		long prevRef = 0;

		// Iterate through each object in the resulting list, updating the best candidate along the way
		for (MapObject o : closeObjects) {
			// Only checks roads
			if (o instanceof Road) {
				Road r = (Road) o;

				boolean first = true;
				double pointAx, pointAy, pointBx, pointBy;
				double[] pointA, pointB;
				pointA = null;

				// Iterate through all references of the current road
				for (long ref : r.getRefs()) {
					// Exclude this if the type of the road is not allowed for this vehicle type
					if (vehicle != null && !VehicleType.isAllowedType(vehicle, objectMap.getRoad(ref).getType()))
						continue;

					if (first) { // Special case for the first object
						pointAx = objectMap.get(ref).getX();
						pointAy = objectMap.get(ref).getY();
						pointA = new double[]{pointAx, pointAy};
						first = false;
					} else {
		                /*
                        finds the shortest distance from a point C, to
                        the line AB, and saves the name of that road.
                         */
						pointBx = objectMap.get(ref).getX();
						pointBy = objectMap.get(ref).getY();
						pointB = new double[]{pointBx, pointBy};
						double ldist = linetoP(pointA, pointB, pointC, true);
						// Update the best candidate if the distance is smaller than the current best
						if (ldist < nearestDist) {
							nearestDist = ldist;
							double distA = distance(pointA, pointC);
							double distB = distance(pointB, pointC);
							name = r.getName();
							if (distB < distA) {
								nearestID = ref;
							} else {
								nearestID = prevRef;
							}
						}
						pointA = pointB;
					}
					/*
                    finds the nearest road to where the mouse is clicked.
                     */
					double dist = objectMap.get(ref).distance(p);

					// Update best candidate if necessary
					if (dist < nearestDist) {
						nearestDist = dist;
						nearestID = ref;
						name = r.getName();
					}

					prevRef = ref;
				}
			}
		}

		if (nearestID == 0) return null;
		Point2D obj = objectMap.get(nearestID);
		return new Pin(name, obj, nearestID);
	}


	/**
	 * Saves the current Model object state to the file specified by the parameter.
	 *
	 * @param filename the path and filename to save to
	 */
	public void save(File file, String filename) {
		try (ObjectOutputStream out = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(file + "/" + filename)))) {
			out.writeObject(this);
		} catch (IOException e) {
			View.infoBox("ERROR", "File not saved");
			throw new RuntimeException(e);
		}
	}
}
