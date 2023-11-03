package com.slutprojekt.JimmyKarlsson.model;

import java.util.concurrent.TimeUnit;

/**
 * The Consumer class is designed to consume items from a shared buffer. It
 * implements the Runnable interface, allowing instances of this class to be
 * executed by a Thread. The consumption process can be started and stopped as
 * needed.
 */
public class Consumer implements Runnable {

	private final Buffer buffer; // Shared buffer from which items are consumed.
	private final int delayInSeconds; // Time delay in seconds between consuming items.
	private volatile boolean shutdown = false; // Flag to signal the consumer to stop running.

	/**
	 * Constructs a new Consumer that will take items from the specified buffer.
	 *
	 * @param delayInSeconds The delay in seconds between each item consumption.
	 * @param buffer         The shared buffer from which items will be consumed.
	 */
	public Consumer(int delayInSeconds, Buffer buffer) {
		this.buffer = buffer;
		this.delayInSeconds = delayInSeconds;
	}

	/**
	 * The main running method for the Consumer thread. Continuously attempts to
	 * consume items from the buffer at a specified delay interval until the
	 * shutdown signal is received.
	 */
	@Override
	public void run() {
		while (!shutdown) {
			try {
				buffer.take(); // Consume an item from the buffer.
				TimeUnit.SECONDS.sleep(delayInSeconds); // Pause the thread for the delay period.
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt(); // Ensures the thread maintains its interrupted status.
				handleInterruptedException(e); // Custom handler for the interruption.
			}
		}
	}

	/**
	 * Triggers the shutdown of the consumer thread, stopping it from consuming any
	 * more items.
	 */
	public void shutdown() {
		this.shutdown = true;
	}

	/**
	 * Handles what should occur when an InterruptedException is thrown during the
	 * consumer's operation.
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
}
