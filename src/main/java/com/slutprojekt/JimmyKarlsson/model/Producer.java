package com.slutprojekt.JimmyKarlsson.model;

public class Producer implements Runnable {

	private final Buffer buffer; // Shared buffer between producers and consumers
	private final int delay; // Delay in seconds
	private final Item item; // The item to be produced
	private volatile boolean shutdown = false; // Flag to shut down the producer

	// Constructor
	public Producer(int delay, Buffer buffer, Item item) {
		this.buffer = buffer;
		this.delay = delay;
		this.item = item;
	}

	// Shutdown the producer
	public void shutdown() {
		this.shutdown = true;
	}

	// This is the core logic for the Producer. It's an overridden method from
	// Runnable interface.
	@Override
	public void run() {
		while (!shutdown) {
			try {
				buffer.put(item); // Put item into the buffer
				Thread.sleep(delay * 1000); // Simulate time needed to produce item
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				return; // Exit if interrupted
			}
		}
	}

	// Getter methods
	public Buffer getBuffer() {
		return buffer;
	}

	public int getDelay() {
		return delay;
	}

	public Item getItem() {
		return item;
	}
}
