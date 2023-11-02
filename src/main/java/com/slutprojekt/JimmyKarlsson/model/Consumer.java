package com.slutprojekt.JimmyKarlsson.model;

public class Consumer implements Runnable {

	private final Buffer buffer;
	private final int delay;
	private volatile boolean shutdown = false;

	public Consumer(int delay, Buffer buffer) {
		this.buffer = buffer;
		this.delay = delay;
	}

	@Override
	public void run() {
		while (!shutdown) {
			try {
				buffer.take();
				Thread.sleep(delay * 1000);
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				return;
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
