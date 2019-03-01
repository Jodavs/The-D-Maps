package edu.itu.the_d.map.view;

import edu.itu.the_d.map.model.Model;
import edu.itu.the_d.map.model.Pin;
import edu.itu.the_d.map.model.mapobjects.Address;
import edu.itu.the_d.map.model.mapobjects.ColorTheme;
import edu.itu.the_d.map.model.mapobjects.RoadType;
import edu.itu.the_d.map.utils.Haversine;
import edu.itu.the_d.map.utils.ImageLoader;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Point2D;
import java.util.*;
import java.util.List;
import java.util.Timer;

/**
 * View for showing the map itself, including pins and routes.
 * <p>
 * Copyright 2016 The-D
 **/
public class MapView extends JComponent implements Observer, ActionListener {
	public static final int TX = 5;
	public static final int TY = 5;
	private static final int MIN_ZOOM_LEVEL = 200;
	private static final int MAX_ZOOM_LEVEL = 240000;
	public AffineTransform map_transform;
	public AffineTransform tile_transform;
	public AffineTransform zoom_transform;
	public int tile_width, tile_height;
	public double dx, dy = 0;
	public int w, h;
	public int outerW, outerH;
	private Model m;
	private MapTileFactory tileFactory;
	private Timer zoomTimer = new Timer();
	private View view;
	private double moveX = 0;
	private double moveY = 0;

	/**
	 * Creates a map view with a new tile factory.
	 *
	 * @param m
	 */
	public MapView(Model m) {
		// Set the model reference
		this.m = m;
		// Set the initial map view width and height to the constants defined in View
		this.w = View.GUI_WIDTH;
		this.h = View.GUI_HEIGHT;

		// Set the width and height of the initial map tiles
		this.outerW = w + w / 2;
		this.outerH = h + h / 2;

		// Add this as an observer to the model
		m.addObserver(this);

		tile_transform = new AffineTransform();
		map_transform = new AffineTransform();

		// Pan and zoom to the initial location of the map
		pan(-m.getMinlon(), -m.getMaxlat());
		zoom(2500 / Math.max(m.getMaxlon() - m.getMinlon(), m.getMaxlat() - m.getMinlat()), 0, 0);


		// Translate the tile transform to compensate for the extra width and height of the initial tile draw area
		tile_transform.preConcatenate(AffineTransform.getTranslateInstance(-w / 4, -h / 4));


		// Create a new map tile factory with
		tileFactory = new MapTileFactory(this, m);
		// Produce a set of tiles covering the view bounds (and a w/4 or h/4 buffer outside the screen edge)
		tileFactory.produceTilesForBounds();
		// Repaint the view to display the changes
		repaint();
	}

	/**
	 * Method for getting the view reference.
	 *
	 * @return a reference to the view
	 */
	public View getView() {
		return view;
	}

	/**
	 * Method for setting the view reference. This is necessary because both views need a reference to each other
	 *
	 * @param view
	 */
	public void setView(View view) {
		this.view = view;
	}

	/**
	 * Sets the width of the map view
	 *
	 * @param width
	 */
	public void setWidth(int width) {
		this.w = width;
	}

	/**
	 * Sets the height of the map view
	 *
	 * @param height
	 */
	public void setHeight(int height) {
		this.w = height;
	}

	/**
	 * The paint method is responsible for painting the map itself, using tiles, as well as location pins and routes.
	 *
	 * @param _g
	 */
	@Override
	public void paint(Graphics _g) {
		Graphics2D g = (Graphics2D) _g;

		// Get the zoomlevel
		double zoomLevel = getZoomLevel();
		// Make sure every road that scales on zoomlevel gets the new updated zoomlevel
		RoadType.setZoomLevel((float) zoomLevel);

		// Set the transform of the graphics to use tile_transform
		g.setTransform(tile_transform);
		// Then request the tileFactory to draw all it's tiles on the graphics
		tileFactory.drawTiles(g);

		// Create a copy of the map_transform
		AffineTransform translatedMapTransform = new AffineTransform(map_transform);
		// And pan it by the tile_trasforms x and y coordinates. By this the route below can be drawn on top of the tiles
		translatedMapTransform.preConcatenate(AffineTransform.getTranslateInstance(tile_transform.getTranslateX(), tile_transform.getTranslateY()));
		g.setTransform(translatedMapTransform);

		// Draw route if any given
		if (m.getRoutePath() != null) {
			// Set on antialiasing for the route
			g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			// Set the stroke of the path. It scales on the zoomLevel, so it will be bigger when one zooms out
			g.setStroke(new BasicStroke(0.000072f * (float) Math.max(1, Math.min(72000 / zoomLevel, 6)), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
			g.setColor(Color.RED);
			// Finally draw it by getting it from the model
			g.draw(m.getRoutePath());
		}

		// Change the transform back to the tile_transform
		g.setTransform(tile_transform);


		//Calculate point and draw nearest neighbor pin
		Point2D.Double point = new Point2D.Double(0, 0);
		map_transform.transform(m.pinLocation_nn, point);
		Point2D.Double f = new Point2D.Double(point.getX(), point.getY());
		g.drawImage(m.pin_nn_image, (int) f.getX() - m.pinXOffset, (int) f.getY() - m.pinYOffset, this);
		//Calculate point and draw navigation navigate to pin
		map_transform.transform(m.pinLocation_to, point);
		f = new Point2D.Double(point.getX(), point.getY());
		g.drawImage(m.pin_to_image, (int) f.getX() - m.pinXOffset, (int) f.getY() - m.pinYOffset, this);
		//Calculate point and draw navigation navigate from pin
		map_transform.transform(m.pinLocation_from, point);
		f = new Point2D.Double(point.getX(), point.getY());
		g.drawImage(m.pin_from_image, (int) f.getX() - m.pinXOffset, (int) f.getY() - m.pinYOffset, this);

		for (Pin pin : m.pinList) {
			//Calculate point and draw each saved pin
			map_transform.transform(pin.getLocation(), point);
			f = new Point2D.Double(point.getX() - dx, point.getY() - dy);
			g.drawImage(m.pin_nn_image, (int) f.getX() - m.pinXOffset, (int) f.getY() - m.pinYOffset, this);
		}

		// For smooth antialiased fonts
		g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

		//Set font and color
		g.setFont(m.primaryFont);
		if (ColorTheme.themeCode == 2) {
			g.setColor(Color.WHITE);
		} else {
			g.setColor(new Color(0x333333));
		}
		Font font = g.getFont();

		// Get a list over all the cities
		ArrayList<Address> l = m.addressSearcher.getCities();

		if (zoomLevel > 7000 && l.size() > 4) {
			//If zoom level is very high then draw all city names
			for (Address addr : l.subList(4, l.size())) drawAddressAsString(g, addr);
		} else if (zoomLevel > 1500 && l.size() > 4) {
			//If zoom level is low then draw small cities with a smaller font size
			Font smallerSizeFont = font.deriveFont(font.getSize() * 0.75f);
			g.setFont(new Font(font.getName(), Font.PLAIN, smallerSizeFont.getSize()));
			for (Address addr : l.subList(4, (l.size() >= 50 ? l.size() / 10 : l.size()))) drawAddressAsString(g, addr);
		}

		//Draw 4 biggest citites with large font
		g.setFont(m.primaryFont);
		List<Address> tenPercent = l.subList(0, (l.size() > 4 ? 4 : l.size()));
		for (Address addr : tenPercent) drawAddressAsString(g, addr);

		//Clear affine transform
		g.setTransform(new AffineTransform());

		//Calculate  distance in meters from point 0,0 to point 100,0 in screen coordinates
		double dist = Haversine.distanceInMeters(inverse(0, 0), inverse(100, 0));

		//Set to kilometers if distance > 1000 meters
		String strDist;
		if (dist >= 1000) strDist = String.valueOf((int) (dist / 1000)) + "km";
		else strDist = String.valueOf((int) dist) + "m";

		g.setColor(new Color(0x4A90E2));
		int xPos = w - 200;
		int yPos = h - 47;
		//Draw stock image
		g.drawImage(ImageLoader.loadImage("stock.png", 100), xPos, yPos, this);
		//Draw the string
		g.drawString(strDist, xPos + 50 - (g.getFontMetrics().stringWidth(strDist) / 2), h - 32);
	}

	/**
	 * Draws an address to the specified graphics context.
	 *
	 * @param g    the graphics context to draw to
	 * @param addr the address to draw
	 */
	private void drawAddressAsString(Graphics2D g, Address addr) {
		Point2D.Double point = new Point2D.Double(0, 0);
		map_transform.transform(addr.getPoint(), point);
		Point2D.Double f = new Point2D.Double(point.getX() - dx, point.getY() - dy);
		if (!g.getClipBounds().contains(f.getX(), f.getY())) return;
		g.drawString(addr.getName(), (int) f.getX() - (g.getFontMetrics().stringWidth(addr.getName()) / 2), (int) f.getY());
	}

	/**
	 * Get the inverse transform of a point defined by the input parameters. This essentially "converts" screen coordinates
	 * to map coordinates.
	 *
	 * @param x
	 * @param y
	 * @return a point corresponding to a coordinate on the map
	 */
	public Point2D inverse(double x, double y) {
		try {
			return map_transform.inverseTransform(new Point2D.Double(x, y), null);
		} catch (NoninvertibleTransformException ex) {
			throw new RuntimeException(ex);
		}
	}

	public double getZoomLevel() {
		return map_transform.getScaleX();
	}

	/**
	 * Checks if the current map zoom level is within the bounds specified by the constants {@link #MAX_ZOOM_LEVEL} and
	 * {@link #MIN_ZOOM_LEVEL}
	 *
	 * @param s the direction of the current change in zoom level. This is directly passed from the {@link #zoom(double, double, double)}
	 *          and {@link #zoomTiles(double, double, double)} methods s parameter.
	 * @return a boolean specifying whether the zoom is valid
	 */
	private boolean isValidZoom(double s) {
		return (s > 1 && map_transform.getScaleX() <= MAX_ZOOM_LEVEL) ||
				(s < 1 && map_transform.getScaleX() >= MIN_ZOOM_LEVEL);
	}

	/**
	 * Zooms the tiles by the given factor around the mouse coordinates.
	 *
	 * @param s  The factor to zoom by.
	 * @param cx The x-coordinate of the mouse.
	 * @param cy The y-coordinate of the mouse.
	 */
	public void zoomTiles(double s, double cx, double cy) {
		// Return if trying to zoom beyond the valid bounds
		if (!isValidZoom(s)) return;

		// Set the zoom transform if it's not already set.
		// This is a separate transform used only on the temporally zoomList (in MapTileFactory) when zooming.
		if (zoom_transform == null) {
			zoom_transform = new AffineTransform(tile_transform);
			tileFactory.setZoomTransform(zoom_transform);
		}

		// Pan so the mouse coordinates becomes 0,0
		zoom_transform.preConcatenate(AffineTransform.getTranslateInstance(-cx, -cy)); // Pan
		// Zoom around the 0,0 coordinate
		zoom_transform.preConcatenate(AffineTransform.getScaleInstance(s, s)); // Scale
		// Pan back to the original position
		zoom_transform.preConcatenate(AffineTransform.getTranslateInstance(cx, cy)); // Pan

		// Trash all the current tiles, but save the current tiles to the zoomList, so they can be drawn while drawing new tiles on top of them.
		tileFactory.trashCache(true);

		// Cancel the timer (read below) if already started
		zoomTimer.cancel();
		zoomTimer.purge();
		zoomTimer = new Timer();
		// Schedule a new timer to execute in 80 milliseconds
		// This basically ensures that if you spam the mousewheel, it will not produce tiles untill you stop for at least 80 milliseconds.
		zoomTimer.schedule(new TimerTask() {
			public void run() {
				// Reset the tile positions
				resetTilePosition();
				// And produce new tiles for the View boundaries.
				tileFactory.produceTilesForBounds();
			}
		}, 80);
	}

	/**
	 * Reset position of the tiles by creating a new tile_transform and panning it a fourth of the width to the left and a fourth of the height up.
	 */
	private void resetTilePosition() {
		// To compensate that we're resetting the tile_transform, the map_transform must be moved by the saved moveX and moveY values
		// that have been stored every time the tiles have been panned.
		pan(moveX, moveY);
		// Reset the moveX and moveY as they have now been compensated for
		moveX = 0;
		moveY = 0;

		// Reset the tileTransform by creating a new
		tile_transform = new AffineTransform();
		// and moving it a fourth of the width to the left and a fourth of the height up
		tile_transform.preConcatenate(AffineTransform.getTranslateInstance(-view.getContentPane().getWidth() / 4, -view.getContentPane().getHeight() / 4));
	}

	/**
	 * Get the bounds of the view (this includes the border around it)
	 *
	 * @return
	 */
	public Bounds getViewBounds() {
		return new Bounds(0, 0, outerW, outerH);
	}

	/**
	 * Scales the map transform by the given factor. The zoom is limited, so one cannot zoom in too far nor out too far.
	 *
	 * @param s  The scale to append to the transforms.
	 * @param cx The x coordinate to zoom by.
	 * @param cy The y coordinate to zoom by.
	 * @see MapView#isValidZoom(double)
	 */
	public void zoom(double s, double cx, double cy) {
		// If the zoom is below the minimum zoom or above the maximum zoom, just return and do nothing
		if (!isValidZoom(s)) return;

		// Pan so the mouse coordinates becomes 0,0
		pan(-cx + tile_transform.getTranslateX(), -cy + tile_transform.getTranslateY());
		// Now zoom by the factor s around the 0,0
		map_transform.preConcatenate(AffineTransform.getScaleInstance(s, s));
		// Pan back to the x and y where the mouse were
		pan(cx - tile_transform.getTranslateX(), cy - tile_transform.getTranslateY());
	}

	/**
	 * Pan the tiles by the given x and y coordinates. Also checks if there should be drawn new tiles:
	 * If the view has been panned to the left, checks if any tiles to the left of the view should be repainted.
	 * The same happends if we pan up, right or down. Hence tiles that are not yet drawn, will be drawn again.
	 *
	 * @param _dx The amount to translate the x-axis.
	 * @param _dy The amount to translate the y-axis.
	 * @see MapTileFactory
	 */
	public void panTiles(double _dx, double _dy) {
		// Round up the delta x to the nearest integer. This ensures that even minor pans in a direction will be detected.
		int dx = (int) Math.ceil(_dx);
		// Same applies for delta y.
		int dy = (int) Math.ceil(_dy);

		// Start by setting x to 0;
		int x = 0;
		Bounds xBounds = null;

		// If any panning in the x-axis is detected
		if (dx != 0) {
			// Moving left
			if (dx > 0) {
				// Go to the left of the tile_transform. Go tile_width more to the left. Go dx to the right (dx is positive)
				x += -tile_transform.getTranslateX() - tile_width - dx;
			}
			// Moving Right
			else if (dx < 0) {
				// Go to the x coordinate and add the width. This brings us to the right of the view boundaries.
				x += -tile_transform.getTranslateX() + outerW;
			}

			// Create the bounds in the x direction based on the x value calculated above.
			// The height of these boundaries are equal to outerH + two tile heights. These two extra tiles checks the corners of the view boundaries.
			xBounds = new Bounds(x, (int) -tile_transform.getTranslateY() - tile_height, Math.abs(dx), outerH + (2 * tile_height));

		}


		// Repeat the whole thing for up and down panning
		int y = 0;
		Bounds yBounds = null;

		if (dy != 0) {
			// Moving up
			if (dy > 0) {
				y += -tile_transform.getTranslateY() - tile_height - dy;
			}
			// Moving down
			else if (dy < 0) {
				y += -tile_transform.getTranslateY() + outerH;
			}

			// Here we just check two extra tiles in the x direction
			yBounds = new Bounds((int) -tile_transform.getTranslateX() - tile_width, y, outerW + (2 * tile_width), Math.abs(dy));
		}

		// Make the factory produce tiles for the given bounds. It will snap the bounds to tiles to see which tiles the bounds corresponds to
		tileFactory.produceTilesForBounds(xBounds, yBounds);

		// Add to the moveX and moveY the value that we have panned in these directions.
		moveX += _dx;
		moveY += _dy;

		// Actually pan the tile_transform
		tile_transform.preConcatenate(AffineTransform.getTranslateInstance(_dx, _dy));
		// If there's a zoom_transform active, also make sure we pan that (So we also pan the zoomed in tiles)
		if (zoom_transform != null) {
			zoom_transform.preConcatenate(AffineTransform.getTranslateInstance(_dx, _dy));
		}
	}

	/**
	 * Pans the {@link MapView#map_transform mapTransform} with the given dx and dy coordinates.
	 *
	 * @param dx The amount to translate the x-axis.
	 * @param dy The amount to translate the y-axis.
	 */
	public void pan(double dx, double dy) {
		map_transform.preConcatenate(AffineTransform.getTranslateInstance(dx, dy));
	}

	/**
	 * A method that should be called whenever the View has been resized. This will make sure the tiles' sizes are also updated upon resizing view.
	 *
	 * @param width  The new width of the View.
	 * @param height The new height of the View.
	 */
	public void resizeView(int width, int height) {
		w = width;
		h = height;
		tile_width = (w + w / 2) / TX;
		tile_height = (h + h / 2) / TY;
		this.outerW = w + w / 2;
		this.outerH = h + h / 2;
		tileFactory.setNewBoundaries(outerW, outerH);
	}

	/**
	 * Trash all current tiles and redraw them for the View boundaries.
	 */
	public void redrawAllTiles() {
		// Trash the tiles and do not save anything for a zoom image
		tileFactory.trashCache(false);
		// Reset the tiles positioning
		resetTilePosition();
		// Produce new tiles for the view boundaries
		tileFactory.produceTilesForBounds();
	}

	/**
	 * Whenever an observed object changes state, this method is called and the view is repainted.
	 *
	 * @param obs The observer obj.
	 * @param obj The object given with the update.
	 */
	public void update(Observable obs, Object obj) {
		repaint();
	}

	/**
	 * This method does nothing, but if the class does not implement ActionListener, the View will not register the key strokes
	 *
	 * @param e The ActionEvent
	 */
	@Override
	public void actionPerformed(ActionEvent e) {
	}
}