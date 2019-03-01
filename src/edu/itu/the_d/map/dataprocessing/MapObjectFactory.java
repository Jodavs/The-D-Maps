package edu.itu.the_d.map.dataprocessing;

import edu.itu.the_d.map.datastructures.*;
import edu.itu.the_d.map.datastructures.nongeneric_maps.NameMap;
import edu.itu.the_d.map.datastructures.nongeneric_maps.RelationWayMap;
import edu.itu.the_d.map.model.Model;
import edu.itu.the_d.map.model.mapobjects.*;
import edu.itu.the_d.map.utils.Debugger;
import edu.itu.the_d.map.utils.Haversine;
import edu.itu.the_d.map.utils.LoadingView;
import edu.itu.the_d.map.utils.User;
import edu.itu.the_d.map.view.SuggestionType;

import java.awt.*;
import java.awt.geom.Path2D;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.util.*;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * <p>
 * Creates {@link MapObject}s from an input of {@link OSMObject}s. The convertion is based on the type of osm object
 * (node, way, etc.) and the type of object it represents on the map (coastline, road, etc.).
 * </p>
 *
 * Copyright 2016 The-D
 */
public class MapObjectFactory implements Runnable {
    private HashMap<String, Address> l;
    private BlockingQueue<OSMObject> inputQueue;
    private BlockingQueue<Pair<int[], MapObject>> objOutputQueue;
    private boolean boundsParsed, nodesParsed, waysParsed;
    private RelationWayMap relationWayMap;
    private AtomicBoolean endOfInput;
    private AtomicBoolean endOfOutput;
    private long fsize;
    private Model modelRef;

    private static final int NAME_MAP_CAPACITY = 100000;

    /**
     * Creates a new map object factory with references to a number of different queues and signals, and a reference to
     * the model object.
     *
     * @param inputQueue     a queue of {@link OSMObject} which the factory reads from
     * @param objOutputQueue the queue which holds the output of the object factory
     * @param endOfInput     boolean signalling that the parser is done
     * @param endOfOutput    boolean signalling that the {@link MapObjectFactory} is done
     * @param modelRef       reference to a model object. Used to set map bounds and add coastlines
     */
    public MapObjectFactory(BlockingQueue<OSMObject> inputQueue, BlockingQueue<Pair<int[], MapObject>> objOutputQueue, AtomicBoolean endOfInput, AtomicBoolean endOfOutput, Model modelRef, long fsize) {
        this.inputQueue = inputQueue;
        this.objOutputQueue = objOutputQueue;
        this.endOfInput = endOfInput;
        this.endOfOutput = endOfOutput;
        this.modelRef = modelRef;

        this.fsize = fsize;

        relationWayMap = new RelationWayMap((int) (fsize/234f/100f));
    }

    /**
     * This is the main method of this class and works by reading all objects coming from the {@link OSMParser} and converting
     * them to {@link MapObject}s and also setting map bounds on the model and adds coastlines to the model. Read code comments for implementation details.
     */
    public void run() {
        Thread.currentThread().setName("MapObjectFactory-Thread");

        List<CoastlineObject> coastlines = new ArrayList<>();

        // Variables used for debugging by outputting number of received objects
        long count = 0;

        NameMap nameMap = new NameMap(NAME_MAP_CAPACITY);

        // Loop runs until the endOfInput signal is received and the inputQueue is empty
        while (!endOfInput.get() || !inputQueue.isEmpty()) {
            try {
                // Poll the inputQueue for an object, waiting a maximum of 10 milliseconds in order to ensure the program
                // doesn't hang.
                OSMObject osmObj = inputQueue.poll(10, TimeUnit.MILLISECONDS);
                // If the poll failed, continue from beginning of loop
                if (osmObj == null) continue;

                // Debugging: test number of received osmobjects
                count++;
                if (count % 10000 == 0) {
                    LoadingView.setLoadPercentage((((double) count) / ((double) fsize / 1000000f * 4273f)) * 100f);
                }

                // gennemgå tags
                addToSearchList(osmObj);


                // Switch based on the type of OSMObject (Node, Bounds, Way, Relation)
                caseSwitch: switch (osmObj.getType()) {
                    case BOUNDS:
                        // Sets the models map bounds to the bounds information from the osm file
                        float floatLonMin = Float.parseFloat(osmObj.getTag("minlon"));
                        float floatLonMax = Float.parseFloat(osmObj.getTag("maxlon"));
                        float floatLatMin = Float.parseFloat(osmObj.getTag("minlat"));
                        float floatLatMax = Float.parseFloat(osmObj.getTag("maxlat"));
                        // The lonfactor is found by taking the cosine of the average latitude in radians.
                        modelRef.lonfactor = (float) Math.cos(Math.PI / 180 * (floatLatMin + (floatLatMax - floatLatMin) / 2));
                        // Correct projection
                        modelRef.setMinlon(floatLonMin * modelRef.lonfactor);
                        modelRef.setMaxlon(floatLonMax * modelRef.lonfactor);
                        // Adapt to swings coordinate system (need to invert y-axis)
                        modelRef.setMinlat(-floatLatMin);
                        modelRef.setMaxlat(-floatLatMax);
                        break;

                    case NODE:
                        if (!boundsParsed) {
                            LoadingView.setInfoMsg("Parsing nodes from .osm file...");
                            boundsParsed = true;
                        }

                        // Modify the longitude of the object to compensate for stretched projection
                        float olon = osmObj.getLon() * modelRef.lonfactor;
                        //osmObj.setLon(osmObj.getLon() * modelRef.lonfactor);
                        // Invert the objects latitude to align the coordinates with the way swings coordinate system works
                        float olat = -osmObj.getLat();
                        //osmObj.setLat(-osmObj.getLat());
                        // Put the updated information into the models node map, with an id along with the nodes coordinates
                        //modelRef.objectMap.put(osmObj.getId(), osmObj.getLon(), osmObj.getLat());
                        modelRef.objectMap.put(osmObj.getId(), olon, olat);

                        // Maps node to the name of the street name.
                        String name;
                        if ((name = osmObj.getTag("name")) != null) nameMap.put(osmObj.getId(), name);
                        break;

                    case WAY:
                        // Nodes has been parsed and we therefore constructs the graph
                        if (!nodesParsed) {
                            LoadingView.setInfoMsg("Parsing ways from .osm file...");
                            modelRef.setGraph(new WayUndirectedGraph(modelRef.objectMap));

                            nodesParsed = true;
                        }

                        // Create a new Path2D for the way object
                        Path2D.Float path = new Path2D.Float();
                        List<Point2D> point_list = new ArrayList<>();
                        // Get all the id references from the osmobject. These id's reference node objects, which can
                        // be found in the models objectMap
                        List<Long> refs = osmObj.getRef();

                        long prevRef = refs.get(0);
                        // Now get the starting coordinates of the path by getting the coordinates from the models objectmap
                        Point2D first = modelRef.objectMap.get(prevRef);

                        // Add ways first point to object-map
                        //modelRef.objectMap.put(osmObj.getId(), (float) first.getX(), (float) first.getY());


                        // Set the ways starting minimum and maximum coordinates (bounds) to the first nodes coordinates
                        float minlon = (float) first.getX();
                        float maxlon = minlon;
                        float minlat = (float) first.getY();
                        float maxlat = minlat;

                        // Move the path's starting point to the first nodes coordinates
                        path.moveTo(first.getX(), first.getY());
                        point_list.add(first);

                        // Get road type
                        RoadType roadType = getRoadType(osmObj.getTag("highway"));

                        boolean skipFirst = true;

                        String wayName = null;
                        // Go through each reference in the way object
                        for (long ref : refs) {
                            if (skipFirst) {
                                skipFirst = false;
                                continue;
                            }
                            // Get the nodes coordinates from the model
                            Point2D o = modelRef.objectMap.get(ref);

                            // Make sure that we only try to get information from valid nodes (throws an exception otherwise)
                            if (o != null) {
                                // Get the nodes coordinates as floats
                                float lon = (float) o.getX();
                                float lat = (float) o.getY();

                                // Add edges to the graph
                                if (osmObj.getTag("highway") != null) {
                                    float euclidDistance = (float) o.distance(modelRef.objectMap.get(prevRef));

                                    // Get speed limit and use default if not set for particular road
                                    int maxSpeed = roadType.getDefaultSpeed();
                                    try {
                                        if (osmObj.getTag("maxspeed") != null)
                                            maxSpeed = Integer.parseInt(osmObj.getTag("maxspeed"));
                                    } catch (NumberFormatException e) {
                                        System.err.println("Maxspeed could not be parsed for: "+ osmObj.getId());
                                    }

                                    // Generate direction flag based on one-way and way type
                                    String oneway = osmObj.getTag("oneway");

                                    int dir;
                                    if (oneway != null) {
                                        switch(oneway) {
                                            case "1":
                                            case "true":
                                                dir = 1;
                                                break;
                                            case "-1":
                                            case "false":
                                                dir = -1;
                                                break;
                                            default:
                                                dir = 0;
                                                break;
                                        }
                                    } else {
                                        dir = 0;
                                    }

                                    byte dirFlags = 0;

                                    // If motorway
                                    if (osmObj.getTag("highway").equals("motorway")) {
                                        if (dir == 0 || dir == 1) {
                                            dirFlags |= VehicleType.CAR.getForwardFlag();
                                        }
                                        if (dir == 0 || dir == 1) {
                                            dirFlags |= VehicleType.CAR.getBackwardFlag();
                                        }
                                    }
                                    // If footway, cycleway or way has a "motorcar=no" tag, exclude cars
                                    else if (osmObj.getTag("highway").equals("footway") || osmObj.getTag("highway").equals("cycleway") || osmObj.getTag("highway").equals("path")
                                            || (osmObj.getTag("motorcar") != null && osmObj.getTag("motorcar").equals("no"))
                                            || (osmObj.getTag("motor_vehicle") != null && osmObj.getTag("motor_vehicle").equals("no"))) {
                                        if (dir == 0 || dir == 1) {
                                            dirFlags |= VehicleType.WALK.getForwardFlag();
                                            dirFlags |= VehicleType.BICYCLE.getForwardFlag();
                                        }
                                        if (dir == 0 || dir == -1) {
                                            dirFlags |= VehicleType.WALK.getBackwardFlag();
                                            dirFlags |= VehicleType.BICYCLE.getBackwardFlag();
                                        }
                                    }
                                    else {
                                        if (dir == 0 || dir == 1) {
                                            dirFlags |= VehicleType.WALK.getForwardFlag();
                                            dirFlags |= VehicleType.BICYCLE.getForwardFlag();
                                            dirFlags |= VehicleType.CAR.getForwardFlag();
                                        }
                                        if (dir == 0 || dir == -1) {
                                            dirFlags |= VehicleType.WALK.getBackwardFlag();
                                            dirFlags |= VehicleType.BICYCLE.getBackwardFlag();
                                            dirFlags |= VehicleType.CAR.getBackwardFlag();
                                        }
                                    }
                                    float dist = (float) Haversine.distanceInMeters(modelRef.objectMap.get(prevRef), o);
                                    modelRef.getGraph().addEdge(new WayEdge(modelRef.getGraph().getIndex(prevRef), modelRef.getGraph().getIndex(ref), euclidDistance/maxSpeed, euclidDistance, dist, dirFlags));

                                    prevRef = ref;
                                }

                                // If the nodes longitude is less than the ways minimum longitude, set the minimum to this
                                // nodes longitude
                                if (lon < minlon) minlon = lon;
                                    // Same with the maximum longitude. If this nodes longitude is greater than the current
                                    // update the current value to this nodes longitude.
                                else if (lon > maxlon) maxlon = lon;
                                // Now exactly the same for the latitude. The only difference here is that the comparison
                                // symbols are reversed. This is because the latitude values are negative
                                if (lat > minlat) minlat = lat; // Reverted < & > because its -
                                else if (lat < maxlat) maxlat = lat;

                                // Finally we make a new line segment to the current nodes coordinates
                                point_list.add(new Point2D.Float(lon, lat));
                                path.lineTo(lon, lat);

                                // Set the name if this node has any
                                if (nameMap.get(ref) != null) wayName = nameMap.get(ref);
                            } else throw new RuntimeException("Null pointer");
                        }

                        PolygonApprox outpath = new PolygonApprox(point_list);


                        // Create a last point with the same coordinates as the last point in the way object
                        Point2D last = modelRef.objectMap.get(refs.get(refs.size() - 1));

                        // Since the kd-tree deals in integers instead of floats we multiply each coordinate by 10^7 because
                        // The Openstreetmap Wiki specifies that each coordinate has exactly 7 decimal places.
                        int objLon = (int) (minlon * Math.pow(10, 7));
                        // Again the latitude is reversed because we don't want to deal with negative values in the kd-tree
                        int objLat = (int) -(minlat * Math.pow(10, 7));
                        int objLonMax = (int) (maxlon * Math.pow(10, 7));
                        int objLatMax = (int) -(maxlat * Math.pow(10, 7));

                        int[] boundaries = new int[]{objLon, objLat, objLonMax, objLatMax};

                        MapObject outObj;

                        // If the current osm object contains a highway tag, we try to find out which kind of road it is
                        // in order to specify the road type in the outputted map object
                        if (osmObj.getTag("highway") != null || osmObj.getTag("railway") != null || osmObj.getTag("waterway") != null) {
                            if (wayName == null) wayName = osmObj.getTag("name");

                            if (osmObj.getTag("waterway") != null) roadType = getRoadType(osmObj.getTag("waterway"));

                            // draw railways but not subways (rails for the metro)
                            if(osmObj.getTag("railway") != null && !osmObj.getTag("railway").equals("subway"))
                                roadType = RoadType.RAILWAY;
                            // Create a new map object of type road
                            outObj = new Road(objLat, objLon, roadType, outpath, refs, wayName);

                            // Sets a boolean to true if it's a bridge due to it having a higher z-index
                            if (osmObj.getTag("bridge") != null) ((Road) outObj).setBridge();
                            // Insert this object along with the bounds information into the output queue for insertion
                            // into the kd-tree by the structurer thread
                            objOutputQueue.add(new Pair<>(
                                    boundaries,
                                    outObj
                            ));

                            for (long ref : refs) {
                                modelRef.objectMap.setRoad(ref, (Road) outObj);
                            }
                        }
                        // Check if it's a railway

                        // Regions
                        else if (osmObj.getTag("natural") != null ||
                                osmObj.getTag("leisure") != null ||
                                osmObj.getTag("building") != null ||
                                osmObj.getTag("landuse") != null ||
                                osmObj.getTag("amenity") != null) {
                            RegionType regionType = RegionType.UNSPECIFIED;

                            if (osmObj.getTag("building") != null) regionType = RegionType.BUILDING;
                            if (osmObj.getTag("landuse") != null) regionType = getRegionType(osmObj.getTag("landuse"));
                            if (osmObj.getTag("leisure") != null) regionType = getRegionType(osmObj.getTag("leisure"));
                            if (osmObj.getTag("amenity") != null) regionType = getRegionType(osmObj.getTag("amenity"));
                            // Check all the different type indicator tags and set the regions type accordingly
                            String typeTag;
                            if ((typeTag = osmObj.getTag("natural")) != null) {
                                regionType = getRegionType(typeTag);
                                if (typeTag.equals("coastline")) {
                                    coastlines.add(
                                            new CoastlineObject(
                                                    new Point2D.Float((float) first.getX(), (float) first.getY()),
                                                    new Point2D.Float((float) last.getX(), (float) last.getY()),
                                                    path, minlon, maxlon, minlat, maxlat)
                                    );
                                    continue; // (former to do) Does this really do anything?? Yes it does my friend
                                }
                            }


                            // Create a new map object, this time of type Region
                            outObj = new Region(objLat, objLon, regionType, outpath);
                            // Insert this object along with the bounds information into the output queue for insertion
                            // into the kd-tree by the structurer thread
                            objOutputQueue.add(new Pair<>(
                                    boundaries,
                                    outObj
                            ));
                        }

                        // Add current way to relationWayMap
                        relationWayMap.put(osmObj.getId(), path, boundaries);

                        break;
                    case RELATION:
                        if (!waysParsed) {
                            LoadingView.setInfoMsg("Parsing relations from .osm file...");
                            waysParsed = true;
                        }

                        Set<Pair<Long, String>> memberSet = osmObj.getMemberSet();

                        Path2D relInnerPath = new Path2D.Float();
                        Path2D relOuterPath = new Path2D.Float();
                        Integer minLon = null;
                        Integer minLat = null;
                        Integer maxLon = null;
                        Integer maxLat = null;


                        for (Pair<Long, String> memberPair : memberSet) {
                            Pair<Path2D.Float, int[]> wayInfo;
                            // If just a single way within this relation is null, skip the entire relation
                            if ((wayInfo = relationWayMap.get(memberPair.valA)) == null) break caseSwitch;

                            Shape curPath = wayInfo.valA;
                            boolean isOuter = memberPair.valB.equals("outer");
                            boolean isInner = memberPair.valB.equals("inner");

                            // Update lower and upper bounds of relation if needed
                            int[] posInfo = wayInfo.valB;

                            if (minLon == null || posInfo[0] < minLon) minLon = posInfo[0];
                            if (minLat == null || posInfo[1] < minLat) minLat = posInfo[1];
                            if (maxLon == null || posInfo[2] > maxLon) maxLon = posInfo[2];
                            if (maxLat == null || posInfo[3] > maxLat) maxLat = posInfo[3];

                            // Add to relation path
                            if (isOuter) relOuterPath.append(curPath, false);
                            else if (isInner) relInnerPath.append(curPath, false);
                        }

                        Path2D.Float relPath = new Path2D.Float();
                        relPath.setWindingRule(Path2D.WIND_EVEN_ODD);
                        relPath.append(relOuterPath, false);
                        relPath.append(relInnerPath, false);

                        // Get tag by checking RegionType
                        RegionType regionType;
                        if (osmObj.getTag("building") != null) regionType = RegionType.BUILDING;
                        else if (osmObj.getTag("landuse") != null) regionType = getRegionType(osmObj.getTag("landuse"));
                        else if (osmObj.getTag("leisure") != null) regionType = getRegionType(osmObj.getTag("leisure"));
                        else if (osmObj.getTag("natural") != null) regionType = getRegionType(osmObj.getTag("natural"));
                        else if (osmObj.getTag("amenity") != null) regionType = getRegionType(osmObj.getTag("amenity"));
                        else if (osmObj.getTag("waterway") != null) regionType = getRegionType(osmObj.getTag("waterway"));
                        else regionType = RegionType.UNSPECIFIED;

                        objOutputQueue.add(new Pair<>(
                                new int[]{minLon, minLat, maxLon, maxLat},
                                new Region(minLat, minLon, regionType, relPath)
                        ));


                        break;

                    default:
                        break;
                }
                // Now we got all the information we wanted from the osmobject and constructed a new mapobject from this information.
                // Therefore we set the osmobj reference to null in order to ensure that it gets deleted by Java's garbage collector
                osmObj = null;

            } catch (InterruptedException e) { // If the queue polling gets interrupted this exception will be thrown
                throw new RuntimeException(e.toString());
            }
        }

        LoadingView.setInfoMsg("Initializing addressSearcher...");

        //AddressObject preperation
        modelRef.addressSearcher.initialize();

        LoadingView.setInfoMsg("Merging Coastlines...");
        for (CoastlineObject coast : merge(coastlines)) {
            // Multiply coordinates by 10^7 to convert to integer
            int minLon = (int) (Math.pow(10, 7) * coast.minlon);
            int minLat = (int) -(Math.pow(10, 7) * coast.minlat);
            int maxLon = (int) (Math.pow(10, 7) * coast.maxlon);
            int maxLat = (int) -(Math.pow(10, 7) * coast.maxlat);

            modelRef.coastlines.add(new Pair<>(
                    new int[]{minLon, minLat, maxLon, maxLat},
                    new Region(minLat, minLon, RegionType.COASTLINE, coast.path)));
        }

        // Signal to the structurer that the creation of MapObjects is done
        endOfOutput.set(true);
        Debugger.print(User.AESK, "Dataprocessing: ObjectFactory Done");
    }

    /**
     * Adds relevant data to the search list, based on tags.
     * @param obj of type OSMObject.
     */
    private void addToSearchList(OSMObject obj) {
        if (obj.getLat() == 0F || obj.getLon() == 0F) return;

        // Reference to the tmp_addreses hashmap
        if (l == null) l = modelRef.addressSearcher.tmp_addresses;

        String poi = obj.getTag("name");
        String city = obj.getTag("addr:city");
        String street = obj.getTag("addr:street");
        String postcode = obj.getTag("addr:postcode");
        String housenumber = obj.getTag("addr:housenumber");
        String place = obj.getTag("place");
        String postal_code = obj.getTag("postal_code");

        // Get the point for this node
        Point2D.Float point = new Point2D.Float(obj.getLon() * modelRef.lonfactor, -obj.getLat());

        // If it's a size add that and return
        if (place != null && poi != null) {
            if (place.equals("village") || place.equals("city") || place.equals("town")) {
                Address addr = new Address(poi, point, SuggestionType.CITY);
                String population = obj.getTag("population");
                addr.setPopulation(population != null ? Integer.parseInt(population) : 0);
                l.put(poi, addr);
                if (postal_code == null) return;
            }
        }

        // If it's a postcode then add that and return.
        if (postal_code != null) {
            // Remove the "DK-" prefix to make it easier to searc  for.
            postal_code = postal_code.replace("DK-","");
            l.put(postal_code, new Address(postal_code, point, SuggestionType.POSTCODE));
            return;
        }

        // Create new address object to fill up
        Address addr = new Address();

        // Set properties
        addr.setStreet(street + (housenumber != null ? " " + housenumber : ""));
        addr.setCity(city);
        addr.setPostcode(postcode);
        addr.setPoi(poi);
        addr.setPoint(point);

		/**
		 * Set point if it's not null.
		 * We check for duplicates first because streets and cities are sometimes also added
		 * as POIs, and that makes searching hard to do.
		 */
        if (poi != null && !l.containsKey(poi)) {
            addr.setName(addr.getPoi());
            addr.setType(SuggestionType.POI);
        }
        else if (street != null) {
            addr.setName(addr.getStreet());
            addr.setType(SuggestionType.STREET);
        }

        if (addr.getName() != null) l.put(addr.getName(), addr);
    }

    // This merge method is taken from trbj "https://github.itu.dk/trbj/bfst16/blob/master/handson5/Model.java" with adjustments

    /**
     * @param ways
     * @return
     */
    private List<CoastlineObject> merge(List<CoastlineObject> ways) {
        Map<Point2D, CoastlineObject> segments = new HashMap<>();

        // Loops through coastlines list
        for (CoastlineObject seg : ways) {
            CoastlineObject prev = segments.remove(seg.firstPoint);
            CoastlineObject next = segments.remove(seg.lastPoint);
            CoastlineObject merged = new CoastlineObject(seg.minlon, seg.maxlon, seg.minlat, seg.maxlat);
            Point2D.Float firstPoint, lastPoint;

            // Appends the previous path segment if found
            if (prev != null) {
                if (prev.equals(next)) next = null;
                // Reverts if wrong direction
                if (prev.firstPoint.equals(seg.firstPoint)) {
                    segments.remove(prev.lastPoint);
                    prev.reverse();
                }
                firstPoint = prev.firstPoint;
                merged.path.append(prev.path.getPathIterator(null), true);

            } else {
                firstPoint = seg.firstPoint;
            }
            // Appends current segment
            merged.path.append(seg.path, true);

            // Appends the next path segment if found
            if (next != null) {
                // Reverts if wrong direction
                if (next.lastPoint.equals(seg.lastPoint)) {
                    segments.remove(next.firstPoint);
                    next.reverse();
                }
                lastPoint = next.lastPoint;
                merged.path.append(next.path.getPathIterator(null), true);


            } else {
                lastPoint = seg.lastPoint;
            }
            // Puts first and lastPoint into the HashMap
            merged.firstPoint = firstPoint;
            merged.lastPoint = lastPoint;

            if (prev == null && next != null) {
                merged.minlon = next.minlon;
                merged.maxlon = next.maxlon;
                merged.minlat = next.minlat;
                merged.maxlat = next.maxlat;
            } else if (next == null && prev != null) {
                merged.minlon = prev.minlon;
                merged.maxlon = prev.maxlon;
                merged.minlat = prev.minlat;
                merged.maxlat = prev.maxlat;
            } else if (next != null && prev != null) {
                // Check minlon etc.
                if (next.minlon < prev.minlon) merged.minlon = next.minlon;
                else merged.minlon = prev.minlon;
                if (next.minlat > prev.minlat) merged.minlat = next.minlat;
                else merged.minlat = prev.minlat;

                if (next.maxlon > prev.maxlon) merged.maxlon = next.maxlon;
                else merged.maxlon = prev.maxlon;
                if (next.maxlat < prev.maxlat) merged.maxlat = next.maxlat;
                else merged.maxlat = prev.maxlat;
            }


            segments.put(firstPoint, merged);
            segments.put(lastPoint, merged);
        }
        // Converts HashMap to an ArrayList and returns it
        List<CoastlineObject> res = new ArrayList<>();
        segments.forEach((k, v) -> {
            if (k.equals(v.firstPoint)) res.add(v);
        });
        return res;
    }

    /**
     * Method created to avoid code duplication
     *
     * @param s States whether it's a natural, leisure etc.
     * @return which RegionType the current osmObject contains.
     */
    public RegionType getRegionType(String s) {
        if (s == null) return RegionType.UNSPECIFIED;
        switch (s) {
            case "farmland":
                return RegionType.FARMLAND;
            case "beach":
                return RegionType.BEACH;
            case "glacier":
                return RegionType.ICE;
            case "river_terrace":
            case "bay":
            case "spring":
            case "hot_spring":
            case "wetland":
            case "geyser":
            case "river":
            case "riverbank":
            case "water":
                return RegionType.WATER;
            case "tree_row":
            case "tree":
            case "wood":
                return RegionType.PARK;
            case "fell":
            case "heath":
            case "moor":
            case "scrub":
                return RegionType.SCRUB;
            case "ridge":
            case "arete":
            case "cliff":
            case "saddle":
            case "rock":
            case "stone":
            case "cave_entrance":
            case "sinkhole":
            case "mud":
            case "scree":
            case "peak":
            case "volcano":
            case "valley":
            case "shingle":
            case "bare_rock":
                return RegionType.ROCK;
            case "sand":
                return RegionType.SAND;
            case "common":
            case "track":
            case "golf_course":
            case "miniature_golf":
            case "nature_reserve":
            case "garden":
            case "greenfield":
            case "construction":
            case "grassland":
                return RegionType.GRASS;
            case "meadow":
                return RegionType.GRASS_LIGHTER;
            case "pitch":
                return RegionType.GRASS_DARKER;
            case "wildlife_hide":
            case "sports_centre":
            case "stadium":
            case "summer_camp":
            case "playground":
            case "bird_hide":
            case "firepit":
            case "bandstand":
            case "dog_park":
            case "park":
                return RegionType.PARK;
            case "adult_gaming_centre":
            case "dance":
            case "hackerspace":
            case "amusement_arcade":
                return RegionType.ARCADE;
            case "beach_resort":
                return RegionType.BEACH;
            case "fishing":
                return RegionType.FISHING;
            case "ice_rink":
                return RegionType.ICE_HOCKEY;
            case "slipway":
            case "swimming_area":
            case "swimming_pool":
            case "water_park":
            case "marina":
                return RegionType.MARINA;
            case "university":
                return RegionType.UNIVERSITY;
            case "forest":
                return RegionType.FOREST;
            case "residential":
                return RegionType.RESIDENTIAL;
            default:
                return RegionType.UNSPECIFIED;
        }
    }

    /**
     * Checks the give string against all the relevant words
     * to categorise the RoadTypes.
     * @param s of type String.
     * @return the RoadType.
     */
    public RoadType getRoadType(String s) {
        if (s == null) return RoadType.UNSPECIFIED;
        switch (s) {
            case "motorway_link":
            case "motorway":
                return RoadType.MOTORWAY;
            case "trunk_link":
            case "trunk":
                return RoadType.TRUNK;
            case "primary_link":
            case "primary":
                return RoadType.LARGE;
            case "secondary_link":
            case "secondary":
                return RoadType.MEDIUM;
            case "tertiary_link":
            case "tertiary":
                return RoadType.SMALL;
            case "track":
                return RoadType.MINI;
            case "unclassified":
            case "service":
            case "residential":
                return RoadType.RESIDENTIAL;
            case "path":
            case "pedestrian":
            case "footway":
                return RoadType.FOOTPATH;
            case "cycleway":
                return RoadType.CYCLEWAY;
            case "railway":
                return RoadType.RAILWAY;
            case "river":
            case "stream":
                return RoadType.RIVER;
            default:
                return RoadType.UNSPECIFIED;
        }
    }

    /**
     * Represents a temporary Coastline storing a path, firstPoint and lastPoint.
     */
    private class CoastlineObject {
        float minlon, maxlon, minlat, maxlat;
        private Point2D.Float firstPoint, lastPoint;
        private Path2D.Float path;

        /**
         * Constructs a CoastlineObject with the listed parameters.
         *
         * @param firstPoint The first point of the path.
         * @param lastPoint  The last point of the path.
         * @param path       The coastline path.
         */
        CoastlineObject(Point2D.Float firstPoint, Point2D.Float lastPoint, Shape path, float minlon, float maxlon, float minlat, float maxlat) {
            this.firstPoint = firstPoint;
            this.lastPoint = lastPoint;
            this.path = new Path2D.Float(path);

            this.minlat = minlat;
            this.maxlat = maxlat;
            this.minlon = minlon;
            this.maxlon = maxlon;
        }

        /**
         * Constructs an empty CoastlineObject.
         */
        CoastlineObject(float minlon, float maxlon, float minlat, float maxlat) {
            this.firstPoint = null;
            this.lastPoint = null;
            this.path = new Path2D.Float();

            this.minlat = minlat;
            this.maxlat = maxlat;
            this.minlon = minlon;
            this.maxlon = maxlon;
        }

        /**
         * Reverts the direction of the path and sets first and lastPoint accordingly.
         */
        void reverse() {
            List<Point2D.Float> pointList = new ArrayList<>();
            PathIterator iterator = path.getPathIterator(null);
            // Iterates over each point in the path and store it in the list.
            while (!iterator.isDone()) {
                float[] nextPoint = new float[2];
                iterator.currentSegment(nextPoint); // Stores the two coordinates in nextPoint
                pointList.add(new Point2D.Float(nextPoint[0], nextPoint[1]));

                iterator.next();
            }

            path = new Path2D.Float();
            firstPoint = pointList.get(pointList.size() - 1);
            lastPoint = pointList.get(0);
            path.moveTo(firstPoint.getX(), firstPoint.getY());

            // Recreates the path in reversed order
            for (int i = pointList.size() - 2; i >= 0; i--) {
                Point2D p = pointList.get(i);
                path.lineTo(p.getX(), p.getY());
            }
        }


    }
}