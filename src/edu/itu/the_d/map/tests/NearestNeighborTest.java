package edu.itu.the_d.map.tests;

import edu.itu.the_d.map.model.Model;
import junit.framework.TestCase;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;

import java.awt.geom.Point2D;

/**
 * Tests the nearest neighbor algorithm.
 * Copyright 2016 The-D
 */
public class NearestNeighborTest extends TestCase {
    Model m = Model.createModel("resources/resources/small.osm.zip");

    // Check if all the points are their own nearest neighbor
    /* @Test
    public void testNearestNeighborToYourself() {
        MinPQ<MapObject> min = m.getTreeRef().getRange((int) (m.getMinlon()*Math.pow(10,7)), (int) -(m.getMinlat()*Math.pow(10,7)), (int) (m.getMaxlon()*Math.pow(10,7)), (int) -(m.getMaxlat()*Math.pow(10,7)));
        for(MapObject map : min) {
            if (map instanceof Road) {
               for(long l : ((Road) map).getRefs()) {
                   Point2D p = m.objectMap.get(l);
                   Assert.assertEquals(l, m.nearestNeighbor(p));

               }
            }
        }
    } */

    @Test
    public void testDifferentInputs() {
        // Check if it's its own nearest neighbor
        Point2D p = m.objectMap.get(8901302);
        Assert.assertEquals(8901302, m.nearestNeighbor(p).getId());

        /*  Check with a point that is not in the graph but is expected to be pass
            p1 is taken close to "p" above and is expected to have the same nearest neighbor
         */
        Point2D p1 = m.objectMap.get(340485900);
        Assert.assertEquals(8901302, m.nearestNeighbor(p1).getId());

        // Check with a point not in the graph and is expected to fail
        /*Point2D p2 = m.objectMap.get(1236299916);
        Assert.assertEquals(8901302, m.nearestNeighbor(p2)); */

        // Check with a Point that has been initialized to null
        /*Point2D p3 = null;
        long longOfP3 = m.nearestNeighbor(p3); */

    }

}
