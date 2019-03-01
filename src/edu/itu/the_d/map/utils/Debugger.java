package edu.itu.the_d.map.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Makes is possible for each user to print a message to the terminal, that only is printed
 * if the program is run from there user.
 * <p>
 * Copyright 2016 The-D
 */
public final class Debugger {
	private static User currentUser = null;
	private static boolean showAll = false;

	/**
	 * Identifies who the user is, so it knows what to print.
	 */
	private static void identifyUser() {
		try {
			String command = "git config user.name";
			Process p = Runtime.getRuntime().exec(command);
			BufferedReader input = new BufferedReader(new InputStreamReader(p.getInputStream()));
			String a = input.readLine();
			switch (a) {
				case "Anton Mølbjerg Eskildsen":
					currentUser = User.AESK;
					break;
				case "Lucas Schønrock":
					currentUser = User.LUSC;
					break;
				case "Jonleif Huneck Davidsen":
					currentUser = User.JDAV;
					break;
				case "Jakob":
					currentUser = User.JAKS;
					break;
				case "Stefanie Schor Olafsdottir":
					currentUser = User.STEO;
					break;
				case "Jannik Munk Bryld":
				case "Exfridos":
					currentUser = User.JANB;
					break;
				default:
					currentUser = User.NONE;
					break;
			}
		} catch (IOException e) {
			//If git is not installed or it otherwise fails then set user to global
			currentUser = User.NONE;
		}
	}

	/**
	 * prints the messages if user matches GLOBAL or the currentUser.
	 * Global is printed for all users.
	 *
	 * @param user of type User
	 * @param msg  of type Object
	 */
	public static void print(User user, Object msg) {
		if (currentUser == null) identifyUser();
		if (user == currentUser || user == User.GLOBAL || showAll) System.out.println(msg);
	}

}
