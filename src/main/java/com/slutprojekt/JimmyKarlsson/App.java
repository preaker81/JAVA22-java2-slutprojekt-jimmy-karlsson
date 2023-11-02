package com.slutprojekt.JimmyKarlsson;

import com.slutprojekt.JimmyKarlsson.controller.Facade;

/**
 * The main application class that serves as the entry point to the program.
 */
public class App {

	/**
	 * The main method that starts the application.
	 * 
	 * @param args Command line arguments passed to the program (not used in this
	 *             application).
	 */
	public static void main(String[] args) {
		// Creates an instance of the Facade, passing an integer that could represent
		// an initial configuration value or parameter for the facade (e.g., initial
		// buffer size).
		Facade facade = new Facade(100);

		// Calls a method on the facade to display the graphical user interface.
		// This encapsulates all the GUI initialization code within the Facade class.
		facade.showGUI();
	}
}
