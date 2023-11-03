package com.slutprojekt.JimmyKarlsson.model.interfaces;

import java.util.List;

/**
 * Interface defining the methods to be implemented for providing buffer size
 * information. This interface can be used to abstract the details of how buffer
 * size and capacity are retrieved in a producer-consumer scenario.
 */
public interface BufferSizeProvider {
	/**
	 * Gets the current number of items in the buffer.
	 * 
	 * @return the current size of the buffer
	 */
	int getCurrentSize();

	/**
	 * Retrieves the maximum number of items that the buffer can hold.
	 * 
	 * @return the buffer's capacity
	 */
	int getCapacity();

	/**
	 * Provides the intervals at which producers generate new items. This list helps
	 * in understanding the production rate and can be used for configuring producer
	 * timing or handling load balancing.
	 * 
	 * @return a list of intervals in seconds at which producers work
	 */
	List<Integer> getProducerIntervals();
}
