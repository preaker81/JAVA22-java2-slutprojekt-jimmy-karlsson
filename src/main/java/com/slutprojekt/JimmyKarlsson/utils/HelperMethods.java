package com.slutprojekt.JimmyKarlsson.utils;

import java.util.Random;

public class HelperMethods {

	// Generate random int between min and max, inclusive
	public static int getRandomIntBetween(int min, int max) {
		Random random = new Random();
		return random.nextInt((max - min) + 1) + min;
	}
}
