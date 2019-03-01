package edu.itu.the_d.map.datastructures;

import java.io.Serializable;

/**
 * A (bad-practice) class used when it is useful to pass two values as a pair.
 * There's so many times where it makes sense to pass two values as a pair, but it would be better practice to create a new object for them,
 * so this class is really just bad practice in Java programming; yet we use it to make life easier. Yup.
 * <p>
 * Copyright 2016 The-D
 *
 * @param <A> The generic type of the first value - valA.
 * @param <B> The generic type of the second value - valB.
 */
public class Pair<A, B> implements Serializable {
    private static final long serialVersionUID = 113;

    // The fields are public final, so a getter is not needed and they may only be accessed (not set again) after initialization
    // This is good, because we save the getter method for each pair method
    public final A valA;
    public final B valB;

    /**
     * Initialize the Pair with the given two values.
     * @param valA The value to assign to valA.
     * @param valB The value to assign to valB.
     */
    public Pair(A valA, B valB) {
        this.valA = valA;
        this.valB = valB;
    }
}
