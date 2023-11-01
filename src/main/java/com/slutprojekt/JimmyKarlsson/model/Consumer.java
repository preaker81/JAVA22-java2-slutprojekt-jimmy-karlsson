package com.slutprojekt.JimmyKarlsson.model;

public class Consumer implements Runnable {

	private final Buffer buffer; // Shared buffer between producers and consumers
	private final int delay; // Delay in seconds
	private volatile boolean shutdown = false;

	// Constructor
	public Consumer(int delay, Buffer buffer) {
		this.buffer = buffer;
		this.delay = delay;
	}

	// This is the core logic for the Consumer. It's an overridden method from
	// Runnable interface.
	@Override
	public void run() {
		while (!shutdown) {
			try {
				buffer.take(); // Take item from the buffer
				Thread.sleep(delay * 1000); // Simulate work by sleeping
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				return; // Exit if interrupted
			}
		}
	}

	public void shutdown() {
		this.shutdown = true;
	}

	// Getter methods

	public Buffer getBuffer() {
		return buffer;
	}

	public int getDelay() {
		return delay;
	}
}
