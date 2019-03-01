package edu.itu.the_d.map.dataprocessing;

import edu.itu.the_d.map.datastructures.Pair;

import java.util.*;

/**
 * Class used to store data from the osm file.
 * <p>
 * Copyright 2016 The-D
 */
public class OSMObject {
    public Map<String, String> tagsMap;
    public Set<Pair<Long, String>> memberSet;
    private long id;
    private float lat;
    private float lon;
    private List<Long> ref;
    private OSMType type = OSMType.UNKNOWN;

    /**
     * Constructor that creates a new OSMObject, with a HashMap, HashSet and ArrayList
     */
    public OSMObject() {
        this.tagsMap = new HashMap<>();
        this.memberSet = new HashSet<>();
        this.ref = new ArrayList<>();
    }

    /**
     * Returns the reference on index k
     * @param k of type string
     * @return the reference as a string
     */
    public long getRef(int k) {
        return ref.get(k);
    }

    /**
     * Gets the size of the reference list.
     * @return the size as an int.
     */
    public int getRefSize() {
        return ref.size();
    }

    /**
     * Returns the ID
     * @return as a long
     */
    public long getId() {
        return id;
    }

    /**
     * Sets ID
     * @param id of type long
     */
    public void setId(long id) {
        this.id = id;
    }

    /**
     * Returns the lat
     * @return the lat as a float
     */
    public float getLat() {
        return lat;
    }

    /**
     * Sets the lat
     * @param lat of type float
     */
    public void setLat(float lat) {
        this.lat = lat;
    }

    /**
     * Returns the lon
     * @return the lon as a float
     */
    public float getLon() {
        return lon;
    }

    /**
     * Sets the lon
     * @param lon of type float
     */
    public void setLon(float lon) {
        this.lon = lon;
    }

    /**
     * Iterates over the tagsMap
     * @return the tag.
     */
    public Iterator<Map.Entry<String, String>> getTagsIterator() {
        return tagsMap.entrySet().iterator();
    }

    /**
     * Returns the refs
     * @return of type strings
     */
    public List<Long> getRef() {
        return ref;
    }

    /**
     * Adds tings to the ref list
     * @param k of type string.
     */
    public void setRef(String k) {
        ref.add(Long.parseLong(k));
    }

    /**
     * Returns the tags with the given key.
     * @param key of type string
     * @return tags as strings
     */
    public String getTag(String key) {
        return tagsMap.get(key);
    }

    /**
     * Adds tags to the hashMap
     * @param key   of type string
     * @param value of type string
     */
    public void addTags(String key, String value) {
        tagsMap.put(key, value);
    }

    /**
     * Adds members to the HashSet
     * @param key   of type long
     * @param value of type Pair
     */
    public void addMember(long key, String value) {
        memberSet.add(new Pair<>(key, value));
    }

    /**
     * Gets the member set
     * @return member set
     */
    public Set<Pair<Long, String>> getMemberSet() {
        return memberSet;
    }

    /**
     * Returns the OMSType
     *
     * @return as OSMType
     */
    public OSMType getType() {
        return type;
    }

    /**
     * Sets the OSMType
     *
     * @param type of type OSMType
     */
    public void setType(OSMType type) {
        this.type = type;
    }

    /**
     * Returns type as a string
     *
     * @return a string
     */
    public String toString() {
        return type.toString();
    }

}