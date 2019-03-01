package edu.itu.the_d.map.view;

import edu.itu.the_d.map.utils.ImageLoader;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Class for creating image buttons easily. Supports retina imageList too
 * <p>
 * Copyright 2016 The-D
 */
public class ImageButton extends JButton {
	//Icons for this button
	private RetinaIcon btn, btnHovered, btnClicked;
	//Fold status
	private boolean isFoldedOut;
	//Lock status
	private boolean lock;
	//Children
	private List<JComponent> children;

	/**
	 * @param btnName   This is actually the image name. Do not include filetype
	 * @param size      The size of the button
	 * @param foldedOut Fold status.
	 */
	public ImageButton(String btnName, int size, boolean foldedOut) {
		//Fold status
		this.isFoldedOut = foldedOut;
		//Children
		children = new ArrayList<>();

		//Load imageList
		btn = new RetinaIcon(ImageLoader.loadImage(btnName + ".png", size, true));
		btnHovered = new RetinaIcon(ImageLoader.loadImage(btnName + "_hover.png", size, true));
		btnClicked = new RetinaIcon(ImageLoader.loadImage(btnName + "_hover.png", size, true));

		// Does not paint the standard JButton
		setBorderPainted(false);
		setOpaque(false);
		setFocusPainted(false);
		setContentAreaFilled(false);
		setCursor(new Cursor(Cursor.HAND_CURSOR));

		//Set size
		setPreferredSize(new Dimension(btn.getIconWidth(), btn.getIconHeight()));

		//Disable margin
		setMargin(new Insets(0, 0, 0, 0));

		//Set icons
		setIcon(btn);
		setRolloverIcon(btnHovered);
		setPressedIcon(btnClicked);

		//Set position & layout
		setHorizontalTextPosition(JButton.CENTER);
		setVerticalTextPosition(JButton.CENTER);
		setLayout(null);
	}


	/**
	 * Add children to this button. This is used by Spring Animation
	 *
	 * @param comp the child to add
	 */
	public void addChild(JComponent comp) {
		children.add(comp);
	}


	/**
	 * Override to also update the y value for children. This is used by Spring Animation
	 *
	 * @param x
	 * @param y
	 */
	@Override
	public void setLocation(int x, int y) {
		super.setLocation(x, y);
		children.forEach(c -> c.setLocation(c.getX(), y + (64 - 48) / 2));
	}

	/**
	 * Return the fold status. This is used by SpringAnimation
	 *
	 * @return boolean fold status
	 */
	public boolean isFoldedOut() {
		return isFoldedOut;
	}

	/**
	 * Set the fold status. This is used by SpringAnimation
	 *
	 * @param isFoldedOut
	 */
	public void setFoldedOut(boolean isFoldedOut) {
		this.isFoldedOut = isFoldedOut;
	}

	/**
	 * Returns the lock status. This is used by SpringAnimation
	 *
	 * @return true if is locked, false otherwise
	 */
	public boolean getLock() {
		return lock;
	}

	/**
	 * Sets the lock status. This is used by SpringAnimation
	 *
	 * @param lock the lock status to set it to
	 */
	public void setLock(boolean lock) {
		this.lock = lock;
	}


	/**
	 * Change this button's image to the clicked image and set any other images to not-clicked.
	 *
	 * @param buttons
	 */
	public void setClicked(ImageButton... buttons) {
		for (ImageButton btn : buttons) {
			btn.setDeClicked();
		}
		setIcon(btnClicked);
		setRolloverIcon(btnClicked);
		setPressedIcon(btnClicked);
	}

	/**
	 * Set images to the normal images.
	 */
	private void setDeClicked() {
		setIcon(btn);
		setRolloverIcon(btnHovered);
		setPressedIcon(btnClicked);
	}
}
