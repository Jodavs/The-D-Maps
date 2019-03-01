package edu.itu.the_d.map.view;

import edu.itu.the_d.map.model.mapobjects.Address;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;

/**
 * Class for creating suggestion views. This is being used to show results from live searching.
 * <p>
 * Copyright 2016 The-D
 */
public class SuggestionView {
	// The label for this suggestion view
	private JLabel label;
	// The panel that makes this suggestion view
	private JPanel panel;
	// A string that contains the same text as the label just without the HTML code.
	private String noHTML;


	/**
	 * Constructor for direction suggestions
	 */
	SuggestionView(RetinaIcon icon) {
		// Create panel
		panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		// Create label
		label = new JLabel("", icon, JLabel.LEFT);
		// Add label to panel
		panel.add(label);
		// Set the border
		setBorder();
		// Set the size of the panel
		panel.setPreferredSize(new Dimension(310, 43));
		// Set the color to inactive color (White)
		setInactiveColor();
		// Hide the suggestion
		hideSuggestion();
	}


	/**
	 * This method changes the text of the suggestions and makes those suggestions that aren't used invisible.
	 *
	 * @param list  The list of AddressObject to display as suggestions. This is usually coming from the liveSearch method.
	 * @param query The string that the enter used that matched with one or more AddressObjects. We use this to highlight part of the suggestion.
	 * @param v     View reference to show suggestions on
	 */
	public static void showSuggestions(ArrayList<Address> list, String query, View v) {
		int i = 0;
		for (Address obj : list) {
			// Set the suggestion
			v.suggestionViewList.get(i).setSuggestion("<html>"
					+ colorMatch(query, obj.toString())
					+ " <font color='#BDBDBD'><br>"
					+ obj.toDisplayString()
					+ "</font></html>", obj.toString());
			i++;
		}
		for (int j = 6; j >= i; j--) v.suggestionViewList.get(j).hideSuggestion(); // Hide unused suggestions
	}


	/**
	 * This method id used by showSuggestions to generate a string with html that highlights letter similarity between two strings
	 *
	 * @param str1 First string to compare with.
	 * @param str2 Second string to compare with.
	 * @return a string that contains html code
	 */
	private static String colorMatch(String str1, String str2) {
		String html = "<font color='#9E9E9E'>";

		int i;
		int min = str1.length() <= str2.length() ? str1.length() : str2.length();
		for (i = 0; i < min; i++) {
			if (str2.substring(i, i + 1) == str1.substring(i, i + 1)) break;
			if (Character.toLowerCase(str1.charAt(i)) != Character.toLowerCase(str2.charAt(i))) break;
		}
		if (i > 0) {
			html += str2.substring(0, i) + "</font>" + str2.substring(i, str2.length());
			return html;
		}
		if (html.equals("<font color='#9E9E9E'>")) html += str2;
		html += "</font>";
		return html;
	}

	/**
	 * Get NoHTML
	 *
	 * @return noHTML
	 */
	public String getNoHTML() {
		return noHTML;
	}


	/**
	 * Set the color of this suggestion to the active color
	 */
	public void setActiveColor() {
		panel.setBackground(new Color(230, 230, 230));
	}

	/**
	 * Set the color of this suggestio nto the inactive color
	 */
	void setInactiveColor() {
		panel.setBackground(Color.WHITE);
	}

	/**
	 * Get the panel
	 *
	 * @return JPanel
	 */
	JPanel getPanel() {
		return panel;
	}

	/**
	 * Create a border for this view
	 */
	private void setBorder() {
		panel.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(189, 189, 189)));
	}

	/**
	 * Hide the border for this view
	 */
	private void hideBorder() {
		panel.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.WHITE));
	}

	/**
	 * return a boolean indicating if this suggestion is visible or not
	 *
	 * @return boolean
	 */
	public boolean isVisible() {
		return label.isVisible();
	}

	/**
	 * Set this suggestions text and make it visible
	 *
	 * @param text   as string containing HTML code
	 * @param NoHtml as string without HTML code
	 */
	void setSuggestion(String text, String NoHtml) {
		label.setText(text);
		this.noHTML = NoHtml;
		setBorder();
		label.setVisible(true);
	}

	/**
	 * Hide this suggestion
	 */
	public void hideSuggestion() {
		label.setVisible(false);
		hideBorder();
	}

}
