package com.slutprojekt.JimmyKarlsson.model;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

/**
 * The Buffer class represents a thread-safe container for Item objects, with a
 * fixed capacity. It allows producer and consumer threads to safely add and
 * remove items, with support for notifying listeners about the size changes of
 * the buffer.
 */
public class Buffer {

	private BlockingQueue<Item> itemsQueue; // Thread-safe queue to hold items
	private final PropertyChangeSupport propertyChangeSupport; // Utility class to manage property change listeners

	/**
	 * Constructs a Buffer with the specified capacity.
	 *
	 * @param capacity the fixed size of the buffer
	 */
	public Buffer(int capacity) {
		this.itemsQueue = new ArrayBlockingQueue<>(capacity);
		this.propertyChangeSupport = new PropertyChangeSupport(this);
	}

	/**
	 * Adds a PropertyChangeListener to the listener list.
	 *
	 * @param pcl the PropertyChangeListener to be added
	 */
	public void addPropertyChangeListener(PropertyChangeListener listener) {
		propertyChangeSupport.addPropertyChangeListener(listener);
	}

	/**
	 * Removes a PropertyChangeListener from the listener list.
	 *
	 * @param pcl the PropertyChangeListener to be removed
	 */
	public void removePropertyChangeListener(PropertyChangeListener listener) {
		propertyChangeSupport.removePropertyChangeListener(listener);
	}

	/**
	 * Puts an item into the buffer, waiting if necessary for space to become
	 * available.
	 *
	 * @param item the item to be added to the buffer
	 * @throws InterruptedException if interrupted while waiting
	 */
	public void put(Item item) throws InterruptedException {
		int oldSize = getCurrentSize(); // Store current size before adding new item
		itemsQueue.put(item); // Adds the item, waits if no space is available
		fireSizeChange(oldSize, getCurrentSize()); // Notify listeners of size change
	}

	/**
	 * Takes an item from the buffer, waiting if necessary until an item becomes
	 * available.
	 *
	 * @return the item from the front of the buffer
	 * @throws InterruptedException if interrupted while waiting
	 */
	public Item take() throws InterruptedException {
		int oldSize = getCurrentSize(); // Store current size before taking an item
		Item item = itemsQueue.take(); // Removes and returns the head item, waits if necessary
		fireSizeChange(oldSize, getCurrentSize()); // Notify listeners of size change
		return item;
	}

	/**
	 * Gets the total capacity of the buffer.
	 *
	 * @return the capacity of the buffer
	 */
	public int getCapacity() {
		return itemsQueue.remainingCapacity() + getCurrentSize();
	}

	/**
	 * Gets the current number of items in the buffer.
	 *
	 * @return the current size of the buffer
	 */
	public int getCurrentSize() {
		return itemsQueue.size();
	}

	/**
	 * Resizes the buffer to the new capacity and fills it with a specified number
	 * of new items.
	 *
	 * @param newCapacity the new capacity of the buffer
	 * @param itemsToFill the number of items to fill the buffer with
	 */
	public synchronized void setCapacityAndFill(int newCapacity, int itemsToFill) {

		if (newCapacity < itemsToFill) {
			throw new IllegalArgumentException("New capacity cannot be less than the number of items to fill.");
		}

		int oldSize = getCurrentSize(); // Store current size for notification
		BlockingQueue<Item> newBuffer = new ArrayBlockingQueue<>(newCapacity);
		itemsQueue.drainTo(newBuffer, itemsToFill); // Drain items to new buffer

		// Fill the new buffer with new items until it reaches the desired number
		while (newBuffer.size() < itemsToFill) {
			newBuffer.add(new Item());
		}

		itemsQueue = newBuffer; // Replace the old buffer with the new buffer

		// Notify listeners if the size has changed as a result of resizing
		if (oldSize != getCurrentSize()) {
			fireSizeChange(oldSize, getCurrentSize());
		}
	}

	/**
	 * Clears all the items from the buffer.
	 */
	public void clear() {
		this.itemsQueue.clear(); // Clear the queue of all items
	}

	/**
	 * Notifies listeners of a change in the buffer size.
	 *
	 * @param oldSize the size of the buffer before the change
	 * @param newSize the size of the buffer after the change
	 */
	private void fireSizeChange(int oldSize, int newSize) {
		propertyChangeSupport.firePropertyChange("bufferSize", oldSize, newSize); // Notify listeners of the property
																					// change
	}
}
