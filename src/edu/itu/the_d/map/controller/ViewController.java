package edu.itu.the_d.map.controller;

import edu.itu.the_d.map.Main;
import edu.itu.the_d.map.datastructures.Dijkstra;
import edu.itu.the_d.map.datastructures.NoPathFoundException;
import edu.itu.the_d.map.datastructures.VehicleType;
import edu.itu.the_d.map.model.Model;
import edu.itu.the_d.map.model.mapobjects.Address;
import edu.itu.the_d.map.model.mapobjects.ColorTheme;
import edu.itu.the_d.map.utils.Debugger;
import edu.itu.the_d.map.utils.SpringAnimation;
import edu.itu.the_d.map.utils.User;
import edu.itu.the_d.map.view.MapTileFactory;
import edu.itu.the_d.map.view.View;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.event.*;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.File;

/**
 * Controller class for the View class
 * <p>
 * Copyright 2016 The-D
 */
public class ViewController implements KeyListener {
	// Model reference
	private Model m;
	// View reference
	private View view;
	// Reference to this. Used by Actions implementation
	private ViewController vc = this;
	// Separate thread for Dijkstra
	private Thread dijkstraThread;

	/**
	 * Creates the necessary Key- and ActionListeners for the View Class
	 * @param model of Model
	 * @param view of View
	 */
	public ViewController(Model model, View view) {
		this.m = model;
		this.view = view;

		// Key listener search text field
		KeyListener pressed_search = new KeyListener() {
			@Override
			public void keyTyped(KeyEvent e) {vc.keyTyped(e);}
			@Override
			public void keyPressed(KeyEvent e) {
				// Set search field as active text field
				view.tf_active = view.tf_search;
				view.active_tf = 0;
				vc.keyPressed(e);
				//view.hidePin();
			}
			@Override
			public void keyReleased(KeyEvent e) {vc.keyReleased(e);}
		};

		// Key listener for navigation from text field
		KeyListener pressed_from = new KeyListener() {
			@Override
			public void keyTyped(KeyEvent e) {vc.keyTyped(e);}
			@Override
			public void keyPressed(KeyEvent e) {
				// Set navigation navigate from as active text field
				view.tf_active = view.tf_from;
				view.active_tf = 1;
				vc.keyPressed(e);
			}
			@Override
			public void keyReleased(KeyEvent e) {vc.keyReleased(e);}
		};


		// Key listener for navigation to text field
		KeyListener pressed_to = new KeyListener() {
			@Override
			public void keyTyped(KeyEvent e) {
				vc.keyTyped(e);
			}
			@Override
			public void keyPressed(KeyEvent e) {
				// Set navigation navigate to as active text field
				view.tf_active = view.tf_to;
				view.active_tf = 2;
				vc.keyPressed(e);
			}
			@Override
			public void keyReleased(KeyEvent e) {
				vc.keyReleased(e);
			}
		};

		// Action handler for navigation navigate to text field
		Action actionNavigate = new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e) {
				doPreSearch();
				navigateToFoundAddresses();
			}
		};

		// Action handler for navigation navigate from text field
		Action actionMoveFrom = new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e) {
				doPreSearch();
				view.tf_to.requestFocus();
				ViewController.this.view.tf_active = view.tf_to;
				ViewController.this.view.active_tf = 2;
			}
		};

		// Action handler for search text field
		Action actionMoveTo = new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e) {
				panToFoundAddress();
			}
		};

		// Add keylistener
		view.addKeyListener(this);

		// Add action listener to the "navigate to" search field
		view.tf_to.addActionListener(actionNavigate);

		// Add action listener to the "search" search field
		view.tf_search.addActionListener(actionMoveTo);

		// Add action listener for the navigation button
		view.b_directions.addActionListener(e -> view.showNavigationPanel());

		// Add action listener to the search button
		view.b_search.addActionListener(e -> panToFoundAddress());

		// Add action listener to the zoom out button
		view.b_zoomOut.addActionListener(e -> {
			view.mapView.zoomTiles(Math.pow(1.1, -1), view.getWidth() / 2, view.getHeight() / 2);
			view.mapView.zoom(Math.pow(1.1, -1), view.getWidth() / 2, view.getHeight() / 2);
			model.dirty();
		});

		// Add action listener to the zoom in button
		view.b_zoomIn.addActionListener(e -> {
			view.mapView.zoomTiles(Math.pow(1.1, 1), view.getWidth() / 2, view.getHeight() / 2);
			view.mapView.zoom(Math.pow(1.1, 1), view.getWidth() / 2, view.getHeight() / 2);
			model.dirty();
		});

		// Add action listener to the navigation select fastest route type button
		view.b_fastest_route.addActionListener(e -> {
				view.setFastestRouteBtn();
				view.b_car.setClicked(view.b_walk, view.b_bike);
		});

		// Add action listener to the navigation select shortest route type button
		view.b_shortest_route.addActionListener(e -> view.setShortestRouteBtn());

		// Add action listener to the navigation select walk type button
		view.b_walk.addActionListener(e -> {
			if(view.b_shortest_route.isSelected()) {
				Dijkstra.setVehicleType(VehicleType.WALK);
				view.b_walk.setClicked(view.b_bike, view.b_car);
			}
			else {
				view.setShortestRouteBtn();
				Dijkstra.setVehicleType(VehicleType.WALK);
				view.b_walk.setClicked(view.b_bike, view.b_car);
			}
		});

		view.b_bike.addActionListener(e -> {
			if(view.b_shortest_route.isSelected()) {
				Dijkstra.setVehicleType(VehicleType.BICYCLE);
				view.b_bike.setClicked(view.b_walk, view.b_car);
			}
			else {
				view.setShortestRouteBtn();
				Dijkstra.setVehicleType(VehicleType.BICYCLE);
				view.b_bike.setClicked(view.b_walk, view.b_car);
			}
		});

		// Add action listener to the navigation select car type button
		view.b_car.addActionListener(e -> {
			Dijkstra.setVehicleType(VehicleType.CAR);
			view.b_car.setClicked(view.b_walk, view.b_bike);
		});

		// Add action listener to the navigation text field swap button
		view.b_swap.addActionListener(e -> swapNavigateFields());


		// Add action listener to the close navigation button
		view.b_cross.addActionListener(e -> {
			view.showSearchPanel();
			// Remove dijkstra route
			model.clearRoutePath();
			// Remove pins
			model.pinLocation_from = new Point2D.Float(0,0);
			model.pinLocation_to = new Point2D.Float(0,0);
			SwingUtilities.invokeLater(model::dirty);
		});



		// Add action listener to the open obj file button
		view.btn_open.addActionListener(e -> openL());

		// Add action listener to the save obj file button
		view.btn_save.addActionListener(e -> saveL());

		// Add action listener to the default theme button
		view.btn_theme1.addActionListener(e -> {
			ColorTheme.setDefaultTheme();
			model.setPinImage(0);
			view.mapView.redrawAllTiles();
			model.dirty();
		});

		// Add action listener to the nyan cat theme button
		view.btn_theme2.addActionListener(e -> {
			ColorTheme.setNyanTheme();
			model.setPinImage(2);
			view.mapView.redrawAllTiles();
			model.dirty();
		});

		// Add action listener to the batman theme button
		view.btn_theme3.addActionListener(e -> {
			ColorTheme.setBatmanTheme();
			model.setPinImage(1);
			view.mapView.redrawAllTiles();
			model.dirty();
		});

		// Add document listener to the search text field
		view.tf_search.getDocument().addDocumentListener(new DocListener());

		// Add document listener to the navigation navigate to text field
		view.tf_to.getDocument().addDocumentListener(new DocListener());

		// Add document listener to the navigation navigate from text field
		view.tf_from.getDocument().addDocumentListener(new DocListener());

		// Add action listener to the search text field
		view.tf_search.addActionListener(actionMoveTo);

		// Add key listener to the search text field
		view.tf_search.addKeyListener(pressed_search);

		// Add action listener to the navigation navigate from text field
		view.tf_from.addActionListener(actionMoveFrom);

		// Add key listener to the navigation navigate from text field
		view.tf_from.addKeyListener(pressed_from);

		// Add key listener to the navigation navigate to text field
		view.tf_to.addKeyListener(pressed_to);

		// Add action listener to the close pin view button
		view.b_pinClose.addActionListener(e -> {
			model.pinLocation_nn = new Point2D.Float(0,0);
			view.hidePin();
			view.activePin = null;
			model.pinLocation_nn = new Point2D.Float(0,0);
			SwingUtilities.invokeLater(model::dirty);
		});

		// Add action listener to the delete pin button
		view.b_pinDelete.addActionListener(e -> {
			model.pinList.remove(view.activePin);
			view.hidePin();
			model.pinLocation_nn = new Point2D.Float(0,0);
			SwingUtilities.invokeLater(model::dirty);
		});

		// Add action listener to the save pin button
		view.b_pinSave.addActionListener(e -> {
			if (!model.pinList.contains(view.activePin)) {
				model.pinList.add(view.activePin);
				SwingUtilities.invokeLater(model::dirty);
			}
			view.hidePin();
		});

		// Add action listener to the pin navigate to button
		view.b_pinNavigate.addActionListener(e -> {
			if (view.activePin == null || view.activePin.getName() == null || view.activePin.getName().length() < 1) {
				View.infoBox("Du kan ikke navigere til denne vej", "Rutevejledningsfejl");
				return;
			}
			view.tf_to.setText(view.activePin.getName());
			view.setAddressTo(m.addressSearcher.liveSearch(view.activePin.getName(), view));
			view.address_to.setPoint((Point2D.Float) view.activePin.getLocation());
			view.showNavigationPanelAfterPin();
			SwingUtilities.invokeLater(model::dirty);
		});


		// Add mouse listener to the buttons that are
		// affected by SpringAnimation to fix the visual
		// background bug
		view.btn_settings.addMouseListener(new MListener());
		view.btn_briller.addMouseListener(new MListener());
		view.btn_placering.addMouseListener(new MListener());
		view.btn_open.addMouseListener(new MListener());
		view.btn_save.addMouseListener(new MListener());
		view.btn_theme1.addMouseListener(new MListener());
		view.btn_theme2.addMouseListener(new MListener());
		view.btn_theme3.addMouseListener(new MListener());



		// Add action listener to the drop down settings button
		view.btn_settings.addActionListener(e -> {
			if (view.btn_briller.isFoldedOut()) {
				if (view.btn_theme1.isFoldedOut()) {
					// Pull theme buttons in
					SpringAnimation.animateGroup(SpringAnimation.DIR_RIGHT, view.btn_theme1.getWidth(), model, view.btn_theme1, view.btn_theme2, view.btn_theme3);
				}
				// Pull buttons up
				SpringAnimation.animateGroup(SpringAnimation.DIR_UP, view.btn_briller.getHeight(), model, view.btn_briller, view.btn_placering, view.btn_save, view.btn_open);
			} else // Pull buttons down
				SpringAnimation.animateGroup(SpringAnimation.DIR_DOWN, view.btn_briller.getHeight(), model, view.btn_briller, view.btn_placering, view.btn_save, view.btn_open);
			SwingUtilities.invokeLater(model::dirty);
		});

		// Add action listener to the drop down theme buttons
		view.btn_briller.addActionListener(e -> {
			if (view.btn_theme1.isFoldedOut()) {
				// Pull theme buttons in
				SpringAnimation.animateGroup(SpringAnimation.DIR_RIGHT, view.btn_theme1.getWidth(), m, view.btn_theme1, view.btn_theme2, view.btn_theme3);
			} else {
				// Pull theme buttons out
				SpringAnimation.animateGroup(SpringAnimation.DIR_LEFT, view.btn_theme1.getWidth(), m, view.btn_theme1, view.btn_theme2, view.btn_theme3);
			}
			SwingUtilities.invokeLater(model::dirty);
		});

		// Add action listener to the drop down "pan to your location" button
		view.btn_placering.addActionListener(e -> {
			// If location couldn't be loaded then return
			if (m.geoLocation.getWeatherIcon() == null) return;
			// Get middle of screen
			double scrX = view.mapView.w / 2 + view.mapView.dx - view.mapView.tile_transform.getTranslateX();
			double scrY = view.mapView.h / 2 + view.mapView.dy - view.mapView.tile_transform.getTranslateY();
			// Calculate point to pan to
			Point2D cp = view.mapView.inverse(scrX, scrY);
			// Pan to the users location
			view.mapView.map_transform.translate(cp.getX()-m.geoLocation.getLongitude()*model.lonfactor, cp.getY()-(-m.geoLocation.getLatitude()));
			// Update tiles
			view.mapView.redrawAllTiles();
			SwingUtilities.invokeLater(model::dirty);
		});

	} //  => End of constructor

	/**
	 * Handles key pressed event in View
	 * @param e KeyEvent
	 */
	@Override
	public void keyPressed(KeyEvent e) {
		int keyCode = e.getKeyCode();
		switch (keyCode) {
			case KeyEvent.VK_ESCAPE:
				view.showSearchPanel();
				// Remove dijkstra route
				m.clearRoutePath();
				// Remove pins
				m.pinLocation_from = new Point2D.Float(0,0);
				m.pinLocation_to = new Point2D.Float(0,0);
				SwingUtilities.invokeLater(m::dirty);
			case KeyEvent.VK_P:
				if (e.isControlDown()) { // Debugging mode
					MapTileFactory.setDebug(!MapTileFactory.getDebug());
					view.mapView.redrawAllTiles();
				}
				break;
			case KeyEvent.VK_UP:
				// Set arrow status
				view.isUsingArrows = true;
				// If user has used the arrow keys to get back to the textfield
				if (view.arrowIndex <= 0) {
					view.setAllSuggestionsInactive();
					view.isUsingArrows = false;
					view.arrowIndex = -1;
					e.consume();
					return;
				}
				// Check if this suggestion is visible (if it's used).
				if (view.suggestionViewList.get(view.arrowIndex - 1).isVisible()) {
					// Decrement arrowIndex
					if (view.arrowIndex > 0) view.arrowIndex--;
					// Set the text from this suggestion
					view.tf_active.setText(view.suggestionViewList.get(view.arrowIndex).getNoHTML());
					// Set caret position to end of the new string so user is rdy to type
					view.tf_active.setCaretPosition(view.tf_active.getText().length());
					// Set all other suggestions inactive
					view.setAllSuggestionsInactive();
					// Highlight suggestion
					view.suggestionViewList.get(view.arrowIndex).setActiveColor();
					e.consume(); // Consume the event so default actions doesn't apply.
				}
				break;
			case KeyEvent.VK_DOWN:
				view.isUsingArrows = true;
				if (view.arrowIndex >= 6) return;
				if (view.suggestionViewList.get(view.arrowIndex + 1).isVisible()) {
					if (view.arrowIndex < 7) view.arrowIndex++;
					view.tf_active.setText(view.suggestionViewList.get(view.arrowIndex).getNoHTML());
					view.tf_active.setCaretPosition(view.tf_active.getText().length());
					view.setAllSuggestionsInactive();
					view.suggestionViewList.get(view.arrowIndex).setActiveColor();
					e.consume();
				}
				break;
			case KeyEvent.VK_ENTER:
				// Do nothing if enter is pressed. Note that this doesn't consume the event.
				break;
			default:
				// If any other key than arrow up, down or enter is pressed then set suggetions inactive (changes color to white) and set index to -1 again.
				view.setAllSuggestionsInactive();
				view.isUsingArrows = false;
				view.arrowIndex = -1;
				break;
		}
	}

	/**
	 * Handles key released event in View
	 * @param e KeyEvent
	 */
	@Override
	public void keyReleased(KeyEvent e) {}

	/**
	 * Handles key typed event in View
	 * @param e KeyEvent
	 */
	@Override
	public void keyTyped(KeyEvent e) {}


	/**
	 * Mouse listener made for the buttons that are affected by SpringAnimation.
	 * This was created to fix the visual background bug.
	 */
	private class MListener implements MouseListener {

		@Override
		public void mouseClicked(MouseEvent e) {}

		@Override
		public void mousePressed(MouseEvent e) {}

		@Override
		public void mouseReleased(MouseEvent e) {}

		@Override
		public void mouseEntered(MouseEvent e) {
			SwingUtilities.invokeLater(m::dirty);
		}

		@Override
		public void mouseExited(MouseEvent e) {
			SwingUtilities.invokeLater(m::dirty);
		}
	}


	/**
	 * Document listener class for text fields in view class
	 */
	private class DocListener implements DocumentListener {
		/**
		 * Handles changed update event
		 * @param e DocumentEvent
		 */
		public void changedUpdate(DocumentEvent e) {
			if (view.isUsingArrows) return;
			if (!view.p_suggestions.isVisible()) view.p_suggestions.setVisible(true);
			m.addressSearcher.liveSearch(view.tf_active.getText(), view);
		}
		/**
		 * Handles remove update event
		 * @param e DocumentEvent
		 */
		public void removeUpdate(DocumentEvent e) {
			if (view.isUsingArrows) return;
			if (view.tf_active.getText().length() != 0) m.addressSearcher.liveSearch(view.tf_active.getText(), view);
			else {
				view.p_suggestions.setVisible(false);
				view.clearAddress();
				SwingUtilities.invokeLater(m::dirty);
			}
		}
		/**
		 * Handles insert update event
		 * @param e DocumentEvent
		 */
		public void insertUpdate(DocumentEvent e) {
			if (view.isUsingArrows) return;
			if (!view.p_suggestions.isVisible()) view.p_suggestions.setVisible(true);
			m.addressSearcher.liveSearch(view.tf_active.getText(), view);
		}
	}

	/**
	 * Load OBJ, osm or xml files.
	 */
	public void openL() {
		String fileloc;
		JFileChooser fileChooser = new JFileChooser();
		int rVal = fileChooser.showOpenDialog(view);
		if (rVal == JFileChooser.APPROVE_OPTION) {
			fileloc = fileChooser.getSelectedFile().getName();
			File file = fileChooser.getCurrentDirectory();
			if (fileloc.endsWith(".osm") || fileloc.endsWith(".xml") || fileloc.endsWith(".obj")) {
				String c = file + "/" + fileloc;
				view.setVisible(false);
				view.dispose();

				m.objectMap = null;
				m.addressSearcher = null;
				m.setGraph(null);
				view = null;

				System.gc();

				new Main().recreateWithNewFile(c);
			} else {
				View.infoBox("This is not a valid .osm file", "ERROR");
			}
		}
	}

	/**
	 * Save OBJ file
	 */
	public void saveL() {
		StringBuilder builder = new StringBuilder();
		JFileChooser fileChooser = new JFileChooser();
		int rVal = fileChooser.showSaveDialog(view);
		if (rVal == JFileChooser.APPROVE_OPTION) {
			builder.append(fileChooser.getSelectedFile().getName());
			if (!builder.toString().endsWith(".obj")) {
				builder.append(".obj");
			}
			File file = fileChooser.getCurrentDirectory();
			m.save(file, builder.toString());
			View.infoBox("File saved as: " + builder, "File saved");
		}
	}

	/**
	 * This method handles a navigation request between two selected points.
	 * It draws the directions on the map and shows them in the direction view.
	 */
	private void navigateToFoundAddresses() {
		// If no point to navigate to or from has been set then return
		if (view.getAddressFrom().getPoint() == null || view.getAddressTo().getPoint() == null) {
			View.infoBox("Du skal udfylde begge felter", "Fejl");
			return;
		}
		// Get nearest road ID from point
		long fromID = m.nearestNeighbor(view.getAddressFrom().getPoint(), Dijkstra.getVehicleType()).getId();
		long toID = m.nearestNeighbor(view.getAddressTo().getPoint(), Dijkstra.getVehicleType()).getId();

		Debugger.print(User.JANB, Dijkstra.getVehicleType());
		Debugger.print(User.JANB, "From: "+ fromID +" | to: "+ toID);
		Debugger.print(User.JANB, "From Type: "+ m.objectMap.getRoad(fromID).getType());
		Debugger.print(User.JANB, "To Type: "+ m.objectMap.getRoad(fromID).getType());


		// Set pin positions
		m.pinLocation_from.setLocation(view.getAddressFrom().getPoint());
		m.pinLocation_to.setLocation(view.getAddressTo().getPoint());

		// If the thread is already alive, kill it
		if (dijkstraThread != null && dijkstraThread.isAlive()) dijkstraThread.interrupt();
		// Generate Dijkstra in seperate thread
		dijkstraThread = new Thread(() -> {
			// Turn loading icon on
			view.showBlueLoadingIcon();

			// Generate route
			try {
				view.displayDirections(m.generateDijkstra(fromID, toID));
			} catch (NoPathFoundException e) {
				System.err.println(e.getMessage());
				View.infoBox("Der er ingen vej fra " + view.getAddressFrom().getName() + " til " + view.getAddressTo().getName(), "Rutevejledningsfejl");
			}

			// Calculate pin positions
			double xCoord = view.mapView.w / 2 + view.mapView.dx - view.mapView.tile_transform.getTranslateX();
			double yCoord = view.mapView.h / 2 + view.mapView.dy - view.mapView.tile_transform.getTranslateY();

			// Pan to the the pin
			view.mapView.map_transform.translate(view.mapView.inverse(xCoord, yCoord).getX() - view.getAddressFrom().getPoint().getX(),
					view.mapView.inverse(xCoord, yCoord).getY() - view.getAddressFrom().getPoint().getY());

			// Turn loading icon off
			view.hideBlueLoadingIcon();
			m.dirty();
			view.mapView.redrawAllTiles();
		});
		// Start the thread
		dijkstraThread.start();
	}


	/**
	 * This is the method called when the user pressed the search button or presses enter.
	 * This method is responsible for panning the map view to the place the user has searched for setting the pin location
	 */
	private void panToFoundAddress() {
		view.hidePin();
		// If user has been using arrows to change search query then do live search again
		doPreSearch();

		if (view.getAddressTo().getPoint() == null) return;

		// Calculate current location
		double scrX = view.mapView.w / 2 + view.mapView.dx - view.mapView.tile_transform.getTranslateX();
		double scrY = view.mapView.h / 2 + view.mapView.dy - view.mapView.tile_transform.getTranslateY();

		// Calculate point to pan to
		Point2D cp = view.mapView.inverse(scrX, scrY);

		// Set pin location
		m.pinLocation_nn.setLocation(view.getAddressTo().getPoint());

		// Pan to the location
		view.mapView.map_transform.translate(cp.getX()-view.getAddressTo().getPoint().getX(), cp.getY()-view.getAddressTo().getPoint().getY());

		// Update tiles
		m.dirty();
		view.mapView.redrawAllTiles();
	}


	/**
	 * Perform a live search if enter has been pressed after arrow keys has been used to select a suggestion.
	 */
	private void doPreSearch() {
		if (view.isUsingArrows) {
			m.addressSearcher.liveSearch(view.tf_active.getText(), view);
			view.arrowIndex = -1;
			view.isUsingArrows = false;
			view.setAllSuggestionsInactive();
		}
	}

	/**
	 * Swap content of the navigate from with the navigate to text field.
	 */
	private void swapNavigateFields() {
		view.isUsingArrows = true;
		String from = view.tf_from.getText();
		String to = view.tf_to.getText();
		view.tf_from.setText(to);
		view.tf_to.setText(from);
		Address tmp_address_from = view.getAddressFrom();
		view.setAddressFrom(view.getAddressTo());
		view.setAddressTo(tmp_address_from);
		view.isUsingArrows = false;
		navigateToFoundAddresses();
	}

}
