package edu.itu.the_d.map.utils;

import java.io.InputStream;
import java.net.URL;

/**
 * Class used to get all the necessary resources as stream, so that they are included
 * when running a jar file.
 * <p>
 * Copyright 2016 The-D
 */

public final class ResourceLoader {
	/**
	 * Gets the resources as stream from path of type string
	 * so that they can run with the jar file.
	 *
	 * @param path of type string.
	 * @return and InputStream of resources.
	 */
	public static InputStream load(String path) {
		InputStream input = ResourceLoader.class.getResourceAsStream(path);
		if (input == null) {
			input = ResourceLoader.class.getResourceAsStream("/" + path);
		}
		return input;
	}

	/**
	 * Get a resource file URL object.
	 * @param path path to the file to load
	 * @return a {@link URL}
	 */
	public static URL getUrl(String path) {
		return ResourceLoader.class.getResource("/" + path);
	}
}