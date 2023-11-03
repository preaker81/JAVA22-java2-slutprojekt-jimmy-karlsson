package com.slutprojekt.JimmyKarlsson;

import com.slutprojekt.JimmyKarlsson.controller.Facade;

/**
 * This is the main class of the application which acts as the entry point for
 * the program. It initializes the system by creating an instance of Facade and
 * starting the GUI. To adapt this application for different start-up scenarios,
 * you can modify the main method.
 */
public class App {

	/**
	 * Main method which boots up the application. Here you can pass command-line
	 * arguments if needed in the future. Currently, the program does not use
	 * command-line arguments.
	 * 
	 * @param args Command line arguments passed to the program (not currently
	 *             used).
	 */
	public static void main(String[] args) {
		// Initialize the facade with a configuration parameter (e.g., 100).
		// If the initial configuration needs to change, modify this value.
		Facade facade = new Facade(100); // Consider extracting '100' as a constant if it's a significant figure.

		// Calls the method to display the Graphical User Interface (GUI).
		// To change the way the GUI is displayed or to initialize it with specific
		// data,
		// you might want to modify the showGUI method within the Facade class.
		facade.showGUI();
	}
}
