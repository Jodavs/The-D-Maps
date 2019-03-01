package edu.itu.the_d.map.tests;

import edu.itu.the_d.map.dataprocessing.OSMObject;
import edu.itu.the_d.map.dataprocessing.OSMParser;
import junit.framework.TestCase;
import org.junit.Test;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by Lucas on 01-05-2016.
 */
public class OSMParserTest extends TestCase {

    static final int QUEUE_SIZE = 10000;
    static final int OSM_OBJECT_COUNT = 149;
    static final String A1 = "resources/resources/test/test.osm";
    static final String A2 = "resources/resources/test/test-missing-attributes.osm";
    static final String A3 = "resources/resources/test/test-missing-tags.osm";
    static final String A4 = "resources/resources/test/test-wrong-order.osm";

    @Test
    public void testRunCorrectInput() { //ID: A
        BlockingQueue<OSMObject> osmobjQueue = new ArrayBlockingQueue<>(QUEUE_SIZE);
        OSMParser parser = new OSMParser(A1, osmobjQueue, new AtomicBoolean(false));
        parser.run();
        assertEquals(OSM_OBJECT_COUNT, osmobjQueue.size());
    }


    @Test
    public void testRunInputMissingAtrributes() { //ID: B
        try {
            BlockingQueue<OSMObject> osmobjQueue = new ArrayBlockingQueue<>(QUEUE_SIZE);
            OSMParser parser = new OSMParser(A2, osmobjQueue, new AtomicBoolean(false));
            parser.run();
            fail();
        } catch (Exception e) {
        }
    }


    @Test
    public void testRunInputMissingTags() { //ID: C
        BlockingQueue<OSMObject> osmobjQueue = new ArrayBlockingQueue<>(QUEUE_SIZE);
        OSMParser parser = new OSMParser(A3, osmobjQueue, new AtomicBoolean(false));
        parser.run();
        assertEquals(OSM_OBJECT_COUNT, osmobjQueue.size());
    }


    @Test
    public void testRunInputWrongOrder() { //ID: D
        BlockingQueue<OSMObject> osmobjQueue = new ArrayBlockingQueue<>(QUEUE_SIZE);
        OSMParser parser = new OSMParser(A4, osmobjQueue, new AtomicBoolean(false));
        parser.run();
        assertEquals(OSM_OBJECT_COUNT, osmobjQueue.size());
    }


}
