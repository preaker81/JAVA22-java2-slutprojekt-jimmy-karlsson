package com.slutprojekt.JimmyKarlsson.model;

import java.util.concurrent.TimeUnit;

/**
 * The Producer class is responsible for producing items and putting them into a
 * shared buffer. It implements the Runnable interface, which allows instances
 * of this class to be executed by a Thread. The production process can be
 * initiated and terminated as necessary.
 */
public class Producer implements Runnable {

	private final Buffer buffer; // Shared buffer into which items are placed.
	private final int delayInSeconds; // Time delay in seconds between producing items.
	private final Item item; // Template item that this producer will produce and place into the buffer.
	private volatile boolean shutdown = false; // Flag to signal the producer to stop running.

	/**
	 * Constructs a new Producer that will produce items and place them into the
	 * specified buffer.
	 *
	 * @param delayInSeconds The delay in seconds between producing each item.
	 * @param buffer         The shared buffer into which produced items will be
	 *                       placed.
	 * @param item           The template of the item to be produced.
	 */
	public Producer(int delayInSeconds, Buffer buffer, Item item) {
		this.buffer = buffer;
		this.delayInSeconds = delayInSeconds;
		this.item = item;
	}

	/**
	 * Initiates the shutdown of the producer thread, which stops it from producing
	 * any further items.
	 */
	public void shutdown() {
		this.shutdown = true;
	}

	/**
	 * The main running method for the Producer thread. Continuously produces items
	 * and places them into the buffer at a specified delay interval until the
	 * shutdown signal is received.
	 */
	@Override
	public void run() {
		while (!shutdown) {
			try {
				buffer.put(item); // Add a new item to the buffer.
				TimeUnit.SECONDS.sleep(delayInSeconds); // Pause the thread for the delay period.
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt(); // Ensures the thread maintains its interrupted status.
				handleInterruptedException(e); // Custom handler for the interruption.
			}
		}
	}

	/**
	 * Handles what should occur when an InterruptedException is thrown during the
	 * producer's operation.
	 *
	 * @param e The caught InterruptedException.
	 */
	private void handleInterruptedException(InterruptedException e) {
		// Log the exception or perform additional actions as needed upon interruption.
	}

	// Accessor methods

	public Buffer getBuffer() {
		return buffer;
	}

	public int getDelay() {
		return delayInSeconds;
	}

	public Item getItem() {
		return item;
	}
}
