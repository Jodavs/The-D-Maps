package edu.itu.the_d.map.controller;

import edu.itu.the_d.map.model.Model;
import edu.itu.the_d.map.model.Pin;
import edu.itu.the_d.map.view.MapView;

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.Timer;
import java.util.TimerTask;

/**
 * ViewController for the map
 * <p>
 * Copyright 2016 The-D
 */
public class MapViewController implements KeyListener, MouseListener, MouseMotionListener, MouseWheelListener {
    private static final double VELOCITY_LOWER_LIMIT = 0.08; // Do not apply actions on velocities below this limit
    private static final double VELOCITY_UPPER_LIMIT = 2.4; // Cap the velocity at this limit
    private static final double VELOCITY_DECREASEMENT = 0.009; // Decreasement in velocity per millisecond

    private Model model;
    private MapView mapView;
    private int mx, my;

    // Used to track mouse for smooth panning
    private Long lastTime;
    private Double velocity;
    private Point2D dirUnitVec;
    private Timer timer;
    private boolean newDrag;

    /**
     * Constructer for MapViewController.
     * @param m takes Model as parameter.
     * @param v of type MapView.
     */
    public MapViewController(Model m, MapView v) {
        model = m;
        mapView = v;
        v.addKeyListener(this);
        v.addMouseListener(this);
        v.addMouseMotionListener(this);
        v.addMouseWheelListener(this);
    }

    /**
     * Handles mouse movements in MapView.
     * @param e of type MouseEvent.
     */
    public void mouseMoved(MouseEvent e) {
        double x = e.getX() + mapView.dx - mapView.tile_transform.getTranslateX();
        double y = e.getY() + mapView.dy - mapView.tile_transform.getTranslateY();

        //Show hand cursor if hovering over a pin
        for (Pin pin: model.pinList) {
            Point2D.Double point = new Point2D.Double(0, 0);
            mapView.map_transform.transform(pin.getLocation(), point);
            Point2D.Double f = new Point2D.Double(point.getX() - mapView.dx, point.getY() - mapView.dy);
            Rectangle rect = new Rectangle((int) f.getX() - model.pinXOffset, (int) f.getY() - model.pinYOffset, 56, 82);
            if (rect.contains(new Point((int) x, (int) y))) {
                mapView.getView().getRootPane().setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                break;
            }
            else {
                mapView.getView().getRootPane().setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
            }
        }
    }

    /**
     * Handles MouseEvents if the mouse button is clicked.
     * @param e of type MouseEvent.
     */
    public void mouseClicked(MouseEvent e) {
        double x = e.getX() + mapView.dx - mapView.tile_transform.getTranslateX();
        double y = e.getY() + mapView.dy - mapView.tile_transform.getTranslateY();

        model.pinLocation_nn = new Point2D.Float(0,0);

        //If we clicked on a pin then show the options for that pin
        for (Pin pin: model.pinList) {
            Point2D.Double point = new Point2D.Double(0, 0);
            mapView.map_transform.transform(pin.getLocation(), point);
            Point2D.Double f = new Point2D.Double(point.getX() - mapView.dx, point.getY() - mapView.dy);
            Rectangle rect = new Rectangle((int) f.getX() - model.pinXOffset, (int) f.getY() - model.pinYOffset, 56, 82);
            if (rect.contains(new Point((int) x, (int) y))) {
                mapView.getView().showPin(pin);
                model.dirty();
                return;
            }
        }

        //Create new pin
        Pin pin = model.nearestNeighbor(mapView.inverse(x,y));
        if (pin == null) return;
        model.pinLocation_nn.setLocation(pin.getLocation());
        mapView.getView().showPin(pin);
        model.dirty();
    }

    /**
     * Handles mouseEvents in MapView when the mouse button is pressed.
     * @param e of type MouseEvent.
     */
    public void mousePressed(MouseEvent e) {
        mx = e.getX();
        my = e.getY();
    }

    /**
     * Handles mouseEvents in MapView when the mouse button is released.
     * @param e of type MouseEvent.
     */
    public void mouseReleased(MouseEvent e) {
        // If velocity above lower limit
        if (velocity != null && velocity > VELOCITY_LOWER_LIMIT) {
            newDrag = false;

            // If there's already a timer, kill it.
            if (timer != null) {
                timer.cancel();
                timer.purge();
            }
            // Start a new timer that runs per 1 millisecond
            timer = new Timer();
            timer.schedule(new TimerTask() {
                public void run() {
                    // Pan the tiles by a velocity vector (Unitvector * velocity)
                    mapView.panTiles(velocity * dirUnitVec.getX(), velocity * dirUnitVec.getY());
                    model.dirty();

                    // Decrease velocity by constant
                    velocity -= VELOCITY_DECREASEMENT;

                    // If the velocity has reached the lower limit or there's a new drag, stop the timer.
                    if (velocity < VELOCITY_LOWER_LIMIT || newDrag) timer.cancel();
                }
            }, 0, 1);
        }
    }

    /**
     * Handles MouseEvent in MapView when the mouse is exited.
     * @param e of type MouseEvent.
     */
    public void mouseExited(MouseEvent e) {
    }

    /**
     * Handles MouseEvents in MapView when the mouse is entered.
     * @param e of type MouseEvent.
     */
    public void mouseEntered(MouseEvent e) {
    }

    /**
     * Handles MouseEvent in MapView when the mouseWheel is Moved.
     * @param e of type MouseEvent.
     */
    public void mouseWheelMoved(MouseWheelEvent e) {
        mapView.zoomTiles(Math.pow(1.1, -e.getWheelRotation()), e.getX(), e.getY());
        mapView.zoom(Math.pow(1.1, -e.getWheelRotation()), e.getX(), e.getY());

        model.dirty();
    }

    /**
     * Handles MouseEvent in MapView when the mouse is dragged.
     * @param e of type MouseEvent.
     */
    public void mouseDragged(MouseEvent e) {
        mapView.panTiles(e.getX() - mx, e.getY() - my);
        model.dirty();
        // Get current time
        long now = System.currentTimeMillis();
        // If there is a last time detected
        if (lastTime != null) {
            // Detect a new drag
            newDrag = true;

            // Create point from mx, my
            Point2D before = new Point2D.Double(mx, my);
            // Set after to the coordinates of the event
            Point2D after = new Point2D.Double(e.getX(), e.getY());

            // Calculate vector by the coordinates (after-before)
            Point2D vector = new Point2D.Double(after.getX() - before.getX(), after.getY() - before.getY());
            // Calculate the length of the vector
            double length = Math.sqrt(Math.pow(vector.getX(), 2) + Math.pow(vector.getY(), 2));
            // Calculate the unit vector (vector / |vector|)
            dirUnitVec = new Point2D.Double(vector.getX() / length, vector.getY() / length);

            // Calculate the time spent
            long timeSpent = now - lastTime;
            // Calculate the velocity by distance/time and cap by the upper limit constant
            velocity = Math.min((before.distance(after) / timeSpent), VELOCITY_UPPER_LIMIT);
        }
        lastTime = now;
        mx = e.getX();
        my = e.getY();
    }

    /**
     * Handles event when a key is pressed in MapView.
     * @param e of type KeyEvent.
     */
    public void keyPressed(KeyEvent e) {}

    /**
     * Handles event when a key is released in MapView.
     * @param e of type KeyEvent.
     */
    public void keyReleased(KeyEvent e) {}

    /**
     * Handles event when a key is typed in MapView.
     * @param e of type KeyEvent.
     */
    public void keyTyped(KeyEvent e) {}

}
