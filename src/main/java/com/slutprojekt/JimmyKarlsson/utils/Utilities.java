package com.slutprojekt.JimmyKarlsson.utils;

import java.util.Random;

/**
 * This class contains utility methods to assist with common tasks that do not
 * fit within a specific domain logic class.
 */
public class Utilities {

	/**
	 * Generates a random integer between the specified minimum and maximum values,
	 * inclusive. This method can be used when a random number within a range is
	 * needed, for example, to simulate the loading time of a process or select a
	 * random element from an array.
	 *
	 * @param min The minimum value of the range (inclusive).
	 * @param max The maximum value of the range (inclusive).
	 * @return A random integer between min and max, inclusive.
	 * @throws IllegalArgumentException If max is less than min.
	 */
	public static int getRandomIntBetween(int min, int max) {
		// Throw an exception if max is less than min to prevent errors in random number
		// generation.
		if (max < min) {
			throw new IllegalArgumentException("Max must be greater than or equal to min.");
		}

		// Create a Random instance to generate random numbers.
		Random random = new Random();

		// Generate a random integer between min (inclusive) and max (inclusive).
		// The calculation (max - min) + 1) generates a range of values that includes
		// the maximum value,
		// and adding the minimum value offsets the range to start from the minimum
		// value.
		return random.nextInt((max - min) + 1) + min;
	}
}
