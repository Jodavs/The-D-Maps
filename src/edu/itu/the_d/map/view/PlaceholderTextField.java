package edu.itu.the_d.map.view;

import javax.swing.*;
import javax.swing.text.Document;
import java.awt.*;

/**
 * Class for creating a textfield that contains a placeholder whenever empty.
 * <p>
 * Copyright 2016 The-D
 */
public class PlaceholderTextField extends JTextField {
	// Placeholder to display
	private String placeholder;

	/**
	 * Default constructor
	 */
	public PlaceholderTextField() {
	}


	/**
	 * Constructor
	 *
	 * @param pDoc     of type Document, and is final.
	 * @param pText    of type string, and is final.
	 * @param pColumns of type int, and is final.
	 */
	public PlaceholderTextField(final Document pDoc, final String pText, final int pColumns) {
		super(pDoc, pText, pColumns);
	}


	/**
	 * Constructor
	 *
	 * @param pColumns of type int, and is final.
	 */
	public PlaceholderTextField(final int pColumns) {
		super(pColumns);
	}

	/**
	 * Constructor
	 *
	 * @param pText of type string, and is final.
	 */
	public PlaceholderTextField(final String pText) {
		super(pText);
	}


	/**
	 * Constructor
	 *
	 * @param pText    of type string, and is final.
	 * @param pColumns of type string, and is final.
	 */
	public PlaceholderTextField(final String pText, final int pColumns) {
		super(pText, pColumns);
	}


	/**
	 * Set the placeholder
	 *
	 * @param s String to put as placeholder
	 */
	public void setPlaceholder(String s) {
		placeholder = s;
	}

	/**
	 * Sets the text color.
	 *
	 * @param c of type Color.
	 */
	@Override
	public void setDisabledTextColor(Color c) {
		super.setDisabledTextColor(c);
	}

	/**
	 * Sets the foreground color.
	 *
	 * @param fg of type Color.
	 */
	@Override
	public void setForeground(Color fg) {
		super.setForeground(fg);
	}

	/**
	 * Paints the components.
	 *
	 * @param p of type Graphics.
	 */
	@Override
	protected void paintComponent(Graphics p) {
		super.paintComponent(p);

		if (placeholder.length() == 0 || getText().length() > 0) {
			return;
		}

		final Graphics2D g = (Graphics2D) p;
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g.setColor(getDisabledTextColor());
		g.drawString(placeholder, 0, p.getFontMetrics().getMaxAscent() + getInsets().top + 7);
	}

}