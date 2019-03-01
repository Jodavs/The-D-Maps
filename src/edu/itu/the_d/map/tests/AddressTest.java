package edu.itu.the_d.map.tests;

import edu.itu.the_d.map.model.Model;
import edu.itu.the_d.map.model.mapobjects.Address;
import edu.itu.the_d.map.view.MapView;
import edu.itu.the_d.map.view.View;
import junit.framework.TestCase;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * Tests the address search functionality of the program.
 * Copyright 2016 The-D
 */
public class AddressTest extends TestCase {
    private static Model model = Model.createModel("resources/resources/bornholm.osm.xml");
    private static View view = new View(model, new MapView(model));

    private static final boolean PRINT_TEST_OUTPUT = true;

    // For test cases A1-A4
    private static final String TEST_STREET_ONE_WORD = "Almindingsvej 10";
    private static final String TEST_STREET_ONE_WORD_LETTER_MISSING = "Almndingsvej 10";
    private static final String TEST_STREET_ONE_WORD_SWITCHED_LETTERS = "Alminidngsvej 10";
    // Expected output
    private static final String TEST_STREET_ONE_WORD_EXPECTED_OUTPUT = "Almindingsvej";

    // For test cases B1-B3
    private static final String TEST_STREET_TWO_WORDS = "Ole Thulesgade 1";
    private static final String TEST_STREET_TWO_WORDS_LETTER_MISSING = "Ole Thlesgade 1";
    private static final String TEST_STREET_TWO_WORDS_SWITCHED_LETTERS = "Ole Thluesgade 1";
    // Expected output
    private static final String TEST_STREET_TWO_WORDS_EXPECTED_OUTPUT = "Ole Thulesgade";

    // For test cases C1-C4
    private static final String TEST_CITY = "Rønne";
    private static final String TEST_CITY_LETTER_MISSING = "Rnne";
    private static final String TEST_CITY_SWITCHED_LETTERS = "Rønen";
    // Expected output
    private static final String TEST_CITY_EXPECTED_OUTPUT = "Rønne";

    // For test cases D1-D4
    private static final String TEST_ADDR = "Doktorbakken 25 Aakirkeby 3720";
    private static final String TEST_ADDR_LETTER_MISSING = "Doktorbkken 25 Aakirkeby 3720";
    private static final String TEST_ADDR_SWITCHED_LETTERS = "Doktrobakken 25 Aakirkeby 3720";
    // Expected output
    private static final String TEST_ADDR_EXPECTED_OUTPUT_STREET = "Doktorbakken 25";
    private static final String TEST_ADDR_EXPECTED_OUTPUT_CITY = "Aakirkeby";
    private static final String TEST_ADDR_EXPECTED_OUTPUT_POSTCODE = "3720";

    // For test cases E1-E4
    private static final String TEST_POI = "Dagli'Brugsen Pedersker";
    private static final String TEST_POI_LETTER_MISSING = "Dagli'Brugsen Pedersker";
    private static final String TEST_POI_SWITCHED_LETTERS = "Dagli'Brugsen Pedersker";
    // Expected output
    private static final String TEST_POI_EXPECTED_OUTPUT = "Dagli'Brugsen Pedersker";

    // For test cases F1-F3
    private static final String TEST_PARTIAL_ADDR_STREET_HOUSENUMBER = "Doktorbakken";
    private static final String TEST_PARTIAL_ADDR_POSTCODE_STREET = "3720 Doktorbakken";
    private static final String TEST_PARTIAL_ADDR_STREET_CITY = "Aakirkeby Doktorbakken";
    // Expected output
    private static final String TEST_PARTIAL_ADDR_EXPECTED_OUTPUT = "Doktorbakken";

    // For test cases G1-G3
    private static final String TEST_INVALID_ADDR = "Randersvej";
    private static final String TEST_ADDR_NOT_IN_POSTCODE = "3720 Torvegade"; // Torvegade is in 3730
    private static final String TEST_ADDR_NOT_IN_CITY = "Aakirkeby Torvegade"; // Torvegade is in Nexø
    // Expected output
    //private static final String TEST_INVALID_ADDR_EXPECTED_OUTPUT = ""; // Maybe
    private static final String TEST_ADDR_NOT_IN_POSTCODE_EXPECTED_OUTPUT = "Torvegade 10";
    private static final String TEST_ADDR_NOT_IN_CITY_EXPECTED_OUTPUT = "Torvegade 10";

    // For test cases H, I, and J
    private static final String TEST_POSTCODE = "3720";
    private static final String TEST_WRONG_CAPITALIZATION = "dAGLI'brugSen pEDersker";
    private static final String TEST_INVALID_HOUSENUMBER = "Nygade 37";
    // Expected output
    private static final String TEST_POSTCODE_EXPECTED_OUTPUT = "3720";
    private static final String TEST_WRONG_CAPITALIZATION_EXPECTED_OUTPUT = "Dagli'Brugsen Pedersker";
    private static final String TEST_INVALID_HOUSENUMBER_EXPECTED_OUTPUT = "Nygade"; // Can be anything

    // For live search
    private static final int TEST_STREET_LIVE_MIN_CHARS = 9;
    private static final int TEST_CITY_LIVE_MIN_CHARS = 5;
    private static final int TEST_POI_LIVE_MIN_CHARS = 14;


    @Test
    public void testCompletedSearchStrings() {
        Address A1 = getResultFromSearch(TEST_STREET_ONE_WORD);
        printOutput("A1: " + A1);
        assertEquals(true, A1.getStreet().contains(TEST_STREET_ONE_WORD_EXPECTED_OUTPUT));

        Address B1 = getResultFromSearch(TEST_STREET_TWO_WORDS);
        printOutput("B1: " + B1);
        assertEquals(true, B1.getStreet().contains(TEST_STREET_TWO_WORDS_EXPECTED_OUTPUT));

        Address C1 = getResultFromSearch(TEST_CITY);
        printOutput("C1: " + C1);
        assertEquals(C1.getCity(), TEST_CITY_EXPECTED_OUTPUT);

        Address D1 = getResultFromSearch(TEST_ADDR);
        printOutput("D1: " + D1.getStreet() + D1.getCity() + D1.getPostcode());
        assertEquals(D1.getStreet(), TEST_ADDR_EXPECTED_OUTPUT_STREET);
        assertEquals(D1.getCity(), TEST_ADDR_EXPECTED_OUTPUT_CITY);
        assertEquals(D1.getPostcode(), TEST_ADDR_EXPECTED_OUTPUT_POSTCODE);

        Address E1 = getResultFromSearch(TEST_POI);
        printOutput("E1: " + E1);
        assertEquals(E1.getName(), TEST_POI_EXPECTED_OUTPUT);

        Address H = getResultFromSearch(TEST_POSTCODE);
        printOutput("H: " + H.toString());
        assertEquals(H.getPostcode(), TEST_POSTCODE_EXPECTED_OUTPUT);
    }

    @Test
    public void testSpellingErrorsLetterMissing() {
        List<String> A2 = getFirstResultFromLevenshtein(TEST_STREET_ONE_WORD_LETTER_MISSING);
        printOutput("A2: " + A2);
        assertListContainsExpectedString(A2, TEST_STREET_ONE_WORD_EXPECTED_OUTPUT);

        List<String> B2 = getFirstResultFromLevenshtein(TEST_STREET_TWO_WORDS_LETTER_MISSING);
        printOutput("B2: " + B2);
        assertListContainsExpectedString(B2, TEST_STREET_TWO_WORDS_EXPECTED_OUTPUT);

        List<String> C2 = getFirstResultFromLevenshtein(TEST_CITY_LETTER_MISSING);
        printOutput("C2: " + C2);
        assertListContainsExpectedString(C2, TEST_CITY_EXPECTED_OUTPUT);

        List<String> E3 = getFirstResultFromLevenshtein(TEST_POI_LETTER_MISSING);
        printOutput("E3: " + E3);
        assertListContainsExpectedString(E3, TEST_POI_EXPECTED_OUTPUT);
    }

    @Test
    public void testSpellingErrorsSwitchedLetters() {
        List<String> A3 = getFirstResultFromLevenshtein(TEST_STREET_ONE_WORD_SWITCHED_LETTERS);
        printOutput("A3: " + A3);
        assertListContainsExpectedString(A3, TEST_STREET_ONE_WORD_EXPECTED_OUTPUT);

        List<String> B3 = getFirstResultFromLevenshtein(TEST_STREET_TWO_WORDS_SWITCHED_LETTERS);
        printOutput("B3: " + B3);
        assertListContainsExpectedString(B3, TEST_STREET_TWO_WORDS_EXPECTED_OUTPUT);

        List<String> C3 = getFirstResultFromLevenshtein(TEST_CITY_SWITCHED_LETTERS);
        printOutput("C3: " + C3);
        assertListContainsExpectedString(C3, TEST_CITY_EXPECTED_OUTPUT);

        List<String> E4 = getFirstResultFromLevenshtein(TEST_POI_SWITCHED_LETTERS);
        printOutput("E4: " + E4);
        assertListContainsExpectedString(E4, TEST_POI_EXPECTED_OUTPUT);
    }

    @Test
    public void testPartialAddresses() {
        String F1 = getResultFromSearch(TEST_PARTIAL_ADDR_STREET_HOUSENUMBER).toString();
        printOutput("F1: " + F1);
        assertTrue(F1.contains(TEST_PARTIAL_ADDR_EXPECTED_OUTPUT));

        String F2 = getResultFromSearch(TEST_PARTIAL_ADDR_POSTCODE_STREET).toString();
        printOutput("F2: " + F2);
        assertTrue(F2.contains(TEST_PARTIAL_ADDR_EXPECTED_OUTPUT));

        String F3 = getResultFromSearch(TEST_PARTIAL_ADDR_STREET_CITY).toString();
        printOutput("F3: " + F3);
        assertTrue(F3.contains(TEST_PARTIAL_ADDR_EXPECTED_OUTPUT));
    }

    @Test
    public void testLiveSearch() {
        // Create list of the input string lengths
        String[] A4 = new String[TEST_STREET_ONE_WORD.length()];
        String[] C4 = new String[TEST_CITY.length()];
        String[] E4 = new String[TEST_POI.length()];

        // Populate with every substring from empty to the full string (to emulate pressing each key after each other)
        for (int i=1; i<=A4.length; i++) { A4[i-1] = TEST_STREET_ONE_WORD.substring(0, i); }
        for (int i=1; i<=C4.length; i++) { C4[i-1] = TEST_CITY.substring(0, i); }
        for (int i=1; i<=E4.length; i++) { E4[i-1] = TEST_POI.substring(0, i); }

        // Test when the search result gives the expected output
        for (int i=TEST_STREET_LIVE_MIN_CHARS; i<A4.length; i++) {
            System.out.println(getResultFromSearch(A4[i].toString()));
            assertTrue(TEST_STREET_ONE_WORD.contains(getResultFromSearch(A4[i]).toString()));
        }
        for (int i=TEST_CITY_LIVE_MIN_CHARS; i<C4.length; i++) {
            assertTrue(TEST_CITY.contains(getResultFromSearch(C4[i]).toString()));
        }
        for (int i=TEST_POI_LIVE_MIN_CHARS; i<E4.length; i++) {
            assertTrue(TEST_POI.contains(getResultFromSearch(E4[i]).toString()));
        }
    }

    @Test
    public void testInvalidAddresses() {
        Address G1 = getResultFromSearch(TEST_INVALID_ADDR);
        printOutput("G1: " + G1);
        assertNull(G1.toString());

        Address G2 = getResultFromSearch(TEST_ADDR_NOT_IN_POSTCODE);
        printOutput("G2: " + G2);
        assertEquals(G2.getStreet(), TEST_ADDR_NOT_IN_POSTCODE_EXPECTED_OUTPUT);

        Address G3 = getResultFromSearch(TEST_ADDR_NOT_IN_CITY);
        printOutput("G3: " + G3);
        assertEquals(G3.getStreet(), TEST_ADDR_NOT_IN_CITY_EXPECTED_OUTPUT);

        String J = getResultFromSearch(TEST_INVALID_HOUSENUMBER).toString();
        printOutput("J: " + J);
        assertEquals(true, J.contains(TEST_INVALID_HOUSENUMBER_EXPECTED_OUTPUT));
    }

    @Test
    public void setTestWrongCapitalization() {
        String I = getResultFromSearch(TEST_WRONG_CAPITALIZATION).toString();
        printOutput("I: " + I);
        assertEquals(true, I.contains(TEST_WRONG_CAPITALIZATION_EXPECTED_OUTPUT));
    }

    private void printOutput(String s) { if (PRINT_TEST_OUTPUT) System.out.println("AddressTest: " + s);}

    private Address getResultFromSearch(String query) {
        return model.addressSearcher.liveSearch(query, view);
    }
    private List<String> getFirstResultFromLevenshtein(String query) {
        List<Address> add = model.addressSearcher.levenshteinSearch(query, 10);
        List<String> res = new ArrayList<>();
        for (Address a : add) {
            res.add(a.toString());
        }
        return res;
    }

    private void assertListContainsExpectedString(List<String> searchResults, String expectedOutput) {
        boolean success = false;
        for (String s : searchResults) {
            if (s.contains(expectedOutput)) success = true;
        }
        assertTrue(success);
    }

}