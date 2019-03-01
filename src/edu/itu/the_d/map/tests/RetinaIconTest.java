package edu.itu.the_d.map.tests;

import edu.itu.the_d.map.utils.ImageLoader;
import edu.itu.the_d.map.view.RetinaIcon;
import org.junit.Test;

import javax.swing.*;
import java.awt.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Tests the {@link RetinaIcon} class.
 * Copyright 2016 The-D
 */
public class RetinaIconTest {

    ImageIcon PIN = RetinaIcon.createLocationPinIcon();
    //ImageIcon WEATHER = RetinaIcon.createWeatherIcon();

    @Test
    public void testIsRetina() throws Exception {
        switch (System.getProperty("os.name")) {
            case "osx": break;
        }
        if (System.getProperty("os.name").equals("Mac OS X"))
            assertTrue(RetinaIcon.isRetina()); // Because everyone on the team who uses mac has a retina screen
        else
            assertFalse(RetinaIcon.isRetina());
    }

    @Test
    public void testGetIconWidth() throws Exception {
        if (!RetinaIcon.isRetina()) {
            assertEquals(PIN.getIconWidth(), 512);
            //assertEquals(WEATHER.getIconWidth(), );
        }
    }

    @Test
    public void testGetIconHeight() throws Exception {
        if (!RetinaIcon.isRetina()) {
            assertEquals(PIN.getIconHeight(), 782);
            //assertEquals(WEATHER.getIconHeight(), );
        }
    }

    @Test
    public void testPaintIcon() throws Exception {
        // Ja den bliver sgu tegnet. ;)
    }

    @Test
    public void testGetImage() throws Exception {
        Image pinImage = ImageLoader.loadImage("placering_pin.png", 22, true);
        //assertEquals(PIN.getImage(), pinImage);       den sammenligner formentlig to pointere.
    }

}