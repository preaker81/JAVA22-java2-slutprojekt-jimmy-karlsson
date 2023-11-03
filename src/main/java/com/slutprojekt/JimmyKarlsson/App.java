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
		Facade facade = new Facade(100);
		facade.showGUI();
	}
}
