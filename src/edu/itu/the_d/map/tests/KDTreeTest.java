package edu.itu.the_d.map.tests;

import edu.itu.the_d.map.datastructures.TwoDTree;
import edu.itu.the_d.map.datastructures.algs4.MinPQ;
import edu.itu.the_d.map.model.Model;
import edu.itu.the_d.map.model.mapobjects.MapObject;
import edu.itu.the_d.map.model.mapobjects.Region;
import edu.itu.the_d.map.model.mapobjects.RegionType;
import junit.framework.TestCase;
import org.junit.Test;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

/**
 * Tests the {@link TwoDTree}.
 * Copyright 2016 The-D
 */
public class KDTreeTest extends TestCase {
    private static Model model = Model.createModel("resources/resources/small.osm.zip");
    private static TwoDTree tree = model.getTreeRef();

    @Test
    public void testGetAllData() {
        MinPQ<MapObject> range = tree.getRange((int) (model.getMinlon() * Math.pow(10, 7)), (int) -(model.getMinlat() * Math.pow(10, 7)), (int) (model.getMaxlon() * Math.pow(10, 7)), (int) -(model.getMaxlat() * Math.pow(10, 7)));

        assertTrue(range.size() > 0);
        for (MapObject o : range) {
        }
    }

    @Test
    public void testNegativeRange() {
        MinPQ<MapObject> range = tree.getRange((int) -(model.getMinlon() * Math.pow(10, 7)), (int) (model.getMinlat() * Math.pow(10, 7)), (int) -(model.getMaxlon() * Math.pow(10, 7)), (int) (model.getMaxlat() * Math.pow(10, 7)));
        assertEquals(0, range.size());
    }

    @Test
    public void testEntriesInTree() {
        List<int[]> iList = new ArrayList<>();
        iList.add(new int[]{10, 10, 10, 10});

        List<MapObject> oList = new ArrayList<>();
        Region testObject = new Region(10, 10, RegionType.GRASS, null);
        oList.add(testObject);
        TwoDTree tmpTree = new TwoDTree();
        tmpTree.constructFromList(iList, oList);

        //MinPQ<MapObject> range = tree.getRange((int) -(model.getMinlon() * Math.pow(10, 7)), (int) (model.getMinlat() * Math.pow(10, 7)), (int) -(model.getMaxlon() * Math.pow(10, 7)), (int) (model.getMaxlat() * Math.pow(10, 7)));

        MinPQ<MapObject> range = tmpTree.getRange(0, 0, 20, 20);

        assertEquals(1, range.size());
        assertEquals(testObject, range.delMin());
    }
}