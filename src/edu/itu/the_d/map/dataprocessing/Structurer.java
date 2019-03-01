package edu.itu.the_d.map.dataprocessing;

import edu.itu.the_d.map.utils.Debugger;
import edu.itu.the_d.map.utils.User;
import edu.itu.the_d.map.datastructures.Pair;
import edu.itu.the_d.map.model.Model;
import edu.itu.the_d.map.model.mapobjects.MapObject;
import edu.itu.the_d.map.utils.LoadingView;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Responsible for receiving the finished {@link MapObject}s and position information of these objects and starting
 * the construction of the 2d-tree. This class is part of the process set up by the {@link Dispatcher} class.
 * <p>
 * Copyright 2016 The-D
 *
 * @see Dispatcher
 * @see OSMParser
 * @see MapObjectFactory
 */
public class Structurer implements Runnable {
	/**
     * Input queue of objects from {@link MapObjectFactory}.
     */
    private BlockingQueue<Pair<int[], MapObject>> objQueue;
	/**
     * Signal from {@link MapObjectFactory}. It's set to true when that thread is finished.
     */
    private AtomicBoolean endOfConversionSignal;
    private Model modelRef;

	/**
     * Create a new instance
     * @param objQueue the queue of map objects and their position information to process
     * @param modelRef a reference to the model. Used to get a reference to the output 2d-tree
     * @param endOfConversionSignal becomes true when the factory constructing the objects on the queue is finished
     */
    public Structurer(BlockingQueue<Pair<int[], MapObject>> objQueue, Model modelRef, AtomicBoolean endOfConversionSignal) {
        this.objQueue = objQueue;
        this.modelRef = modelRef;
        this.endOfConversionSignal = endOfConversionSignal;
    }

	/**
     * Converts the object queue into two separate lists, which are then used as arguments for the construction method
     * of the 2d-tree.
     *
     * @see Dispatcher
     */
    public void run() {
        Thread.currentThread().setName("Structurer-Thread");

        // Create two empty lists that will become the input to the 2d-tree's construction method.
        List<int[]> posinfoList = new ArrayList<>(1000000);
        List<MapObject> mapobjList = new ArrayList<>(1000000);

        // Loop until there's no more items in the queue and the map object factory is finished processing
        while (!endOfConversionSignal.get() || !objQueue.isEmpty()) {
            try {
                // This basically pulls out the current item from the queue and adds each part to the their respective
                // list
                Pair<int[], MapObject> obj = objQueue.poll(10, TimeUnit.MILLISECONDS);
                if (obj == null) continue;

                posinfoList.add(obj.valA);
                mapobjList.add(obj.valB);
            } catch (InterruptedException e) {
                throw new RuntimeException(e.toString());
            }
        }
        Debugger.print(User.AESK, "making kdtree");

        // Set the loading view info message
        LoadingView.setInfoMsg("Constructing KD tree from Map Objects...");

        // Construct the 2d-tree from the now finished input lists
        modelRef.getTreeRef().constructFromList(posinfoList, mapobjList);

        Debugger.print(User.AESK, "size:" + modelRef.getTreeRef().size());
    }
}
