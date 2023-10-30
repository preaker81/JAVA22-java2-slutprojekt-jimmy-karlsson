package com.slutprojekt.JimmyKarlsson.model;

public class Consumer implements Runnable {

	private final Buffer buffer; // Use your Buffer class instead of BlockingQueue directly
	private final int delay; // Delay in seconds between item removals

	// Updated constructor to take delay and buffer
	public Consumer(int delay, Buffer buffer) {
		this.buffer = buffer;
		this.delay = delay;
	}

	@Override
	public void run() {
		while (true) {
			try {
				// Using buffer's BlockingQueue to take the item
				buffer.take();

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

}
