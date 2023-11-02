package com.slutprojekt.JimmyKarlsson.model;

import java.util.concurrent.TimeUnit;

/**
 * Producer is responsible for producing items and placing them into a shared
 * buffer.
 */
public class Producer implements Runnable {

	private final Buffer buffer; // Shared buffer where items are placed.
	private final int delayInSeconds; // Delay between producing items.
	private final Item item; // The item that this producer creates.
	private volatile boolean shutdown = false; // Control flag for stopping the producer thread.

	/**
	 * Constructor for Producer.
	 *
	 * @param delayInSeconds The delay between item productions.
	 * @param buffer         The shared buffer to place items in.
	 * @param item           The item to produce.
	 */
	public Producer(int delayInSeconds, Buffer buffer, Item item) {
		this.buffer = buffer;
		this.delayInSeconds = delayInSeconds;
		this.item = item;
	}

	/**
	 * Initiates the shutdown process for the producer.
	 */
	public void shutdown() {
		this.shutdown = true;
	}

	/**
	 * The producer's run method that continuously produces items until shutdown.
	 */
	@Override
	public void run() {
		while (!shutdown) { // Keep running until shutdown is initiated.
			try {
				buffer.put(item); // Place the produced item into the buffer.
				TimeUnit.SECONDS.sleep(delayInSeconds); // Sleep for the specified delay.
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt(); // Re-interrupt the thread.
				handleInterruptedException(e); // Handle the interrupted exception.
			}
		}
	}

	/**
	 * Handles the InterruptedException thrown when the thread is interrupted.
	 *
	 * @param e The caught InterruptedException.
	 */
	private void handleInterruptedException(InterruptedException e) {
		// Log the exception or perform additional exception handling as needed.
	}

	// Getter methods

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
