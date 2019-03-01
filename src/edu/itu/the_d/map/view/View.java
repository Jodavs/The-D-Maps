package edu.itu.the_d.map.view;

import edu.itu.the_d.map.datastructures.Dijkstra;
import edu.itu.the_d.map.model.Model;
import edu.itu.the_d.map.model.Pin;
import edu.itu.the_d.map.model.mapobjects.Address;
import edu.itu.the_d.map.utils.*;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.*;
import java.util.Timer;

/**
 * Main view
 * <p>
 * Copyright 2016 The-D
 **/
public class View extends JFrame implements Observer {
	public static final int GUI_P_LEFT_WIDTH = 330;
	static final int GUI_WIDTH = 1400;
	static final int GUI_HEIGHT = 800;
	//GUI styling constants
	private static final int GUI_MARGIN = 20;
	private static final int GUI_P_RIGHT_WIDTH = 246;
	private static final int GUI_P_RIGHT_HEIGHT = 400;
	private static final int GUI_P_RIGHT_BUTTON_Y_OFFSET = 64;
	private static final int GUI_P_RIGHT_BUTTON_SMALL_Y_OFFSET = 48;
	private static final int GUI_P_RIGHT_BUTTON_INNER_OFFSET_X = 168;
	private static final int GUI_P_RIGHT_BUTTON_OUTER_OFFSET_X = 171;
	private static final Dimension GUI_NAVIGATE_BUTTON_SIZE = new Dimension(77, 15);

	// Set serial version
	private static final long serialVersionUID = 1L;

	// Declare address objects for navigation.
	public Address address_to;
	// Mapview reference
	public MapView mapView;
	// Declare and initiate list of suggestion views for searching
	public ArrayList<SuggestionView> suggestionViewList;
	// Declare textfieldd for searching
	public PlaceholderTextField tf_search;
	public PlaceholderTextField tf_to;
	public PlaceholderTextField tf_from;
	public JLayeredPane lp_upper;
	public JPanel p_suggestions;
	public JPanel p_search;
	public JPanel p_navigation;
	// This textfield is a reference to the currently active textfield.
	public PlaceholderTextField tf_active = null;
	/**
	 * This variable stores a int value representing the currently active text field. The possible values are as follows:
	 * 0: Search field is active
	 * 1: Navigate from field is active
	 * 2: Navigate to field is active
	 */
	public int active_tf = 0;
	// This field saves the arrow index when browsing suggestions with the arrow keys.
	public int arrowIndex = -1;
	// This boolean states if the arrows is currently being used by the user.
	public boolean isUsingArrows = false;
	// Various buttons for the GUI
	public ImageButton b_zoomIn, b_zoomOut, b_walk, b_bike, b_car, b_cross,
			b_swap, btn_settings, btn_briller, btn_placering, btn_save,
			btn_theme1, btn_theme2, btn_theme3, btn_open, b_search, b_directions, b_pinSave, b_pinDelete, b_pinClose, b_pinNavigate;
	// These buttons are used to switch between shortest and fastest route for navigating.
	public JButton b_fastest_route, b_shortest_route;
	// This is the pin that is displayed untill the  pin is saved and/or deleted when clicking on the map.
	public Pin activePin;
	private Address address_from;
	// Declare layered panes for the left part of the GUI
	private JLayeredPane lp_mid;
	private JPanel p_pin;
	private JPanel p_directions;
	private JPanel p_lower_second;
	// This label is used by p_know to display search result.
	private JLabel lbl_address, loading_label, blue_loading_label;
	// These labels display the road name and lon+lat for the selected pin.
	private JLabel lbl_road, lbl_degree;
	// Model reference
	private Model m;

	// Timer to keep track of when to repaint on resizing the view
	private Timer resizeTimer = new Timer();

	/**
	 * Primary constructor for View
	 *
	 * @param m  of type Model
	 * @param mv of type MapView
	 */
	public View(Model m, MapView mv) {
		super("The-D Maps");
		//Set icon
		super.setIconImage(ImageLoader.loadImage("theme1.png"));
		// Initialize text fields
		tf_search = new PlaceholderTextField("");
		tf_to = new PlaceholderTextField("");
		tf_from = new PlaceholderTextField("");
		// Initialize suggestionList
		suggestionViewList = new ArrayList<>();
		// Intialize p_second_lower panel
		p_lower_second = new JPanel();
		// Initialize addreses
		address_to = new Address();
		address_from = new Address();
		// Set model
		this.m = m;
		// Construct suggestion views for later use.
		constructSuggestionViews();
		// Set mapview
		this.mapView = mv;
		// Initialize
		init();
		// validate the view
		validate();
		// Set the Theme buttons at correct position
		SwingUtilities.invokeLater(() -> {
			btn_settings.setLocation(GUI_P_RIGHT_BUTTON_INNER_OFFSET_X, GUI_MARGIN);
			btn_briller.setLocation(GUI_P_RIGHT_BUTTON_INNER_OFFSET_X, GUI_MARGIN + GUI_P_RIGHT_BUTTON_Y_OFFSET);
			btn_placering.setLocation(GUI_P_RIGHT_BUTTON_INNER_OFFSET_X, GUI_MARGIN + GUI_P_RIGHT_BUTTON_Y_OFFSET * 2);
			btn_save.setLocation(GUI_P_RIGHT_BUTTON_INNER_OFFSET_X, GUI_MARGIN + GUI_P_RIGHT_BUTTON_Y_OFFSET * 3);
			btn_open.setLocation(GUI_P_RIGHT_BUTTON_INNER_OFFSET_X, GUI_MARGIN + GUI_P_RIGHT_BUTTON_Y_OFFSET * 4);
			btn_theme1.setLocation(GUI_P_RIGHT_BUTTON_OUTER_OFFSET_X, GUI_MARGIN);
			btn_theme2.setLocation(GUI_P_RIGHT_BUTTON_OUTER_OFFSET_X, GUI_MARGIN);
			btn_theme3.setLocation(GUI_P_RIGHT_BUTTON_OUTER_OFFSET_X, GUI_MARGIN);
			SpringAnimation.animateGroup(SpringAnimation.DIR_UP, btn_settings.getHeight(), m, btn_briller, btn_placering, btn_save, btn_open);
			btn_placering.setOpaque(false);
			btn_placering.setBackground(new Color(0, 0, 0, 0));
			btn_settings.setOpaque(false);
			btn_settings.setBackground(new Color(0, 0, 0, 0));
			btn_briller.setOpaque(false);
			btn_briller.setBackground(new Color(0, 0, 0, 0));
			mapView.setOpaque(false);
			mapView.setBackground(new Color(0, 0, 0, 0));
		});
	}

	/**
	 * InfoBox used to show Information to the user on the screen in, such as when
	 * the programmes is done saving an file.
	 *
	 * @param infoMessage of type String.
	 * @param titleBar    of type String.
	 */
	public static void infoBox(String infoMessage, String titleBar) {
		JOptionPane.showMessageDialog(null, infoMessage, titleBar, JOptionPane.INFORMATION_MESSAGE);
	}

	/**
	 * Get address_to
	 *
	 * @return Address object
	 */
	public Address getAddressTo() {
		return address_to;
	}

	/**
	 * Set address to for navigation
	 *
	 * @param address_to
	 */
	public void setAddressTo(Address address_to) {
		this.address_to = address_to;
	}

	/**
	 * Get address_from
	 *
	 * @return Address object
	 */
	public Address getAddressFrom() {
		return address_from;
	}

	/**
	 * Set address from for navigation
	 *
	 * @param address_from
	 */
	public void setAddressFrom(Address address_from) {
		this.address_from = address_from;
	}

	/**
	 * Initializes the view.
	 */
	private void init() {
		// Set GUI Size
		getContentPane().setPreferredSize(new Dimension(GUI_WIDTH, GUI_HEIGHT));
		setDefaultCloseOperation(EXIT_ON_CLOSE);

		// Initialize panes and panels
		// This is the upper layered pane which contains p_search and p_navigation
		lp_upper = new JLayeredPane();
		// This is the mid layered pane which contains p_know
		lp_mid = new JLayeredPane();
		JLayeredPane lp_lower = new JLayeredPane();
		// This panel shows the search functions
		p_search = constructSearchPanel();
		// This panel shows the navigation functions
		p_navigation = constructNavigationPanel();
		// This panel shows suggestions
		p_suggestions = constructSuggestionPanel();
		// This panel shows directions
		p_directions = constructDirectionsPanel();
		// This panel shows the "Gå til xx"
		JPanel p_know = constructKnowPanel();
		// This is the root panel that contains the entire GUI
		JPanel p_root = new JPanel(new BorderLayout());
		// This is the left panel that contains all the left gui panels and functions
		JPanel p_left = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));

		// Hide mid panel for now
		lp_mid.setVisible(false);
		hideKnowPanel();

		// Design left part of the GUI
		p_left.setBorder(new EmptyBorder(GUI_MARGIN, GUI_MARGIN, 0, 0));
		p_left.setPreferredSize(new Dimension(GUI_P_LEFT_WIDTH + GUI_MARGIN, HEIGHT + GUI_MARGIN));
		p_left.setOpaque(false);

		// Add search panel and navigation panel to left part of the gui
		lp_upper.setPreferredSize(new Dimension(GUI_P_LEFT_WIDTH, 40));
		lp_upper.add(p_search);
		lp_upper.add(p_navigation);

		// Add the layerpane which contains search and navigation panel to the left panel
		p_left.add(lp_upper);
		p_left.add(Box.createRigidArea(new Dimension(GUI_MARGIN, GUI_MARGIN - 10)));
		lp_lower.setPreferredSize(new Dimension(GUI_P_LEFT_WIDTH, 410));

		// Add the mid layeredpane and the panel that shows the search result to the left panel
		lp_mid.setPreferredSize(new Dimension(GUI_P_LEFT_WIDTH, 40));
		lp_mid.add(p_know);
		p_left.add(lp_mid);
		p_left.add(Box.createRigidArea(new Dimension(GUI_MARGIN, GUI_MARGIN)));

		// Add live suggestions and directions to the left panel
		lp_lower.add(p_suggestions);
		lp_lower.add(p_directions);
		p_left.add(lp_lower);

		// Add left panel to the root panel
		p_root.add(p_left, BorderLayout.WEST);

		// Initiate right side
		JPanel p_right = new JPanel(null);
		p_right.setPreferredSize(new Dimension(GUI_P_RIGHT_WIDTH, GUI_P_RIGHT_HEIGHT));
		p_right.setMaximumSize(new Dimension(GUI_P_RIGHT_WIDTH, GUI_P_RIGHT_HEIGHT));
		p_right.setMinimumSize(new Dimension(GUI_P_RIGHT_WIDTH, GUI_P_RIGHT_HEIGHT));
		p_root.add(p_right, BorderLayout.EAST);

		btn_settings = new ImageButton("settings", GUI_P_RIGHT_BUTTON_Y_OFFSET, true);
		btn_settings.setBounds(0, 0, GUI_P_RIGHT_BUTTON_Y_OFFSET, GUI_P_RIGHT_BUTTON_Y_OFFSET);
		p_right.add(btn_settings);

		btn_briller = new ImageButton("briller", GUI_P_RIGHT_BUTTON_Y_OFFSET, true);
		btn_briller.setBounds(0, 0, GUI_P_RIGHT_BUTTON_Y_OFFSET, GUI_P_RIGHT_BUTTON_Y_OFFSET);
		p_right.add(btn_briller);

		btn_placering = new ImageButton("placering", GUI_P_RIGHT_BUTTON_Y_OFFSET, true);
		btn_placering.setBounds(0, 0, GUI_P_RIGHT_BUTTON_Y_OFFSET, GUI_P_RIGHT_BUTTON_Y_OFFSET);
		p_right.add(btn_placering);

		btn_save = new ImageButton("save", GUI_P_RIGHT_BUTTON_Y_OFFSET, true);
		btn_save.setBounds(0, 0, GUI_P_RIGHT_BUTTON_Y_OFFSET, GUI_P_RIGHT_BUTTON_Y_OFFSET);
		p_right.add(btn_save);

		btn_open = new ImageButton("open", GUI_P_RIGHT_BUTTON_Y_OFFSET, true);
		btn_open.setBounds(0, 0, GUI_P_RIGHT_BUTTON_Y_OFFSET, GUI_P_RIGHT_BUTTON_Y_OFFSET);
		p_right.add(btn_open);

		btn_theme1 = new ImageButton("theme1", GUI_P_RIGHT_BUTTON_SMALL_Y_OFFSET, false);
		btn_theme1.setBounds(0, 0, GUI_P_RIGHT_BUTTON_SMALL_Y_OFFSET, GUI_P_RIGHT_BUTTON_SMALL_Y_OFFSET);
		p_right.add(btn_theme1);
		btn_theme2 = new ImageButton("theme2", GUI_P_RIGHT_BUTTON_SMALL_Y_OFFSET, false);
		btn_theme2.setBounds(0, 0, GUI_P_RIGHT_BUTTON_SMALL_Y_OFFSET, GUI_P_RIGHT_BUTTON_SMALL_Y_OFFSET);
		p_right.add(btn_theme2);
		btn_theme3 = new ImageButton("theme3", GUI_P_RIGHT_BUTTON_SMALL_Y_OFFSET, false);
		btn_theme3.setBounds(0, 0, GUI_P_RIGHT_BUTTON_SMALL_Y_OFFSET, GUI_P_RIGHT_BUTTON_SMALL_Y_OFFSET);
		p_right.add(btn_theme3);

		// Bind the y-value of btn_briller to the btn_themes
		btn_briller.addChild(btn_theme1);
		btn_briller.addChild(btn_theme2);
		btn_briller.addChild(btn_theme3);

		// Create zoom panel
		JPanel p_zoom = new JPanel(null);
		p_zoom.setLayout(new BoxLayout(p_zoom, BoxLayout.Y_AXIS));
		b_zoomIn = new ImageButton("zoomIn", 44, true);
		b_zoomOut = new ImageButton("zoomOut", 44, true);
		p_zoom.add(b_zoomIn);
		p_zoom.add(b_zoomOut);
		p_zoom.setSize(44, 100);
		p_right.add(p_zoom);
		p_zoom.setLocation(190, GUI_HEIGHT - 130);
		p_zoom.setOpaque(false);


		// Create weather panel and load weather
		JPanel p_weather = new JPanel();
		JLabel lbl = new JLabel("<html><font color='#4a90e2'> Loading...</font></html>", JLabel.LEFT);
		Thread t = new Thread() {
			public void run() {
				int counter = 0;
				try {
					// Loop in a seperate thread until the weather information has loaded
					while (m.geoLocation.getWeatherTemperature() == null && m.geoLocation.getWeatherIcon() == null) {
						if (counter == 25) return;
						Thread.sleep(250);
						counter++;
					}
					lbl.setText("<html><font color='#4a90e2'> " + m.geoLocation.getWeatherTemperature() + " </font></html>");
					lbl.setIcon(new RetinaIcon(ImageLoader.loadImage("weather/" + m.geoLocation.getWeatherIcon() + ".png", 32, true)));
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		};
		t.start();


		// Create pin panel
		p_pin = constructPinPanel();


		// Add weather and pin panel to the root panel
		p_weather.add(lbl);
		p_root.add(p_pin);
		p_pin.setLocation((GUI_WIDTH / 2) - p_pin.getWidth() / 2, (int) (GUI_HEIGHT * 0.8));
		p_root.add(p_weather);

		// Set panels opaque
		p_root.setOpaque(false);
		p_right.setOpaque(false);
		p_weather.setOpaque(false);

		// Create layerpane and set size of the views
		JLayeredPane lp = getLayeredPane();
		mapView.setSize(new Dimension(GUI_WIDTH, GUI_HEIGHT));
		p_root.setSize(new Dimension(GUI_WIDTH, GUI_HEIGHT));

		// Add map view and root to the layerpane
		lp.add(mapView, Integer.valueOf(0));
		lp.add(p_root, Integer.valueOf(1));

		pack();
		// Position in middle
		setLocationRelativeTo(null);
		setVisible(true);
		// Give text field the focus
		tf_search.requestFocus();
		tf_active = tf_search;

		// Set Car as the chosen navigation transportation type.
		b_car.setClicked(b_walk, b_bike);
		setShortestRouteBtn();


		// Update location of GUI elements when resizing the GUI
		addComponentListener(new ComponentAdapter() {
			public void componentResized(ComponentEvent evt) {
				SwingUtilities.invokeLater(() -> {
					Debugger.print(User.JANB, "Before: " + p_root.getX());
					Container pane = getContentPane();
					int width = pane.getWidth();
					int height = pane.getHeight();

					p_root.setSize(width, height);
					p_zoom.setLocation(190, height - 112);
					mapView.setSize(new Dimension(width, height));
					mapView.resizeView(width, height);
					p_pin.setLocation((width / 2) - p_pin.getWidth() / 2, (int) (height * 0.9) - p_pin.getHeight());
					p_root.setSize(new Dimension(width, height));
					validate();

					validate();

					// Cancel the resizeTimer if already active
					resizeTimer.cancel();
					resizeTimer.purge();
					resizeTimer = new Timer();
					// Schedule the timer to repaint after a small delay. Hence only if you haven't resized in this delay, redraw the tiles.
					resizeTimer.schedule(new TimerTask() {
						@Override
						public void run() {
							mapView.redrawAllTiles();
						}
					}, 80);

				});
			}
		});


	}


	/**
	 * This method constructs the suggestion views that will be used for live search results.
	 * Note that the suggestion views are being recycled and therefore only created upon initializing.
	 */
	private void constructSuggestionViews() {
		for (int i = 0; i < 7; i++) {
			suggestionViewList.add(new SuggestionView(RetinaIcon.createLocationPinIcon()));
		}
	}

	/**
	 * Update the View
	 */
	@Override
	public void update(Observable arg0, Object arg1) {
	}

	/**
	 * Constructs the search panel
	 *
	 * @return JPanel
	 */
	private JPanel constructSearchPanel() {
		JPanel p_upper_first = new JPanel() {
			@Override
			protected void paintComponent(Graphics g) {
				final Graphics2D g2d = (Graphics2D) g.create();
				//  Set background image
				if (RetinaIcon.isRetina()) g2d.scale(0.5, 0.5);
				g2d.drawImage(ImageLoader.loadImage("rectangle.png", new Dimension(GUI_P_LEFT_WIDTH, 40), true), 0, 0, null);
				g2d.scale(1, 1);
				g2d.dispose();
			}
		};
		tf_search.setPlaceholder("Søg i The-D Maps");
		tf_search.setPreferredSize(new Dimension(246, 30));
		// Remove the border
		tf_search.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 0, Color.WHITE));
		b_search = new ImageButton("søge", 24, true);
		b_directions = new ImageButton("directions", 24, true);
		p_upper_first.add(tf_search);
		JPanel p = new JPanel();
		p.setPreferredSize(new Dimension(4, GUI_MARGIN));
		p.setOpaque(false);
		p_upper_first.add(b_search);
		p_upper_first.add(p);
		p_upper_first.add(b_directions);
		p_upper_first.setOpaque(false);
		p_upper_first.setBounds(0, 0, GUI_P_LEFT_WIDTH, 40);
		return p_upper_first;
	}

	/**
	 * Show the loading icon that is shown when levenshtein search is running
	 */
	public void showLoadingIcon() {
		loading_label.setVisible(true);
	}

	/**
	 * Hide the loading icon that is shown when levenshtein search is running
	 */
	public void hideLoadingIcon() {
		loading_label.setVisible(false);
	}

	/**
	 * Show the blue loading icon that is shown when levenshtein search is running
	 */
	public void showBlueLoadingIcon() {
		blue_loading_label.setVisible(true);
	}

	/**
	 * Hide the blue loading icon that is shown when levenshtein search is running
	 */
	public void hideBlueLoadingIcon() {
		blue_loading_label.setVisible(false);
	}

	/**
	 * Constructs the search panel
	 *
	 * @return JPanel
	 */
	private JPanel constructKnowPanel() {
		JPanel p_upper_first = new JPanel() {
			@Override
			protected void paintComponent(Graphics g) {
				final Graphics2D g2d = (Graphics2D) g.create();
				if (RetinaIcon.isRetina()) g2d.scale(0.5, 0.5);
				g2d.drawImage(ImageLoader.loadImage("rectangle_no_line.png", new Dimension(GUI_P_LEFT_WIDTH, 40), true), 0, 0, null);
				g2d.scale(1, 1);
				g2d.dispose();
			}
		};
		// Set layout manager
		p_upper_first.setLayout(new BoxLayout(p_upper_first, BoxLayout.X_AXIS));
		// Create margin
		p_upper_first.setBorder(new EmptyBorder(0, 10, 0, 10));
		lbl_address = new JLabel("<html><font color='#9E9E9E'>Gå til: </font></html>");
		lbl_address.setSize(GUI_P_LEFT_WIDTH - 20, 40);
		p_upper_first.add(lbl_address);
		Icon icon = new ImageIcon(ResourceLoader.getUrl("resources/ajax-loader.gif"));
		loading_label = new JLabel(icon);
		loading_label.setVisible(false);
		p_upper_first.add(loading_label);
		JPanel p = new JPanel();
		p.setPreferredSize(new Dimension(4, GUI_MARGIN));
		p.setOpaque(false);
		p_upper_first.add(p);
		p_upper_first.setOpaque(false);
		p_upper_first.setBounds(0, 0, GUI_P_LEFT_WIDTH, 40);
		return p_upper_first;
	}


	/**
	 * Show the know panel
	 */
	public void showKnowPanel() {
		lp_mid.setVisible(true);
	}

	/**
	 * Hide the know panel
	 */
	public void hideKnowPanel() {
		lp_mid.setVisible(false);
	}


	/**
	 * Constructs the navigation panel
	 *
	 * @return JPanel
	 */
	private JPanel constructNavigationPanel() {
		JPanel p_upper_second = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0)) {
			@Override
			protected void paintComponent(Graphics g) {
				final Graphics2D g2d = (Graphics2D) g.create();
				if (RetinaIcon.isRetina()) g2d.scale(0.5, 0.5);
				g2d.drawImage(ImageLoader.loadImage("rectangle_navigation.png", new Dimension(GUI_P_LEFT_WIDTH, 140), true), 0, 0, null);
				g2d.scale(1, 1);
				g2d.dispose();
			}
		};

		JPanel p_left = new JPanel();
		p_left.setPreferredSize(new Dimension(297, 140));
		p_left.setOpaque(false);
		p_left.setLayout(new BoxLayout(p_left, BoxLayout.Y_AXIS));
		JPanel p_right = new JPanel();
		p_right.setPreferredSize(new Dimension(33, 140));
		p_right.setLayout(new BoxLayout(p_right, BoxLayout.Y_AXIS));

		// Navigation icons
		JPanel p_nav_icons = new JPanel();
		p_nav_icons.setOpaque(false);
		p_nav_icons.setLayout(new BoxLayout(p_nav_icons, BoxLayout.X_AXIS));
		b_walk = new ImageButton("walk", 24, true);
		b_bike = new ImageButton("bike", 24, true);
		b_car = new ImageButton("car", 24, true);
		b_cross = new ImageButton("clear", 16, true);
		b_swap = new ImageButton("swap", 24, true);
		p_right.add(b_cross);
		p_right.add(Box.createRigidArea(new Dimension(5, 63)));
		p_right.add(b_swap);
		p_right.setOpaque(false);

		Icon icon = new ImageIcon(ResourceLoader.getUrl("resources/ajax-loader-blue.gif"));
		blue_loading_label = new JLabel(icon);
		blue_loading_label.setVisible(false);


		p_nav_icons.add(b_walk);
		p_nav_icons.add(Box.createRigidArea(new Dimension(40, 0)));
		p_nav_icons.add(b_bike);
		p_nav_icons.add(Box.createRigidArea(new Dimension(40, 0)));
		p_nav_icons.add(b_car);
		p_left.add(p_nav_icons);

		JPanel p_type = new JPanel();
		p_type.setOpaque(false);
		p_type.setPreferredSize(new Dimension(p_left.getWidth(), 15));
		p_type.setLayout(new BoxLayout(p_type, BoxLayout.X_AXIS));
		p_type.add(blue_loading_label);

		b_fastest_route = new JButton("");
		b_fastest_route.setBorderPainted(false);
		b_fastest_route.setOpaque(false);
		b_fastest_route.setFocusPainted(false);
		b_fastest_route.setContentAreaFilled(false);
		b_fastest_route.setCursor(new Cursor(Cursor.HAND_CURSOR));
		b_fastest_route.setMargin(new Insets(0, 0, 0, 0));
		b_fastest_route.setIcon(new RetinaIcon(ImageLoader.loadImage("fastest_route", GUI_NAVIGATE_BUTTON_SIZE, true)));
		b_fastest_route.setRolloverIcon(new RetinaIcon(ImageLoader.loadImage("fastest_route_hover", GUI_NAVIGATE_BUTTON_SIZE, true)));
		b_fastest_route.setPressedIcon(new RetinaIcon(ImageLoader.loadImage("fastest_route_hover", GUI_NAVIGATE_BUTTON_SIZE, true)));

		b_shortest_route = new JButton("");
		b_shortest_route.setBorderPainted(false);
		b_shortest_route.setOpaque(false);
		b_shortest_route.setFocusPainted(false);
		b_shortest_route.setContentAreaFilled(false);
		b_shortest_route.setCursor(new Cursor(Cursor.HAND_CURSOR));
		b_shortest_route.setMargin(new Insets(0, 0, 0, 0));
		b_shortest_route.setIcon(new RetinaIcon(ImageLoader.loadImage("shortest_route", GUI_NAVIGATE_BUTTON_SIZE, true)));
		b_shortest_route.setRolloverIcon(new RetinaIcon(ImageLoader.loadImage("shortest_route_hover", GUI_NAVIGATE_BUTTON_SIZE, true)));
		b_shortest_route.setPressedIcon(new RetinaIcon(ImageLoader.loadImage("shortest_route_hover", GUI_NAVIGATE_BUTTON_SIZE, true)));

		p_type.add(b_fastest_route);
		p_type.add(b_shortest_route);
		p_left.add(Box.createRigidArea(new Dimension(0, GUI_MARGIN / 2)));
		p_left.add(p_type);

		p_left.add(Box.createRigidArea(new Dimension(0, GUI_MARGIN / 4)));

		tf_from = new PlaceholderTextField();
		tf_from.setPlaceholder("Indtast fra");
		tf_from.setOpaque(false);
		tf_from.setBackground(new Color(0, 0, 0, 0));
		tf_from.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(255, 255, 255, 135))); //Invisible
		tf_from.setForeground(Color.WHITE);
		tf_from.setDisabledTextColor(new Color(255, 255, 255, 140));
		tf_from.setCaretColor(Color.WHITE);
		p_left.add(tf_from);

		p_left.add(Box.createRigidArea(new Dimension(0, 6)));

		tf_to = new PlaceholderTextField();
		tf_to.setPlaceholder("Indtast til (Tryk på enter for at søge)");
		tf_to.setOpaque(false);
		tf_to.setBackground(new Color(0, 0, 0, 0));
		tf_to.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(255, 255, 255, 135))); //Invisible
		tf_to.setForeground(Color.WHITE);
		tf_to.setCaretColor(Color.WHITE);
		tf_to.setDisabledTextColor(new Color(255, 255, 255, 140));
		p_left.add(tf_to);

		p_left.setBorder(new EmptyBorder(GUI_MARGIN / 2, GUI_MARGIN / 2, GUI_MARGIN / 2, 0));

		p_upper_second.add(p_left);
		p_upper_second.add(p_right);
		p_upper_second.setBounds(0, 0, GUI_P_LEFT_WIDTH, 168);
		p_upper_second.setOpaque(false);
		p_upper_second.setVisible(false);
		return p_upper_second;
	}

	/**
	 * Displays a Pin that can be saved, deleted or navigated to.
	 *
	 * @param pin Pin to display
	 */
	public void showPin(Pin pin) {
		activePin = pin;
		lbl_road.setText(pin.getName() != null ? pin.getName() : "Unavngivet vej");
		float x = (float) (pin.getLocation().getX() / m.lonfactor);
		float y = (float) (-pin.getLocation().getY());
		lbl_degree.setText("<html><font color='#777777'>" + y + ", " + x + "</font></html>");
		p_pin.setVisible(true);
	}

	/**
	 * Hides the currently active pin.
	 */
	public void hidePin() {
		p_pin.setVisible(false);
		activePin = null;
	}


	/**
	 * Constructs the pin modification panel
	 *
	 * @return JPanel
	 */
	private JPanel constructPinPanel() {
		JPanel p = new JPanel() {
			@Override
			protected void paintComponent(Graphics g) {
				final Graphics2D g2d = (Graphics2D) g.create();
				if (RetinaIcon.isRetina()) g2d.scale(0.5, 0.5);
				g2d.drawImage(ImageLoader.loadImage("rectangle_pin.png", new Dimension(250, 105), true), 0, 0, null);
				g2d.scale(1, 1);
				g2d.dispose();
			}
		};
		p.setBorder(new EmptyBorder(GUI_MARGIN / 2, GUI_MARGIN / 2, GUI_MARGIN / 2, GUI_MARGIN / 2));
		p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));

		lbl_road = new JLabel("", RetinaIcon.createLocationPinIcon(), JLabel.LEFT);
		lbl_degree = new JLabel("<html><font color='#777777'></font></html>", JLabel.LEFT);

		JPanel p_close = new JPanel(new BorderLayout());

		b_pinClose = new ImageButton("clear_blue", 16, true);
		p_close.add(lbl_road, BorderLayout.WEST);
		p_close.add(b_pinClose, BorderLayout.EAST);

		JPanel p_degree = new JPanel(new BorderLayout());
		p_degree.setBorder(new EmptyBorder(0, 18, 0, 0));
		p_degree.add(lbl_degree, BorderLayout.WEST);

		JPanel p_buttons = new JPanel();
		b_pinDelete = new ImageButton("pin_delete", 24, true);
		b_pinSave = new ImageButton("pin_save", 24, true);
		b_pinNavigate = new ImageButton("directions", 24, true);
		p_buttons.add(b_pinDelete);
		p_buttons.add(Box.createRigidArea(new Dimension(67, 0)));
		p_buttons.add(b_pinSave);
		p_buttons.add(Box.createRigidArea(new Dimension(67, 0)));
		p_buttons.add(b_pinNavigate);

		p_degree.setOpaque(false);
		p.setOpaque(false);
		p_close.setOpaque(false);
		p_buttons.setOpaque(false);
		p.add(p_close);
		p.add(p_degree);
		p.add(p_buttons);
		p.setSize(250, 105);
		p.setVisible(false);
		return p;
	}

	/**
	 * Constructs the suggestion panel
	 *
	 * @return JPanel
	 */
	private JPanel constructSuggestionPanel() {
		JPanel p_lower_first = new JPanel(new GridBagLayout()) {
			@Override
			protected void paintComponent(Graphics g) {
				setBackgroundForViews(g);
			}
		};
		p_lower_first.setOpaque(false);
		p_lower_first.setBounds(0, 0, GUI_P_LEFT_WIDTH, 410);
		// GridBagConstraints for search results and navigation results
		GridBagConstraints c = new GridBagConstraints();
		c.anchor = GridBagConstraints.ABOVE_BASELINE; //Var first line
		c.weighty = 1;
		c.gridx = 0;
		c.gridy = 0;
		c.insets = new Insets(0, 0, 15, 0);

		// Add each suggestion view to the GUI so they can be reused.
		for (SuggestionView suggestion : suggestionViewList) {
			c.gridy++;
			p_lower_first.add(suggestion.getPanel(), c);
		}
		p_lower_first.setVisible(false);
		return p_lower_first;
	}

	/**
	 * Set 'Fastest' as the chosen navigation type
	 */
	public void setFastestRouteBtn() {
		Dijkstra.setWeightType(Dijkstra.FASTEST);
		b_fastest_route.setIcon(new RetinaIcon(ImageLoader.loadImage("fastest_route_hover", GUI_NAVIGATE_BUTTON_SIZE, true)));
		b_fastest_route.setPressedIcon(new RetinaIcon(ImageLoader.loadImage("fastest_route_hover", GUI_NAVIGATE_BUTTON_SIZE, true)));
		b_fastest_route.setRolloverIcon(new RetinaIcon(ImageLoader.loadImage("fastest_route_hover", GUI_NAVIGATE_BUTTON_SIZE, true)));
		b_shortest_route.setIcon(new RetinaIcon(ImageLoader.loadImage("shortest_route", GUI_NAVIGATE_BUTTON_SIZE, true)));
		b_shortest_route.setPressedIcon(new RetinaIcon(ImageLoader.loadImage("shortest_route", GUI_NAVIGATE_BUTTON_SIZE, true)));
		b_shortest_route.setRolloverIcon(new RetinaIcon(ImageLoader.loadImage("shortest_route", GUI_NAVIGATE_BUTTON_SIZE, true)));
	}

	/**
	 * Set 'Shortest' as the chose navigation type
	 */
	public void setShortestRouteBtn() {
		Dijkstra.setWeightType(Dijkstra.EUCLID);
		b_fastest_route.setIcon(new RetinaIcon(ImageLoader.loadImage("fastest_route", GUI_NAVIGATE_BUTTON_SIZE, true)));
		b_fastest_route.setPressedIcon(new RetinaIcon(ImageLoader.loadImage("fastest_route", GUI_NAVIGATE_BUTTON_SIZE, true)));
		b_fastest_route.setRolloverIcon(new RetinaIcon(ImageLoader.loadImage("fastest_route", GUI_NAVIGATE_BUTTON_SIZE, true)));
		b_shortest_route.setIcon(new RetinaIcon(ImageLoader.loadImage("shortest_route_hover", GUI_NAVIGATE_BUTTON_SIZE, true)));
		b_shortest_route.setPressedIcon(new RetinaIcon(ImageLoader.loadImage("shortest_route_hover", GUI_NAVIGATE_BUTTON_SIZE, true)));
		b_shortest_route.setRolloverIcon(new RetinaIcon(ImageLoader.loadImage("shortest_route_hover", GUI_NAVIGATE_BUTTON_SIZE, true)));
	}

	/**
	 * Constructs the directions panel
	 *
	 * @return JPanel
	 */
	private JPanel constructDirectionsPanel() {
		p_lower_second = new JPanel();
		p_lower_second.setLayout(new BoxLayout(p_lower_second, BoxLayout.Y_AXIS));

		JScrollPane pane = new JScrollPane(p_lower_second);
		pane.setBorder(new EmptyBorder(2, 2, 2, 2)); //Because the background image is round
		pane.setBounds(0, 0, GUI_P_LEFT_WIDTH, 410);
		pane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER); //SETTING SCHEME FOR HORIZONTAL BAR
		pane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		pane.setOpaque(false);

		JPanel p = new JPanel(new BorderLayout()) {
			@Override
			protected void paintComponent(Graphics g) {
				setBackgroundForViews(g);
			}
		};

		p.setOpaque(false);
		p.setBounds(0, 0, GUI_P_LEFT_WIDTH, 410);
		p.add(pane);
		p.setVisible(false);
		return p;
	}

	/**
	 * Draws a background image on a jcomponent
	 *
	 * @param g graphics to draw on
	 */
	private void setBackgroundForViews(Graphics g) {
		final Graphics2D g2d = (Graphics2D) g.create();
		if (RetinaIcon.isRetina()) g2d.scale(0.5, 0.5);
		g2d.drawImage(ImageLoader.loadImage("rectangle_large.png", new Dimension(GUI_P_LEFT_WIDTH, 410), true), 0, 0, null);
		g2d.scale(1, 1);
		g2d.dispose();
	}


	/**
	 * Hide the pin and show navigation panel
	 */
	public void showNavigationPanelAfterPin() {
		hidePin();
		hideKnowPanel();
		active_tf = 1;
		p_search.setVisible(false);
		p_navigation.setVisible(true);
		lp_upper.setPreferredSize(new Dimension(View.GUI_P_LEFT_WIDTH, 140));
		tf_from.requestFocus();
		tf_search.setText("");
	}


	/**
	 * Shows a list of directions in the directions panel from an Arraylist of strings.
	 *
	 * @param directions ArrayList<String>
	 */
	public void displayDirections(java.util.List<String> directions) {
		p_suggestions.setVisible(false);
		p_directions.setVisible(true);
		p_lower_second.removeAll();

		for (String str : directions) {
			SuggestionView sgst = new SuggestionView(new RetinaIcon(ImageLoader.loadImage("road.png", 28, true)));
			sgst.setSuggestion("<html>&nbsp;&nbsp;&nbsp;" + str + "</html>", str);
			p_lower_second.add(sgst.getPanel());
		}
		p_lower_second.updateUI();
	}

	/**
	 * Show the navigation panel and hide overlapping panels
	 */
	public void showNavigationPanel() {
		hideKnowPanel();
		active_tf = 1;
		p_search.setVisible(false);
		p_navigation.setVisible(true);
		lp_upper.setPreferredSize(new Dimension(GUI_P_LEFT_WIDTH, 140));
		tf_from.requestFocus();
		tf_to.setText(tf_search.getText());
		tf_search.setText("");
		SwingUtilities.invokeLater(m::dirty);
	}

	/**
	 * Show the search panel hide overlapping panels
	 */
	public void showSearchPanel() {
		//lp_mid.setVisible(true);
		p_search.setVisible(true);
		p_navigation.setVisible(false);
		p_directions.setVisible(false);
		lp_upper.setPreferredSize(new Dimension(GUI_P_LEFT_WIDTH, 40));
		address_to = new Address();
		address_from = new Address();
		tf_from.setText("");
		tf_to.setText("");
	}

	/**
	 * Sets all suggestions inactive. (The inactive state just means that the color is being changed to white)
	 */
	public void setAllSuggestionsInactive() {
		suggestionViewList.forEach(SuggestionView::setInactiveColor);
	}


	/**
	 * Returns either the address we're trying to search for, navigate from or navigate to based
	 * on the currently active text field.
	 *
	 * @return Address an Address object.
	 */
	public Address getAddress() {
		if (active_tf == 1) return address_from;
		return address_to;
	}


	/**
	 * Set the address that we're either searching for, navigating to or navigating from
	 *
	 * @param address
	 */
	public void setAddress(Address address) {
		switch (address.getType()) {
			case POI:
				if (active_tf == 1) address_from.setPoi(address.getPoi());
				else address_to.setPoi(address.getPoi());
				break;
			case STREET:
				if (active_tf == 1) address_from.setStreet(address.getStreet());
				else address_to.setStreet(address.getStreet());
				break;
			case CITY:
				if (active_tf == 1) address_from.setCity(address.getName());
				else address_to.setCity(address.getName());
				break;
			case POSTCODE:
				if (active_tf == 1) address_from.setPostcode(address.getName());
				else address_to.setPostcode(address.getName());
				break;
		}
		inferAddressPoint(address_from);
		inferAddressPoint(address_to);
		setAddressLabel();
	}


	/**
	 * Set the address label to display as a search result
	 */
	private void setAddressLabel() {
		StringBuilder builder = new StringBuilder();
		Address addr = getAddress();
		if (addr.getPoint() == null) return;
		if (addr.getPoi() != null) builder.append(addr.getPoi() + " ");
		if (addr.getStreet() != null) builder.append(addr.getStreet() + " ");
		if (addr.getCity() != null) builder.append(addr.getCity());
		if (addr.getPostcode() != null) builder.append(addr.getPostcode());
		lbl_address.setText("<html><font color='#9E9E9E'>Gå til: </font>" + builder + "</html>");
		if (active_tf == 0 && !lp_mid.isVisible()) showKnowPanel();
	}


	/**
	 * This is the method that figures out what part of an address object we navigate to.
	 * This is needed because an address object may contain a postcode, city, street and a point of interest so
	 * we must figure out what to show the user.
	 * This is hierarchy we use to select what to show the user
	 * 1. Point of Interest - If what the user has saved for contains a point of interest we select this
	 * 2. Street - If what the user has searched for does not contain a POI, but a street we select this
	 * 3. City - If what the user has searched for does not contain the above, but a city we select this
	 * 4. Postcode - If what the user has searched for does not contain the above, but a postcode we select this
	 * So basically if you search for "Fields Vejlands Allé 163A 2300 København S" we would select Fields
	 * because that's a point of interest.
	 */
	private void inferAddressPoint(Address address) {
		if (address.getPoi() != null) {
			address.setPoint(m.addressSearcher.getAddresses()[m.addressSearcher.binarySearch(address.getPoi())].getPoint());
			address.setName(address.getPoi());
		} else if (address.getStreet() != null) {
			address.setPoint(m.addressSearcher.getAddresses()[m.addressSearcher.binarySearch(address.getStreet())].getPoint());
			address.setName(address.getStreet());
		} else if (address.getCity() != null) {
			address.setPoint(m.addressSearcher.getAddresses()[m.addressSearcher.binarySearch(address.getCity())].getPoint());
			address.setName(address.getCity());
		} else if (address.getPostcode() != null) {
			address.setPoint(m.addressSearcher.getAddresses()[m.addressSearcher.binarySearch(address.getPostcode())].getPoint());
			address.setName(address.getPostcode());
		} else {
			address.setPoint(null);
			address.setName(null);
		}
	}

	/**
	 * This method clears all stored addresses that the user has previously searched for.
	 */
	public void clearAddress() {
		hideKnowPanel();
		if (active_tf == 1) address_from = new Address();
		else address_to = new Address();
	}


}