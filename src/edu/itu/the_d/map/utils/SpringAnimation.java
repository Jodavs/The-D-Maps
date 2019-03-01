package edu.itu.the_d.map.utils;

import edu.itu.the_d.map.model.Model;
import edu.itu.the_d.map.view.ImageButton;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Class used to make springAnimations for the icons on the right side of the view.
 * <p>
 * Copyright 2016 The-D
 */
public final class SpringAnimation {
	// Directions
	public static final Direction DIR_UP = Direction.UP;
	public static final Direction DIR_RIGHT = Direction.RIGHT;
	public static final Direction DIR_DOWN = Direction.DOWN;
	public static final Direction DIR_LEFT = Direction.LEFT;
	// This is the speed of the animation
	private static final int DELAY = 1000 / 150;
	// Initiate float spring
	private final FloatSpring fs = new FloatSpring(42);
	// Set delta
	private final float delta = 1f / 1000;
	private int target;
	private float elapsed = 0f;
	// This is the direction that the animation should go, can be UP, DOWN, LEFT, RIGHT
	private Direction direction;
	// This is the button to animate
	private ImageButton btn;
	// Initiate timer
	private Model model;
	private Timer timer = new Timer(DELAY, new ActionListener() {
		/**
		 * Rolls out the springAnimation or closes it if already open when ActionEvent is invoked.
		 * @param e of type ActionEvent
		 */
		@Override
		public void actionPerformed(ActionEvent e) {
			int curMoveValue;
			if (direction == Direction.LEFT || direction == Direction.RIGHT) curMoveValue = btn.getX();
			else if (direction == Direction.UP || direction == Direction.DOWN) curMoveValue = btn.getY();
			else throw new RuntimeException("Invalid direction");

			if ((direction == Direction.RIGHT || direction == Direction.DOWN) && curMoveValue >= target - 1) {
				// We're done animating so set status accordingly and free the button
				timer.stop();
				btn.setFoldedOut(!btn.isFoldedOut());
				btn.setLock(false);
				SwingUtilities.invokeLater(model::dirty);

				switch (direction) {
					case LEFT:
					case RIGHT:
						btn.setLocation(target, btn.getY());
						break;
					case UP:
					case DOWN:
						btn.setLocation(btn.getX(), target);
						break;
				}
				return;
			} else if ((direction == Direction.LEFT || direction == Direction.UP) && curMoveValue <= target) {
				// We're done animating so set status acoordingly and free the button
				timer.stop();
				btn.setFoldedOut(!btn.isFoldedOut());
				btn.setLock(false);
				SwingUtilities.invokeLater(model::dirty);

				switch (direction) {
					case LEFT:
					case RIGHT:
						btn.setLocation(target, btn.getY());
						break;
					case UP:
					case DOWN:
						btn.setLocation(btn.getX(), target);
						break;
				}
				return;
			}

			//Update animation
			elapsed += delta;
			fs.update(target, elapsed);
			int pos = (int) fs.getPosition();

			switch (direction) {
				case LEFT:
				case RIGHT:
					btn.setLocation(pos, btn.getY());
					break;
				case UP:
				case DOWN:
					btn.setLocation(btn.getX(), pos);
					break;
			}

			if (!btn.isVisible()) btn.setVisible(true);
		}
	});

	/**
	 * SpringAnimation constructor, sets the direction of the animation and the speed of
	 * the animation.
	 *
	 * @param btn       og type ImageButton.
	 * @param distance  of type int.
	 * @param direction of type Direction.
	 */
	public SpringAnimation(ImageButton btn, int distance, Model model, Direction direction) {
		//Stop is button is already animating
		if (btn.getLock()) return;

		//Set fields
		this.model = model;
		this.direction = direction;
		this.btn = btn;
		this.elapsed = 0;
		this.btn.setLock(true);

		switch (direction) {
			case LEFT:
			case RIGHT:
				this.target = btn.getX() + distance * direction.axisDir;
				fs.setPosition(btn.getX());
				break;
			case UP:
			case DOWN:
				this.target = btn.getY() + distance * direction.axisDir;
				fs.setPosition(btn.getY());
				break;
		}

		//Initiate timer
		timer.start();
	}

	/**
	 * The buttons that are supposed to be part of the animation.
	 *
	 * @param direction of type Direction.
	 * @param distance  of type int.
	 * @param buttons   of type ImageButton.
	 */
	public static void animateGroup(Direction direction, int distance, Model model, ImageButton... buttons) {
		//Stop if any of the buttons is already animating
		for (ImageButton btn : buttons) {
			if (btn.getLock()) return;
		}

		//Start a spring animation instance for each button to animate
		for (int i = 0; i < buttons.length; i++) {
			new SpringAnimation(buttons[i], distance * (i + 1), model, direction);
		}
	}

	/**
	 * Enum for the directions.
	 */
	private enum Direction {
		UP(-1), RIGHT(1), DOWN(1), LEFT(-1);

		int axisDir;

		Direction(int axisDir) {
			this.axisDir = axisDir;
		}
	}
}
