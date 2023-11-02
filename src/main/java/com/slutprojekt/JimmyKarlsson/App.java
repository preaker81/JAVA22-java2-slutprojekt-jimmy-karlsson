package com.slutprojekt.JimmyKarlsson;

import com.slutprojekt.JimmyKarlsson.controller.Facade;

public class App {
	public static void main(String[] args) {
		Facade facade = new Facade(100);
		facade.showGUI();
	}
}
