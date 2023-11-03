package com.slutprojekt.JimmyKarlsson.model;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

/**
 * The Buffer class provides a thread-safe mechanism for storing and
 * transferring Item objects between producer and consumer threads. It
 * implements a finite blocking queue with the ability to notify observers about
 * changes in its capacity and content size, ensuring safe and efficient
 * multi-threaded operations. The Buffer is designed to operate in any context
 * where a fixed-size queue for items is required and the observation of content
 * changes is necessary.
 */
public class Buffer {

	// The queue that holds the items. It is thread-safe which ensures that
	// put and take operations can happen concurrently without data corruption.
	private BlockingQueue<Item> itemsQueue;

	// This support class handles the observation mechanism, allowing external
	// entities
	// to subscribe and get notifications when the buffer's state changes.
	private final PropertyChangeSupport propertyChangeSupport;

	/**
	 * Constructs a Buffer with the specified capacity.
	 *
	 * @param capacity the fixed size of the buffer
	 */
	public Buffer(int capacity) {
		// Initialize the queue with the given capacity.
		this.itemsQueue = new ArrayBlockingQueue<>(capacity);
		this.propertyChangeSupport = new PropertyChangeSupport(this);
	}

	// Listener management methods

	public void addPropertyChangeListener(PropertyChangeListener listener) {
		propertyChangeSupport.addPropertyChangeListener(listener);
	}

	public void removePropertyChangeListener(PropertyChangeListener listener) {
		propertyChangeSupport.removePropertyChangeListener(listener);
	}

	// Buffer operation methods

	public void put(Item item) throws InterruptedException {
		int oldSize = getCurrentSize(); // Store the current size for later comparison.
		itemsQueue.put(item); // Add the item, waiting if necessary for space to become available.
		fireSizeChange(oldSize, getCurrentSize()); // Notify listeners if there's a size change.
	}

	public Item take() throws InterruptedException {
		int oldSize = getCurrentSize(); // Store the current size for later comparison.
		Item item = itemsQueue.take(); // Remove and return the head item, waiting if necessary.
		fireSizeChange(oldSize, getCurrentSize()); // Notify listeners if there's a size change.
		return item;
	}

	// Buffer property methods

	public int getCapacity() {
		// The total capacity is the sum of the remaining capacity and the current size.
		return itemsQueue.remainingCapacity() + getCurrentSize();
	}

	public int getCurrentSize() {
		// The current size is the number of items present in the queue.
		return itemsQueue.size();
	}

	// Special operations

	public synchronized void setCapacityAndFill(int newCapacity, int itemsToFill) {
		// Validate input to prevent misuse of the method.
		if (newCapacity < itemsToFill) {
			throw new IllegalArgumentException("New capacity cannot be less than the number of items to fill.");
		}

		int oldSize = getCurrentSize(); // Capture the current size for notification purposes.
		BlockingQueue<Item> newBuffer = new ArrayBlockingQueue<>(newCapacity);
		itemsQueue.drainTo(newBuffer, itemsToFill); // Drain the required number of items to the new buffer.

		// Add new items to the buffer until it reaches the specified number.
		while (newBuffer.size() < itemsToFill) {
			newBuffer.add(new Item()); // Potentially replace with actual item creation logic.
		}

		itemsQueue = newBuffer; // Replace the current buffer with the new buffer.
		fireSizeChange(oldSize, getCurrentSize()); // Notify if the resize resulted in a size change.
	}

	public void clear() {
		itemsQueue.clear(); // Clears all items from the queue.
	}

	// Private helper methods

	private void fireSizeChange(int oldSize, int newSize) {
		// Notify all subscribed listeners about the size change.
		propertyChangeSupport.firePropertyChange("bufferSize", oldSize, newSize);
	}
}
