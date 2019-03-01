package edu.itu.the_d.map.model.mapobjects;

import edu.itu.the_d.map.view.SuggestionType;

import java.awt.geom.Point2D;
import java.io.Serializable;

/**
 * Address object. This is being used for searching and navigating.
 * <p>
 * Copyright 2016 The-D
 */
public class Address implements Comparable<Address>, Serializable {
    public static final long serialVersionUID = 1231;

    // Point for this address
    private Point2D.Float point;

    // Name of this address object. Can be street, city, poi etc.
    private String name;
    private String city;
    private String street;
    private String postcode;
    private String poi;
    private int population;

    // The type of this address. City, street etc.
    private SuggestionType type;

    /**
     * Constructor
     */
    public Address(){

    }

    /**
     * Constructor
     *
     * @param name  String
     * @param point Point2D.Float
     * @param type  SuggestionType
     */
    public Address(String name, Point2D.Float point, SuggestionType type) {
        this.name = name;
        this.point = point;
        this.type = type;
    }

    /**
     * Constructor
     *
     * @param name String
     */
    public Address(String name) {
        this.name = name;
    }

    /**
     * Get population of a city
     *
     * @return int
     */
    public int getPopulation() {
        return population;
    }

    /**
     * Set population of a city
     *
     * @param population int
     */
    public void setPopulation(int population) {
        this.population = population;
    }

    /**
     * Get POI
     *
     * @return String
     */
    public String getPoi() {
        return poi;
    }

    /**
     * Set POI
     *
     * @param poi String
     */
    public void setPoi(String poi) {
        this.poi = poi;
    }

    /**
     * Get Street
     *
     * @return String
     */
    public String getStreet() {
        return street;
    }

    /**
     * Set Street
     *
     * @param street String
     */
    public void setStreet(String street) {
        this.street = street;
    }

    /**
     * Get Postcode
     *
     * @return String
     */
    public String getPostcode() {
        return postcode;
    }

    /**
     * Set Postcode
     *
     * @param postcode String
     */
    public void setPostcode(String postcode) {
        this.postcode = postcode;
    }

    /**
     * Get City
     *
     * @return String
     */
    public String getCity() {
        return city;
    }

    /**
     * Set City
     *
     * @param city String
     */
    public void setCity(String city) {
        this.city = city;
    }

    /**
     * Get the point
     *
     * @return Point2D.Float
     */
    public Point2D.Float getPoint() {
        return point;
    }


    /**
     * Set the point
     *
     * @param point Point2D.Float
     */
    public void setPoint(Point2D.Float point) {
        this.point = point;
    }

    /**
     * Get the type
     *
     * @return SuggestionType
     */
    public SuggestionType getType() {
        return type;
    }

    /**
     * Set the type
     *
     * @param type SuggestionType
     */
    public void setType(SuggestionType type) {
        this.type = type;
    }

    /**
     * Get name
     *
     * @return String
     */
    public String getName() {
        return name;
    }

    /**
     * Set name
     *
     * @param name String
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Check if this object equals another object.
     *
     * @param o Object
     * @return boolean
     */
    @Override
    public boolean equals(Object o) {
        return toString().toLowerCase().equals(o.toString().toLowerCase());
    }

    /**
     * Print the name of this address object
     *
     * @return String
     */
    @Override
    public String toString() {
        return this.name;
    }

    /**
     * Returns what address should be shown, whether it is a POI or a street.
     * @return String
     */
    public String toDisplayString() {
        String str = "";
        switch (type) {
            case POI:
                if (street != null) str += street + " ";
                if (city != null) str += city + " ";
                if (postcode != null) str += postcode;
                break;
            case STREET:
                if (city != null) str += city + " ";
                if (postcode != null) str += postcode;
                break;
        }
        if (str.length() == 0 || str.equals("null "))
            str = getType().typeName;
        return str;
    }

    /**
     * Compare this address object to another object
     *
     * @param o Address object
     * @return int
     */
    public int compareTo(Address o) {
        return name.toLowerCase().compareTo(o.getName().toLowerCase());
    }

}
