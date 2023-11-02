package com.slutprojekt.JimmyKarlsson.model;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class Buffer {

	private BlockingQueue<Item> buffer;
	private final PropertyChangeSupport support;

	public Buffer(int capacity) {
		this.buffer = new ArrayBlockingQueue<>(capacity);
		this.support = new PropertyChangeSupport(this);
	}

	public void addPropertyChangeListener(PropertyChangeListener pcl) {
		support.addPropertyChangeListener(pcl);
	}

	public void removePropertyChangeListener(PropertyChangeListener pcl) {
		support.removePropertyChangeListener(pcl);
	}

	public void put(Item item) throws InterruptedException {
		int oldSize = buffer.size();
		buffer.put(item);
		int newSize = buffer.size();
		support.firePropertyChange("bufferSize", oldSize, newSize);
	}

	public Item take() throws InterruptedException {
		int oldSize = buffer.size();
		Item item = buffer.take();
		int newSize = buffer.size();
		support.firePropertyChange("bufferSize", oldSize, newSize);
		return item;
	}

	public int getCapacity() {
		return this.buffer.remainingCapacity() + this.buffer.size();
	}

	public int getCurrentSize() {
		return this.buffer.size();
	}

	public void setCapacity(int newCapacity) {

		BlockingQueue<Item> newBuffer = new ArrayBlockingQueue<>(newCapacity);
		newBuffer.addAll(this.buffer);
		this.buffer = newBuffer;
	}

	public void clear() {

		this.buffer.clear();
	}
}
