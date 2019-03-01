package edu.itu.the_d.map.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.net.HttpURLConnection;
import java.net.URL;

public final class GeoLocation implements Serializable {
	public static final long serialVersionUID = 12391239;

    private double longitude;
    private double latitude;
    private String weatherIcon;
    private String weatherTemperature;
	//This is the URL to get weather and location information from.
    private static final String URL_GEOLOCATION = "http://lucas.lethjemmeside.dk/misc/geolocation/";


	public GeoLocation() {
		loadUserInformation();
	}


	/**
	 *  Get weather temperature
	 * @return String temperature
	 */
	public String getWeatherTemperature() {
		return weatherTemperature;
	}


	/**
	 * Get a string representing the name of the image file to load
	 * @return String representing the name of the image file to load
	 */
	public String getWeatherIcon() {
		return weatherIcon;
	}


	/**
	 * Get users latitude
	 * @return double latitude
	 */
	public double getLatitude() {
		return latitude;
	}

	/**
	 * Get users longitude
	 * @return double longitude
	 */
	public double getLongitude() {
		return longitude;
	}


    /**
     * Update the users location and gather weather information
     */
    public void loadUserInformation() {
        Thread t = new Thread() {
            public void run() {
                try {
                    sendRequest();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        };
        t.start();
    }

    /**
     * Send a request that returns a string with information about the users
     * approximate location (Based on ISP location) and the weather where the user is located.
     *
     * @throws IOException
     */
    private void sendRequest() throws IOException {
		//Set URL
		URL obj = new URL(URL_GEOLOCATION);
		//Open new http connection
        HttpURLConnection con = (HttpURLConnection) obj.openConnection();
		//Set request type and agent
        con.setRequestMethod("GET");
        con.setRequestProperty("User-Agent", "Mozilla/5.0");
		//Get input stream
        BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
        String inputLine;
        StringBuilder response = new StringBuilder();
		//Append each line of the result the string
        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();
		//Check if status is OK and return if not
        String[] parts = response.toString().split(";");
        if (!parts[0].equals("OK")) return;
		//Set fields
        latitude = Double.parseDouble(parts[8]);
        longitude = Double.parseDouble(parts[9]);
        weatherIcon = parts[11];
		//Append celsius symbol
        weatherTemperature = parts[12].split("\\.")[0] + "°";
    }

}