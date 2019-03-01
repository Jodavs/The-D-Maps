package edu.itu.the_d.map.dataprocessing;

import edu.itu.the_d.map.utils.Debugger;
import edu.itu.the_d.map.utils.LoadingView;
import edu.itu.the_d.map.utils.ResourceLoader;
import edu.itu.the_d.map.utils.User;
import org.xml.sax.InputSource;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.zip.ZipInputStream;

/**
 * Class for parsing OSM files
 * <p>
 * Copyright 2016 The-D
 */
public class OSMParser implements Runnable {
    public BlockingQueue<OSMObject> queue;
    private AtomicBoolean endOfParseSignal;
    private XMLStreamReader streamReader;
    private int latlon = 0;
    private OSMObject osmObject;

    /**
     * Creates a new OSMParser with a BlockingQueue and has
     * a endOfParseSignal for when the parser is done.
     *
     * @param filename         a string, to the osm file
     * @param queue            a queue to put OSMObjects
     * @param endOfParseSignal boolean signaling that {@link OSMParser} is done
     */
    public OSMParser(String filename, BlockingQueue<OSMObject> queue, AtomicBoolean endOfParseSignal) {
        this.queue = queue;
        this.endOfParseSignal = endOfParseSignal;
        try {
            //FileInputStream in = null;
            InputStream in;
            try {
                if (filename.matches(".*zip")) {
                    ZipInputStream input = new ZipInputStream(new FileInputStream(filename));
                    input.getNextEntry();
                    in = input;
                } else {
                    in = new FileInputStream(filename);
                }
            } catch (FileNotFoundException e) {
                System.err.println("Specified map file could not be found: "+ filename);
                LoadingView.hasFailed("Specified map file could not be found: "+ filename);
                return;
            }
            XMLInputFactory factory = XMLInputFactory.newInstance();
            this.streamReader = factory.createXMLStreamReader(in);
            //this.eventReader = factory.createXMLEventReader(in);

        } catch (XMLStreamException | IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Finds the lat and lon positions for the first one, and if they
     * change position later on.
     */
    private void latlonNull() {
        latlon = 0;
        while (!streamReader.getAttributeName(latlon).toString().equals("lat")) {
            latlon++;
            if (streamReader.getAttributeName(latlon).toString().equals("lat")) {
                osmObject.setLat(Float.parseFloat(streamReader.getAttributeValue(latlon)));
                osmObject.setLon(Float.parseFloat(streamReader.getAttributeValue(latlon + 1)));
            }
        }
    }

    /**
     * Creates a new {@link OSMObject} and runs through the data
     * adding tags to the OSMObject and puts each OSMObject into the BlockingQueue.
     */
    public void run() {
        Thread.currentThread().setName("OSMParser-Thread");

        this.osmObject = new OSMObject();
        try {
            while (streamReader.hasNext()) {
                int eventType = streamReader.next();
                if (eventType == XMLStreamReader.START_ELEMENT) {
                    int i = 0;
                    String elementName = streamReader.getLocalName();
                    switch (elementName) {
                        case "bounds":
                            OSMObject bounds = new OSMObject();
                            bounds.setType(OSMType.BOUNDS);
                            while (streamReader.getAttributeName(i) != null) {
                                // adds the value of "minlat", "minlon", "maxlat" and "maxlon" bounds tags
                                bounds.addTags(streamReader.getAttributeName(i).toString(), streamReader.getAttributeValue(i));
                                i++;
                            }
                            queue.put(bounds);
                            break;
                        case "node":
                            // gets the value from the ID of type node and sets it
                            osmObject.setId(Long.parseLong(streamReader.getAttributeValue(0)));
                            try {
                                // gets the value from lat and lon and sets them
                                if (streamReader.getAttributeName(latlon).toString().equals("lat") || streamReader.getAttributeName(latlon) == null) {
                                    osmObject.setLat(Float.parseFloat(streamReader.getAttributeValue(latlon)));
                                    osmObject.setLon(Float.parseFloat(streamReader.getAttributeValue(latlon + 1)));
                                } else {
                                    latlonNull();
                                }
                            } catch (NullPointerException e) {
                                // if the position of lat changes and that position is empty
                                // .toString throws a NullPointerException and latlonNull() tries
                                // to find the new position of lat
                                latlonNull();
                            }
                            break;
                        case "way":
                            // gets the value of the ID og type way and sets it
                            osmObject.setId(Long.parseLong(streamReader.getAttributeValue(0)));
                            break;
                        case "nd":
                            // gets the value of type nd and adds them to a list
                            osmObject.setRef(streamReader.getAttributeValue(0));
                            break;
                        case "tag":
                            // gets the value of type tag and adds them to a hashMap
                            osmObject.addTags(streamReader.getAttributeValue(0), streamReader.getAttributeValue(1));
                            break;
                        case "relation":
                            // gets the value of the ID of type relation and sets it
                            osmObject.setId(Long.parseLong(streamReader.getAttributeValue(0)));
                            break;
                        case "member":
                            // Puth attribute 1 (ref id) and 2 (role) into memberMap
                            osmObject.addMember(Long.parseLong(streamReader.getAttributeValue(1)), streamReader.getAttributeValue(2));
                            break;
                        default:
                            break;
                    }
                } else if (eventType == XMLStreamReader.END_ELEMENT) {
                    String endElement = streamReader.getLocalName();
                    switch (endElement) {
                        case "way":
                            // sets the OSMType to WAY and adds it to the queue
                            osmObject.setType(OSMType.WAY);
                            queue.put(osmObject);
                            osmObject = new OSMObject();
                            break;
                        case "relation":
                            // sets the OSMType to relation and adds it to the queue
                            osmObject.setType(OSMType.RELATION);
                            queue.put(osmObject);
                            osmObject = new OSMObject();
                            break;
                        case "node":
                            // sets the OSMType to node and adds it to the queue
                            osmObject.setType(OSMType.NODE);
                            queue.put(osmObject);
                            osmObject = new OSMObject();
                            break;
                    }
                }
            }
        } catch (XMLStreamException | InterruptedException e) {
            throw new RuntimeException(e.toString());
        }
        endOfParseSignal.set(true);
        Debugger.print(User.AESK, "Dataprocessing: OSMParser Done");
    }

}