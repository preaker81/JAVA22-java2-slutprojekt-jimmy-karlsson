package com.slutprojekt.JimmyKarlsson;

import com.slutprojekt.JimmyKarlsson.utils.LoggerSingleton;

public class App {
	public static void main(String[] args) {
		System.out.println("Hello World!");

		LoggerSingleton.getInstance().info("Hello World!");
	}
}
