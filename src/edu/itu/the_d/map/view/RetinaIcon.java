package edu.itu.the_d.map.view;

import edu.itu.the_d.map.utils.ImageLoader;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;
import java.lang.reflect.Field;

/**
 * Class for creating image icons that are Retina (Apple HiDPI) ready.
 * Idea for retina support has been taken from: http://stackoverflow.com/questions/12431148/swing-and-bitmaps-on-retina-displays
 * <p>
 * Copyright 2016 The-D
 */
public class RetinaIcon extends ImageIcon {
    // Initialize fields
    private static boolean isRetina = false;
    private static boolean isSet = false;

    /**
     * Constructor
     *
     * @param bufferedImage
     */
    public RetinaIcon(BufferedImage bufferedImage) {
        super(bufferedImage);
    }


    /**
     * Returns a boolean indicating whether the user is using a Apple device that supports Retina (HiDPI) or not.
     *
     * @return boolean value
     */
    public static boolean isRetina() {
        // Return if field has already been set
        if (isSet) return isRetina;

        // Set fields
        isRetina = false;
        isSet = true;

        // Get the graphicsDevice
        GraphicsDevice graphicsDevice = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();

        // Check if running OSX. This check avoids a strange error on devices running Windows.
        if (!System.getProperty("os.name").equals("Mac OS X")) return false;

        try {
            // Get the field
            Field field = graphicsDevice.getClass().getDeclaredField("scale");

            if (field != null) {
                field.setAccessible(true);
                Object scale = field.get(graphicsDevice);
                if (scale instanceof Integer && (Integer) scale == 2) {
                    isRetina = true;
                }
            }
        } catch (IllegalAccessException | NoSuchFieldException e) {
            e.printStackTrace();
        }
        return isRetina;
    }

    /**
     * Create a retina icon from the placering_pin.png image
     *
     * @return RetinaIcon
     */
    public static RetinaIcon createLocationPinIcon() {
        return new RetinaIcon(ImageLoader.loadImage("placering_pin.png", 22, true));
    }

    /**
     * Get the icon width
     *
     * @return width int
     */
    @Override
    public int getIconWidth() {
        if (isRetina()) return super.getIconWidth() / 2;
        return super.getIconWidth();
    }

    /**
     * Get the icon height
     *
     * @return height int
     */
    @Override
    public int getIconHeight() {
        if (isRetina()) return super.getIconHeight() / 2;
        return super.getIconHeight();
    }

    /**
     * Paint the component
     *
     * @param c Component
     * @param g Graphics
     * @param x int
     * @param y int
     */
    @Override
    public synchronized void paintIcon(Component c, Graphics g, int x, int y) {
        ImageObserver observer = getImageObserver();

        if (observer == null) {
            observer = c;
        }

        Image image = getImage();
        int width = image.getWidth(observer);
        int height = image.getHeight(observer);
        final Graphics2D g2d = (Graphics2D) g.create(x, y, width, height);

        if (isRetina())
            g2d.scale(0.5, 0.5); // Scale to half the size because the image has been loaded twice the size (Retina support magic happens here)

        g2d.drawImage(image, 0, 0, observer);
        g2d.scale(1, 1);
        g2d.dispose();
    }

}