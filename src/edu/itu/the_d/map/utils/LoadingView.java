package edu.itu.the_d.map.utils;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.Timer;
import java.util.TimerTask;

/**
 * A View used to display the loading process whilest parsing the map.
 * <p>
 * Copyright 2016 The-D
 */
public final class LoadingView extends JFrame {
	private static final int WIDTH = 581;
	private static final int HEIGHT = 112 + 4 + 32 + 4; // 4 spacing
	private static LoadingView instance;
	private static boolean hasFailed = false;
	private JLabel infoField, timerField;
	private JPanel loadingBar;
	private Timer timer;

	/**
	 * A <i>private</i> constructor to initialize the JFrame instance.
	 */
	private LoadingView() {
		//Set icon
		super.setIconImage(ImageLoader.loadImage("theme1.png"));
		// Use borderlayout
		setLayout(new BorderLayout());

		// Get the logo image and use it on a JLabel
		JLabel poster = new JLabel(new ImageIcon(ImageLoader.loadImage("poster.png")));

		// Do not allow resizing
		setResizable(false);
		// Do not paint anything but the components (No windows frame)
		setUndecorated(true);
		// Set the size of the window
		setPreferredSize(new Dimension(WIDTH, HEIGHT));


		// The infoField will hold the most recent data message
		infoField = new JLabel("Loading...");
		// Make it have 32 height
		infoField.setPreferredSize(new Dimension(WIDTH / 2, 32)); // Only height is accounted for

		// The timerField will hold the time taken
		timerField = new JLabel("Time taken: 0 seconds");

		// Put infoField and timerField into a wrapping panel that uses padding
		JPanel infoWrapper = new JPanel(new BorderLayout());
		infoWrapper.setBorder(new EmptyBorder(0, 12, 0, 12)); // Padding
		infoWrapper.setBackground(new Color(255, 255, 255));
		infoWrapper.add(infoField, BorderLayout.WEST); // infoField at left
		infoWrapper.add(timerField, BorderLayout.EAST); // timerField at right

		// Create the loadingBar with null layout
		loadingBar = new JPanel(null);
		loadingBar.setBackground(Color.GREEN);
		// Of height 4 and full width
		loadingBar.setPreferredSize(new Dimension(WIDTH, 4));

		// Wrap the loadingBar and the infoWrapper into a bottom wrapper
		JPanel bottomWrapper = new JPanel();
		bottomWrapper.setLayout(new BoxLayout(bottomWrapper, BoxLayout.Y_AXIS));
		bottomWrapper.add(infoWrapper);
		bottomWrapper.add(loadingBar, BorderLayout.NORTH);

		// Add image and bottomWrapper to the JFrame
		add(poster, BorderLayout.NORTH);
		add(bottomWrapper, BorderLayout.SOUTH);

		// Make the Layout managers place the componenets
		pack();

		// Set the maximum size of the loading Bar to be 0 width, aka start at 0% loaded
		loadingBar.setMaximumSize(new Dimension(0, 4));

		// Set a transparent background on the JFrame
		setBackground(new Color(0, 0, 0, 0));
		// Open the frame in the middle of the screen
		setLocationRelativeTo(null);
		// Make it visible
		setVisible(true);

		// Get the time of which the program has started
		long startTime = System.currentTimeMillis();

		// Create a new timer
		timer = new Timer();
		// Schedule the timer to run a method every second
		timer.schedule(new TimerTask() {
			public void run() {
				// This method is not run exactly once per second, so calculate the accurate time taken by measuring
				// now - startTime.
				timerField.setText("Time taken: " + (int) ((System.currentTimeMillis() - startTime) / 1000) + " seconds");
			}
		}, 0, 1000);
	}

	/**
	 * Creates an instance if there's not already one and sets it to be visible.
	 */
	public static void turnOn() {
		if (instance == null) instance = new LoadingView();

		instance.infoField.setText("Loading...");
		instance.timerField.setText("");
		instance.loadingBar.setMaximumSize(new Dimension(0, 4));

		instance.setVisible(true);
	}

	/**
	 * Sets the instance to not be visible and kills the {@link LoadingView#timer} if active.
	 */
	public static void turnOff() {
		if (instance != null) {
			instance.setVisible(false);
			// Kill the timer if any
			if (instance.timer != null) {
				instance.timer.cancel(); // Cancel all current planned schedules
				instance.timer.purge(); // Remove all references to cancelled schedules
				instance.timer = null; // Forget the timer so the garbage collector will effectively free it from memory
			}
		}
	}

	/**
	 * If the parsing fails, this method can be called to display the error message in {@link LoadingView#infoField infoField}.
	 * After displaying the error for 5 seconds, the whole application will exit with exit code 42.
	 *
	 * @param errorMsg The error message to display in the {@link LoadingView#infoField infoField}.
	 */
	public static void hasFailed(String errorMsg) {
		if (hasFailed) return;
		hasFailed = true;

		// Kill the timer
		instance.timer.purge();
		instance.timer.cancel();

		// Set the text of the timerField to be ERROR
		instance.timerField.setText("<html><font color='red'>&lt;ERROR&gt;</font></html>");
		// Set the text of the infoField to the given errorMsg
		instance.infoField.setText("<html><font color='red'>" + errorMsg + "</font></html>");

		// Auto close the application after 5 seconds
		instance.timer = new Timer();
		instance.timer.schedule(new TimerTask() {
			public void run() {
				System.exit(42);
			} // Bye bye!
		}, 5000);
	}

	/**
	 * Set the info message to displayed on the {@link LoadingView#infoField infoField}.
	 *
	 * @param msg The message to display.
	 */
	public static void setInfoMsg(String msg) {
		if (hasFailed) return;
		instance.infoField.setText(msg);
	}

	/**
	 * Given a loading percentage (between 0 and 100), the {@link LoadingView#loadingBar loadingBar} will visually represent the percentage.
	 *
	 * @param percentage A number in between 0 and 100 to symbolize the current percentage that has been loaded.
	 */
	public static void setLoadPercentage(double percentage) {
		if (hasFailed) return;
		// The width of the loadingBar is simply calculated by WIDTH * (percentage/100)
		instance.loadingBar.setMaximumSize(new Dimension((int) (WIDTH * (percentage / 100.0)), 4));
	}
}