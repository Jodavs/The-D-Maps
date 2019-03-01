package edu.itu.the_d.map.model.mapobjects;

import edu.itu.the_d.map.dataprocessing.Levenshtein;
import edu.itu.the_d.map.datastructures.Pair;
import edu.itu.the_d.map.utils.Debugger;
import edu.itu.the_d.map.utils.ImageLoader;
import edu.itu.the_d.map.utils.User;
import edu.itu.the_d.map.view.RetinaIcon;
import edu.itu.the_d.map.view.SuggestionType;
import edu.itu.the_d.map.view.SuggestionView;
import edu.itu.the_d.map.view.View;

import java.io.Serializable;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This class is responsible for live searching and identifying address objects.
 * <p>
 * Copyright 2016 The-D
 */
public class AddressSearcher implements Serializable {
    public static final long serialVersionUID = 1231;
    /**
     * This hashamp temporaily contains all the address objects while we're parsing
     * the OSM file. We use a hashmap to be able to quickly check for duplicates.
     */
    public HashMap<String, Address> tmp_addresses = new HashMap<>();
    /**
     * This array is a sorted array that contains all the searchable address objects after parsing.
     */
    private Address[] addresses;
    /**
     * This arraylist contains all the cities that has been parsed. We use this list to
     * display city names on the map.
     */
    private ArrayList<Address> cities = new ArrayList<>();
    /**
     * These regular expressions are used by the liveSearch method to check
     * if a given string matches a address pattern.
     */
    private static Pattern pat1 = Pattern.compile("(?<vejnavn>[A-ø ]+) (?<husnummer>[0-9]+[ ]?[A-Z]?)[,] (?<postnummer>[0-9]{4}) (?<bynavn>[A-ø ]+)");
    private static Pattern pat2 = Pattern.compile("(?<vejnavn>[A-ø ]+) (?<husnummer>[0-9]+[ ]?[A-Z]?)[,] (?<bynavn>[A-ø ]+)");
    private static Pattern pat3 = Pattern.compile("(?<vejnavn>[A-ø ]+) (?<husnummer>[0-9]+[ ]?[A-Z]?)[,] (?<etage>st|[1-9][0-9]?)[.]?[ ]?(?<side>th|tv|mf|[0-9]+)[.]?, (?<postnummer>[0-9]{4}) (?<bynavn>[A-ø ]+)");

    /**
     * Sorts a list of address objects by their names
     *
     * @param arr a sorted arraylist.
     */
    private static void insertionSort(ArrayList<Address> arr) {
        int i, j;

        for (i = 1; i < arr.size(); i++) {
            Address key = arr.get(i);
            j = i;
            while ((j > 0) && (arr.get(j - 1).toString().compareTo(key.toString()) > 0)) {
                arr.set(j, arr.get(j - 1));
                j--;
            }
            arr.set(j, key);
        }
    }

    /**
     * @return A list of addresses
     */
    public Address[] getAddresses() {
        return addresses;
    }

    /**
     * @return An arraylist of cities.
     */
    public ArrayList<Address> getCities() {
        return cities;
    }

    /**
     * This method takes the content of tmp_addresses and puts it in the addresses array
     * and then sorts it.
     */
    public void initialize() {
        addresses = new Address[tmp_addresses.size()];
        Iterator it = tmp_addresses.entrySet().iterator();
        int i = 0;
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry) it.next();
            Address addr = (Address) pair.getValue();
            addresses[i] = addr;
            if (addr.getType() == SuggestionType.CITY) cities.add(addr);
            i++;
            it.remove(); // avoids a ConcurrentModificationException
        }
        Arrays.sort(addresses);
        tmp_addresses = null;
        //Sort cities from biggest to smallest population
        Collections.sort(cities, (address2, address1) -> ((Integer) address1.getPopulation()).compareTo(address2.getPopulation()));
    }


    /**
     * Search in the address array for any address object that matches or partially matches a given string value
     * @param value The string to search
     * @param limit a int value indicating how many results to return
     * @return a list of address objects that matches the specified string.
     */
    private ArrayList<Address> partialBinarySearch(String value, int limit) {
        //Initialize the list
        ArrayList<Address> list = new ArrayList<>();
        /**
         * Initiate a binary search that checks if any address object's name starts with the given
         * string
         */
        partialBinarySearchStartsWith(list, value, 0, addresses.length - 1, limit);
        //Sort the list
        insertionSort(list);
        return list;
    }


    /**
     * Do a linear search through the addresses array and calculate the levenshtein distance between
     * any of the address objects's name with the specified string and add it to the list if the
     * levenshtein distance is less than or equal to the maximum allowed distance specified
     * in the Levenshtein class.
     * @param query String to search for
     * @param limit limit the amount of search results
     */
    public ArrayList<Address> levenshteinSearch(String query, int limit) {
        ArrayList<Address> list = new ArrayList<>();
        for (Address obj : addresses) {
            if (list.size() >= limit && limit != 0) return list;
            if (Levenshtein.isLevenshteinAllowed(obj.toString(), query)) list.add(obj);
        }
        insertionSort(list);
        return list;
    }


    /**
     * Initiate a timer that waits for ~ half a second and then initiates a levenshtein search if the user
     * has not searched for anything else in that half a second (At this point we guess that the user has
     * stopped typing and is waiting for a result). <br>
     * So in case the fast partialBinarySearch couldn't find any results
     * for the user we do a levenshtein search in another thread because levenshtein search does N levenshtein distance
     * calculations where N is the number of addresses stored in addresses[] and then we show those potential results
     * to the user in the form of suggestions. <br>
     * If this search finds something that partialBinarySearch has not then the user has misspelled something in his
     * search query.
     * @param v View reference to show suggestions in
     * @param query String for levenshtein to search for
     * @param limit Limit the results for Levenshtein Search
     */
    private void initiateLevenshteinSearch(View v, String query, int limit) {
        v.showLoadingIcon();
        v.showKnowPanel();
        Thread t = new Thread() {
            public void run() {
                try {
                    Thread.sleep(650);
                    if (!v.tf_active.getText().equals(query) || partialBinarySearch(query,1).size() > 0) {v.hideLoadingIcon();return;}
                    ArrayList<Address> list = levenshteinSearch(query, limit);
                    if (list.size() > 0) {
                        SuggestionView.showSuggestions(list, query, v);
                        v.setAddress(list.get(0));
                    }
                    else {
                        v.hideKnowPanel();
                    }
                    v.hideLoadingIcon();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        };
        t.start();
    }

    /**
     * This method does a binary search through the addresses array and check if any of the addresses name
     * starts with the specified string.
     * If a match is found we add it to the list and search in both left and right part of the array, therefore
     * this method is not guaranteed to run in log n time and might take time.
     * @param list to fill up with matches
     * @param value string to search for
     * @param min minimum index of the array to look in
     * @param max maximum index of the array to look in
     * @param limit the max size of results to return
     */
    private void partialBinarySearchStartsWith(ArrayList<Address> list, String value, int min, int max, int limit) {
        if (min > max) {
            return;
        }

        if (list.size() >= limit && limit != 0) return;

        int mid = (max + min) / 2;

        if (addresses[mid].toString().toLowerCase().startsWith(value.toLowerCase())) {
            list.add(addresses[mid]);
            partialBinarySearchStartsWith(list, value, min, mid - 1, limit);
            partialBinarySearchStartsWith(list, value, mid + 1, max, limit);
        } else if (addresses[mid].compareTo(new Address(value)) > 0) {
            partialBinarySearchStartsWith(list, value, min, mid - 1, limit);
        } else {
            partialBinarySearchStartsWith(list, value, mid + 1, max, limit);
        }
    }

    /**
     * Initiates a binary search
     * @param value search value
     * @return int index if successful, -1 if not.
     */
    public int binarySearch(String value) {
        return binarySearch(value, 0, addresses.length - 1);
    }

    /**
     * Does a binary search on a sorted array of AddressObjects recursively.
     *
     * @param value Search Value
     * @param min   Minimum search boundary
     * @param max   Maximum search boundary
     * @return int index if successful, -1 if not.
     */
    private int binarySearch(String value, int min, int max) {
        if (min > max) {
            return -1;
        }

        int mid = (max + min) / 2;
        if (addresses[mid].equals(new Address(value))) {
            return mid;
        } else if (addresses[mid].compareTo(new Address(value)) > 0) {
            return binarySearch(value, min, mid - 1);
        } else {
            return binarySearch(value, mid + 1, max);
        }
    }


    /**
     * <p>
     * This method takes a string and a view reference as parameter.
     * </p>
     * <p>
     * The purpose of this method is to show a list of suggestions based on the content of the specified string.
     * Searching is done in three steps:
     * <ol>
     * <li> Split string by spaces and clear previously found addresses in view, if any. </li>
     * <li>
     *     <ol type="a">
     *         <li>Check if the specified string matches one of our regular expresssions.
     *          If this is the case then return the address object that matches the specified street name.</li>
     *         <li>Go through each part of the string and check if any of the addreseses in the addreses array matches
     *          this string. To be able to find multi-word names like "Vejlands Allé 163A" we check backwards.
     *          So for example if the specified string is "Vejlands Allé 163A København" we do the following checks
     *           in the following order:
     *           <ol>
     *              <li> Match "Vejlands Allé 163A København" - This produces no results because no single address object
     *             contains this as it's name. So we now remove København from this round of searching. </li>
     *              <li> Match "Vejlands Allé 163A" - This produces a result so we now remove all three words from the list
     *                 so that we don't try to match parts that has already been proven to be part of a address object.
     *              We also do a call to v.setAddress(addressObject); which saves this address object that we have found
     *              so that the user can initiate a navigation or a panning. </li>
     *              <li> Match "København" - This produces a result and we now remove this from the list.
     *                 Just as before we also call v.setAddress(); to store this city. </li>
     *              <li>4. Search is done.</li>
     *           </ol>
     *      </li>
     *  </li>
     *  </ol>
     *  3. Show suggestions. We only show suggestions if the input does not exactly match any address object.
     *  </p>
     *  Note: The pros of this method is that the user can search in just about any order and combination of
     *        a address and even misspell some of it. The cons is of course that the user can for example search
     *        for a street and a postcode that does not match because we match each part individually.
     *        Although this could be considered a pro as well because if a address is vaguely recorded in the OSM
     *        data set then the user will most probably still be able to find it with this search method.
     * @param query String to match with and show suggestions for.
     * @param v View reference to use to show suggestions in.
     * @return the Address object that best matches the specified query
     */
    public Address liveSearch(String query, View v) {
        // Set variables
        String[] sp = query.split(" ");
        String suggestionQuery = "";

        //Clear previously stored addresses
        v.clearAddress();

        //Try to match with regular expressions
        Matcher m1 = pat1.matcher(query);
        Matcher m2 = pat2.matcher(query);
        Matcher m3 = pat3.matcher(query);

        //Created arraylist to fill results in
        ArrayList<Address> arr = new ArrayList<>();

        if (m1.matches()) {
            int pos = binarySearch(m1.group(1) + " " + m1.group(2));
            if (pos > -1) {
                v.setAddress(addresses[pos]);
                arr.add(addresses[pos]);
                SuggestionView.showSuggestions(arr, m1.group(1) + " " + m1.group(2), v);
                return v.getAddress();
            }
        } else if (m2.matches()) {
            int pos = binarySearch(m2.group(1) + " " + m2.group(2));
            if (pos > -1) {
                v.setAddress(addresses[pos]);
                arr.add(addresses[pos]);
                SuggestionView.showSuggestions(arr, m2.group(1) + " " + m2.group(2), v);
                return v.getAddress();
            }
        } else if (m3.matches()) {
            int pos = binarySearch(m3.group(1) + " " + m3.group(2));
            if (pos > -1) {
                v.setAddress(addresses[pos]);
                arr.add(addresses[pos]);
                SuggestionView.showSuggestions(arr, m3.group(1) + " " + m3.group(2), v);
                return v.getAddress();
            }
        }

        // Initiate  the for loop that traverses every single word in the string
        for (int i = 0; i < sp.length; i++) {

            // Skip if this part is empty somehow
            if (sp[i].length() == 0) {
                continue;
            }

            // Create search string and append each word from i to sp.length - 1
            String searchString = "";
            for (int j = i; j < sp.length; j++) {
                searchString += (j == i ? "" : " ") + sp[j];
            }

            // Calculate the difference from i to sp.length
            int diff = sp.length - i;

            /**
             * Search through arrays for each combination of the words from sp.length to i.
             * and break loop if something is found. Increment i as well to make sure we don't
             * iterate over the same sp[i] again.
             */
            for (int k = 0; k < diff; k++) {
                if (partialBinarySearch(searchString, 1).size() > 0) {
                    Address obj = partialBinarySearch(searchString, 7).get(0);
                    v.setAddress(obj);
                    i += (diff - k) - 1;
                    suggestionQuery = searchString;
                    break;
                }
                // If this combination doesn't match anything in our arrays then remove one word if possible.
                int indexOfSpace = searchString.lastIndexOf(" ");
                if (indexOfSpace != -1) searchString = searchString.substring(0, indexOfSpace);
            }

        }

        if (suggestionQuery.length() != 0) {
            // Check suggestion type and if it is specified then call the showSuggestions method
            SuggestionView.showSuggestions(partialBinarySearch(suggestionQuery, 7), suggestionQuery, v);
        }
        else {
            v.suggestionViewList.forEach(SuggestionView::hideSuggestion);
        }
        initiateLevenshteinSearch(v, query, 7);
        return v.getAddress();
    }

}
