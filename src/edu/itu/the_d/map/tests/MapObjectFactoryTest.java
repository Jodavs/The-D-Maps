package edu.itu.the_d.map.tests;

import edu.itu.the_d.map.dataprocessing.*;
import edu.itu.the_d.map.datastructures.Pair;
import edu.itu.the_d.map.model.Model;
import edu.itu.the_d.map.model.mapobjects.MapObject;
import edu.itu.the_d.map.model.mapobjects.Region;
import edu.itu.the_d.map.model.mapobjects.Road;
import edu.itu.the_d.map.model.mapobjects.RoadType;
import edu.itu.the_d.map.utils.LoadingView;
import junit.framework.TestCase;
import org.junit.Rule;
import org.junit.Test;

import java.awt.geom.Point2D;
import java.util.Arrays;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static edu.itu.the_d.map.model.mapobjects.RegionType.COASTLINE;
import static edu.itu.the_d.map.model.mapobjects.RegionType.UNSPECIFIED;
import static edu.itu.the_d.map.model.mapobjects.RegionType.WATER;

/**
 * Tests the {@link MapObjectFactory} class.
 * Copyright 2016 The-D
 */
public class MapObjectFactoryTest extends TestCase {
	@Rule


 	// test case A, that lat and lon are converted as they should.
	@Test
	public void testnodeLatLon() throws InterruptedException{
		LoadingView.turnOn();

		BlockingQueue<OSMObject> osmobjQueue = new ArrayBlockingQueue<>(10000);

		OSMObject osmObject = new OSMObject();
		osmObject.addTags("minlat", "55.6631000");
		osmObject.addTags("minlon", "12.5730000");
		osmObject.addTags("maxlat", "55.6804000");
		osmObject.addTags("maxlon", "12.6031000");
		osmObject.setType(OSMType.BOUNDS);

		OSMObject osmObject1 = new OSMObject();
		osmObject1.setType(OSMType.NODE);
		osmObject1.setId(697801);
		osmObject1.setLat(55.6789058F);
		osmObject1.setLon(12.5774728F);

		OSMObject osmObject2 = new OSMObject();
		osmObject2.setType(OSMType.NODE);
		osmObject2.setId(54862);
		osmObject2.setLat(0F);
		osmObject2.setLon(0F);

		osmobjQueue.put(osmObject);
		osmobjQueue.put(osmObject1);
		osmobjQueue.put(osmObject2);

		BlockingQueue<Pair<int[], MapObject>> mapObjQueue = new ArrayBlockingQueue<>(50000);

		Model model = new Model();
		MapObjectFactory objectFactory = new MapObjectFactory(osmobjQueue, mapObjQueue, new AtomicBoolean(true), new AtomicBoolean(true), model, 0L);

		objectFactory.run();
		assertEquals(new Point2D.Float(7.092855F, -55.678905F), model.objectMap.get(697801));
		assertEquals(new Point2D.Float(0F, -0F), model.objectMap.get(54862));
	}

	// Test case B, checks that mapobjects of type road are set as they should.
	@Test
	public void testWayPaths(){
		LoadingView.turnOn();

		BlockingQueue<OSMObject> osmobjQueue = new ArrayBlockingQueue<>(50000);
		OSMParser parser = new OSMParser("resources/resources/test/test.osm", osmobjQueue, new AtomicBoolean(false));
		BlockingQueue<Pair<int[], MapObject>> mapObjQueue = new ArrayBlockingQueue<>(50000);


		Model model = new Model();
		MapObjectFactory objectFactory = new MapObjectFactory(osmobjQueue, mapObjQueue, new AtomicBoolean(true), new AtomicBoolean(true), model, 5000000L);

		parser.run();
		objectFactory.run();
		int count = 0;
		while (!mapObjQueue.isEmpty()) {
			try {
				Pair<int[], MapObject> obj = mapObjQueue.poll(10, TimeUnit.MILLISECONDS);
				if (obj == null) continue;
				//System.out.println(Arrays.toString(obj.valA));
				if (obj.valB instanceof Road) {
					Road r = (Road) obj.valB;
					if (count == 0){
						assertEquals(RoadType.RESIDENTIAL, r.getType());
					}
					else if (count == 3){
						assertEquals(RoadType.SMALL, r.getType());
					}
					else if (count == 6){
						assertEquals(RoadType.MOTORWAY, r.getType());
					}
					else if (count == 7){
						assertEquals(RoadType.MEDIUM, r.getType());
					}
					count++;
				}
				if (obj.valB instanceof Region) {
					Region r = (Region) obj.valB;
					assertEquals(UNSPECIFIED, r.getType());
				}
			} catch (InterruptedException e) {
				throw new RuntimeException(e.toString());
			}
		}
	}
}