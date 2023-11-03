package com.slutprojekt.JimmyKarlsson.utils;

import java.util.Random;

/**
 * This class contains utility methods to assist with common tasks across
 * different parts of the application. It acts as a helper class, providing
 * static methods that are not associated with specific objects. These methods
 * can be used globally without the need for creating an instance of Utilities.
 */
public class Utilities {

	/**
	 * Generates a random integer between the specified minimum and maximum values,
	 * inclusive. The use of {@link Random} class ensures that the numbers are
	 * evenly distributed within the range.
	 * 
	 * This method can be particularly useful in scenarios such as: - Simulating the
	 * roll of a dice. - Picking a random index for an array. - Introducing
	 * randomness into simulations or games.
	 *
	 * @param min The minimum value of the range (inclusive). This is the lowest
	 *            possible number that can be returned.
	 * @param max The maximum value of the range (inclusive). This is the highest
	 *            possible number that can be returned.
	 * @return A random integer between min and max, inclusive.
	 * @throws IllegalArgumentException If max is less than min, which would make
	 *                                  the range invalid.
	 */
	public static int getRandomIntBetween(int min, int max) {
		// Validate the range. The if-statement ensures that the method does not proceed
		// with an invalid range,
		// which could lead to an incorrect result or an exception during the execution
		// of the Random#nextInt() method.
		if (max < min) {
			throw new IllegalArgumentException("Max must be greater than or equal to min.");
		}

		Random random = new Random();

		// Calculate the random integer. The formula used here adjusts the range to
		// start from the minimum value 'min'
		// and extends to the range size, ensuring 'max' is a possible result.
		// (random.nextInt(n) returns a value between 0 (inclusive) and the specified
		// value n (exclusive)).
		return random.nextInt((max - min) + 1) + min;
	}
}
