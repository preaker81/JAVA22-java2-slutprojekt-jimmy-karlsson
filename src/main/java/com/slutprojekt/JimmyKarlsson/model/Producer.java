package com.slutprojekt.JimmyKarlsson.model;

public class Producer implements Runnable {

	private final Buffer buffer; // Use your Buffer class instead of BlockingQueue directly
	private final int delay; // Delay in seconds between item insertions
	private final Item item; // The Item object to be produced
	private volatile boolean shutdown = false;

	// Updated constructor to take delay, buffer, and item
	public Producer(int delay, Buffer buffer, Item item) {
		this.buffer = buffer;
		this.delay = delay;
		this.item = item;
	}

	public void shutdown() {
		this.shutdown = true; // Simply set the shutdown flag
	}

	@Override
	public void run() {
		while (!shutdown) {
			try {
				// Using the item instance variable
				buffer.put(item); // Use the encapsulated put method in Buffer

				// Delay for 'delay' seconds
				Thread.sleep(delay * 1000);
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				return;
			}
		}
	}

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
