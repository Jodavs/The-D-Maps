package edu.itu.the_d.map.tests;

import edu.itu.the_d.map.datastructures.Dijkstra;
import edu.itu.the_d.map.datastructures.TwoDTree;
import edu.itu.the_d.map.datastructures.WayEdge;
import edu.itu.the_d.map.datastructures.WayUndirectedGraph;
import edu.itu.the_d.map.datastructures.algs4.MinPQ;
import edu.itu.the_d.map.datastructures.nongeneric_maps.IdMap;
import edu.itu.the_d.map.model.Model;
import edu.itu.the_d.map.model.mapobjects.MapObject;
import edu.itu.the_d.map.model.mapobjects.Region;
import edu.itu.the_d.map.model.mapobjects.RegionType;
import junit.framework.TestCase;
import org.junit.Test;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Tests the dijkstra algorithm.
 * Copyright 2016 The-D
 */
public class DijkstraWhiteBoxTest{

	private static Model model = Model.createModel("resources/resources/bornholm.osm.zip");

	@Test
	public void testA() {
		IdMap map = new IdMap(2);
		model.objectMap = map;
		map.put(0, 5, 5);
		WayUndirectedGraph graph = new WayUndirectedGraph(map);
		int s = 0; // Source
		int p = 0; // Target
		Dijkstra dijkstra = new Dijkstra(graph, s, p, model);

		// The test passes if no errors are encountered
	}

	@Test
	public void testC() {
		IdMap map = new IdMap(3);
		map.put(0, 5, 5);
		map.put(1, 6, 5);
		map.put(2, 7, 5);

		model.objectMap = map;
		WayUndirectedGraph graph = new WayUndirectedGraph(map);
		graph.addEdge(new WayEdge(0, 1, 0, 1, 0, (byte) 63));
		graph.addEdge(new WayEdge(1, 2, 0, 1, 0, (byte) 63));
		int s = 0; // Source
		int p = 2; // Target
		Dijkstra dijkstra = new Dijkstra(graph, s, p, model);

		assertTrue(dijkstra.hasPathTo(p));

		for (WayEdge edge : dijkstra.pathTo(p)) {
			if (edge.fromV == s || edge.toV == s) {
				// Assert that this edge does not end at the target vertex
				assertEquals(false, edge.other(s) == p);
			} else if (edge.fromV == 1 || edge.toV == 1) {
				assertEquals(true, edge.other(1) == p);
			}
		}
	}

	@Test(expected = IllegalArgumentException.class)
	public void testD() {
		IdMap map = new IdMap(3);
		model.objectMap = map;
		map.put(0, 5, 5);
		map.put(1, 5, 5);
		WayUndirectedGraph graph = new WayUndirectedGraph(map);
		graph.addEdge(new WayEdge(0, 1, 0, -1, 0, (byte) 0));
		int s = 0; // Source
		int p = 0; // Target
		Dijkstra dijkstra = new Dijkstra(graph, s, p, model);
	}

	@Test
	public void testE() {
		IdMap map = new IdMap(3);
		model.objectMap = map;
		map.put(0, 5, 5);
		map.put(1, 5, 5);
		WayUndirectedGraph graph = new WayUndirectedGraph(map);
		graph.addEdge(new WayEdge(0, 1, 0, 0, 0, (byte) 0));
		int s = 0; // Source
		int p = 1; // Target
		Dijkstra dijkstra = new Dijkstra(graph, s, p, model);

		// The test passes if no errors are encountered
	}

	@Test (expected = ArrayIndexOutOfBoundsException.class)
	public void testF() {
		IdMap map = new IdMap(3);
		model.objectMap = map;
		WayUndirectedGraph graph = new WayUndirectedGraph(map);
		int s = 0; // Source
		int p = 0; // Target
		Dijkstra dijkstra = new Dijkstra(graph, s, p, model);
	}
}