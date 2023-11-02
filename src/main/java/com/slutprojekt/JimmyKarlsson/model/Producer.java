package com.slutprojekt.JimmyKarlsson.model;

public class Producer implements Runnable {

	private final Buffer buffer;
	private final int delay;
	private final Item item;
	private volatile boolean shutdown = false;

	public Producer(int delay, Buffer buffer, Item item) {
		this.buffer = buffer;
		this.delay = delay;
		this.item = item;
	}

	public void shutdown() {
		this.shutdown = true;
	}

	@Override
	public void run() {
		while (!shutdown) {
			try {
				buffer.put(item);
				Thread.sleep(delay * 1000);
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				return;
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
