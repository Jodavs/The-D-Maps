package edu.itu.the_d.map.dataprocessing;

import edu.itu.the_d.map.datastructures.TwoDTree;
import edu.itu.the_d.map.utils.Debugger;
import edu.itu.the_d.map.utils.User;
import edu.itu.the_d.map.datastructures.Pair;
import edu.itu.the_d.map.model.Model;
import edu.itu.the_d.map.model.mapobjects.MapObject;
import edu.itu.the_d.map.utils.LoadingView;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * <p>
 * Class for orchestrating the construction of a model object from an .osm file. This happens through its only method
 * {@link #constructModelFromOSMFile(String, long, Model)}.
 * </p>
 * <p>
 * The overall process of converting the osm file to a finished model happens in three synchronous steps:
 * Parsing, object creation, and inserting into final data structures. These three steps are for the most part synchronous,
 * but specifically the creation of the KdTree is asynchronous, since it requires the complete list of objects to insert before creating itself.
 * </p>
 * <p>
 * The class itself mainly sets up the required environment for the {@link OSMParser}, {@link MapObjectFactory}, and {@link Structurer} to run.
 * Most importantly it creates two blocking queues for conveying information between the three threads. The osm object queue is filled by the parser
 * and consumed by the map object factory, and the map object queue is filled by the map object factory and consumed by the structurer.
 * Thus the osm data is first converted by the parser to an internal representation of osm data {@link OSMObject}s, and then by the map object
 * factory these objects are converted to their final form {@link MapObject}s. Finally the structurer prepares and executes the construction of the final
 * data structures, namely {@link TwoDTree}.
 * </p>
 * <p>
 * Because we don't want to handle both and enormous number of osm objects and map objects, these tasks run synchronously. In order for this to work,
 * the dispatcher creates the thread-safe blocking queues for sending information between threads, as well as a pair of {@link AtomicBoolean} variables
 * enabling the threads to signal each other when they finish their task.
 * </p>
 *
 * Copyright 2016 The-D
 *
 * @see OSMParser
 * @see MapObjectFactory
 * @see Structurer
 * @see TwoDTree
 * @see BlockingQueue
 * @see AtomicBoolean
 */
public class Dispatcher {
    private static final int QUEUE_SIZE = 1000000;

    /**
     * This is the only method of this class and orchestrates the construction of the model object. See the class description for details.
     *
     * @param filename the path and name of the osm file to load
     * @param modelRef a reference to a new model object. This is necessary since the reference is shared across threads.
     */
    public static void constructModelFromOSMFile(String filename, long s, Model modelRef) {
        // Two blocking queues for passing information between the threads
        BlockingQueue<OSMObject> osmobjQueue = new ArrayBlockingQueue<>(QUEUE_SIZE);
        BlockingQueue<Pair<int[], MapObject>> mapobjQueue = new ArrayBlockingQueue<>(QUEUE_SIZE);

        // Two atomic booleans for signalling other threads that the current one is finished
        AtomicBoolean endOfParseSignal = new AtomicBoolean(false);
        AtomicBoolean endOfConversionSignal = new AtomicBoolean(false);


        // Create the runnable objects
        OSMParser parser = new OSMParser(filename, osmobjQueue, endOfParseSignal);
        MapObjectFactory objectFactory = new MapObjectFactory(osmobjQueue, mapobjQueue, endOfParseSignal, endOfConversionSignal, modelRef, s);
        Structurer structurer = new Structurer(mapobjQueue, modelRef, endOfConversionSignal);

        // Create the threads responsible for executing the runnables
        Thread parserThread = new Thread(parser);
        Thread objectFactoryThread = new Thread(objectFactory);
        Thread structurerThread = new Thread(structurer);

        // Start the threads
        parserThread.start();
        objectFactoryThread.start();
        structurerThread.start();
        LoadingView.setInfoMsg("Parsing the given .osm file...");

        // Wait for the structurer thread to finish
        try {
            structurerThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        Debugger.print(User.AESK, "Dataprocessing: All Done");
    }
}
