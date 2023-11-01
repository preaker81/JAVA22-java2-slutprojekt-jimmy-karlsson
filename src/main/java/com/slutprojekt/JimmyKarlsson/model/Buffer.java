package com.slutprojekt.JimmyKarlsson.model;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class Buffer {

	private BlockingQueue<Item> buffer; // The actual buffer
	private final PropertyChangeSupport support; // Property change support for listeners

	// Constructor
	public Buffer(int capacity) {
		this.buffer = new ArrayBlockingQueue<>(capacity);
		this.support = new PropertyChangeSupport(this);
	}

	// Add a property change listener
	public void addPropertyChangeListener(PropertyChangeListener pcl) {
		support.addPropertyChangeListener(pcl);
	}

	// Remove a property change listener
	public void removePropertyChangeListener(PropertyChangeListener pcl) {
		support.removePropertyChangeListener(pcl);
	}

	// Add an item to the buffer
	public void put(Item item) throws InterruptedException {
		int oldSize = buffer.size();
		buffer.put(item);
		int newSize = buffer.size();
		support.firePropertyChange("bufferSize", oldSize, newSize);
	}

	// Take an item from the buffer
	public Item take() throws InterruptedException {
		int oldSize = buffer.size();
		Item item = buffer.take();
		int newSize = buffer.size();
		support.firePropertyChange("bufferSize", oldSize, newSize);
		return item;
	}

	// Get buffer capacity
	public int getCapacity() {
		return this.buffer.remainingCapacity() + this.buffer.size();
	}

	// Get current buffer size
	public int getCurrentSize() {
		return this.buffer.size();
	}

	public void setCapacity(int newCapacity) {
		// Assuming the buffer isn't accessed by other threads while changing capacity
		BlockingQueue<Item> newBuffer = new ArrayBlockingQueue<>(newCapacity);
		newBuffer.addAll(this.buffer);
		this.buffer = newBuffer;
	}

	public void clear() {
		// Clear the buffer
		this.buffer.clear();
	}
}
