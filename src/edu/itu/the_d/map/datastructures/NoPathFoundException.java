package edu.itu.the_d.map.datastructures;

/**
 * A more appropiate exception thrown by {@link Dijkstra} if there's no path found between Source and Target.
 * This is a subclass of {@link RuntimeException}.
 * <p>
 * Copyright 2016 The-D
 */
public class NoPathFoundException extends RuntimeException {
    public NoPathFoundException(String message) {
        super(message);
    }
}
