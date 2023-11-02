package com.slutprojekt.JimmyKarlsson.model;

import java.util.concurrent.TimeUnit;

/**
 * Consumer is responsible for consuming items from a shared buffer.
 */
public class Consumer implements Runnable {

	private final Buffer buffer; // Shared buffer from which items are consumed.
	private final int delayInSeconds; // Delay between consuming items.
	private volatile boolean shutdown = false; // Control flag for stopping the consumer thread.

	/**
	 * Constructor for Consumer.
	 *
	 * @param delayInSeconds The delay between item consumption.
	 * @param buffer         The shared buffer from which to consume items.
	 */
	public Consumer(int delayInSeconds, Buffer buffer) {
		this.buffer = buffer;
		this.delayInSeconds = delayInSeconds;
	}

	/**
	 * The consumer's run method that continuously consumes items until shutdown.
	 */
	@Override
	public void run() {
		while (!shutdown) { // Keep running until shutdown is initiated.
			try {
				buffer.take(); // Take (consume) an item from the buffer.
				TimeUnit.SECONDS.sleep(delayInSeconds); // Sleep for the specified delay.
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt(); // Re-interrupt the thread.
				handleInterruptedException(e); // Handle the interrupted exception.
			}
		}
	}

	/**
	 * Initiates the shutdown process for the consumer.
	 */
	public void shutdown() {
		this.shutdown = true;
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
}
