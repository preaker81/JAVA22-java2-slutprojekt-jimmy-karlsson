package com.slutprojekt.JimmyKarlsson.model;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.stream.Collectors;

import com.slutprojekt.JimmyKarlsson.model.interfaces.BufferSizeProvider;
import com.slutprojekt.JimmyKarlsson.utils.Utilities;

/**
 * The LoadBalancer class manages producers and consumers to balance load in a
 * system. It implements a BufferSizeProvider interface to provide information
 * about the buffer state.
 */
public class LoadBalancer implements BufferSizeProvider {

	// Thread-safe list to hold producer tasks
	private final List<Runnable> producerTasks = new CopyOnWriteArrayList<>();
	// List to hold consumer tasks
	private final List<Runnable> consumerTasks = new ArrayList<>();
	// Support for property change events
	private final PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);
	// Buffer used between producers and consumers
	private final Buffer buffer;
	// Executor that manages the threading for consumers and producers
	private final ThreadPoolExecutor executor;

	/**
	 * Constructs a LoadBalancer with a specified buffer capacity.
	 *
	 * @param bufferCapacity The capacity of the buffer to be used by this load
	 *                       balancer
	 */
	public LoadBalancer(int bufferCapacity) {
		this.buffer = new Buffer(bufferCapacity);
		this.executor = (ThreadPoolExecutor) Executors.newCachedThreadPool();
	}

	/**
	 * Initializes a random number of consumer tasks.
	 */
	public void initializeConsumers() {
		int randomConsumerCount = Utilities.getRandomIntBetween(3, 15);
		for (int i = 0; i < randomConsumerCount; i++) {
			Consumer consumer = new Consumer(Utilities.getRandomIntBetween(1, 10), buffer);
			consumerTasks.add(consumer);
			executor.execute(consumer);
		}
	}

	/**
	 * Shuts down all tasks and clears the task list.
	 *
	 * @param tasks List of tasks (either producers or consumers) to shut down
	 */
	private void shutdownTasks(List<Runnable> tasks) {
		tasks.forEach(task -> {
			if (task instanceof Consumer) {
				((Consumer) task).shutdown();
			} else if (task instanceof Producer) {
				((Producer) task).shutdown();
			}
		});
		tasks.clear();
	}

	/**
	 * Adds a producer to the load balancer with a specified delay and item.
	 *
	 * @param delay The delay for the producer
	 * @param item  The item that the producer will produce
	 */
	public void addProducer(int delay, Item item) {
		Producer producer = new Producer(delay, buffer, item);
		producerTasks.add(producer);
		executor.execute(producer);
	}

	/**
	 * Removes the most recently added producer from the load balancer.
	 */
	public void removeProducer() {
		if (!producerTasks.isEmpty()) {
			Runnable toRemove = producerTasks.remove(producerTasks.size() - 1);
			if (toRemove instanceof Producer) {
				((Producer) toRemove).shutdown();
			}
		}
	}

	// Shuts down all producer tasks
	public void shutdownProducers() {
		shutdownTasks(producerTasks);
	}

	// Shuts down all consumer tasks
	public void shutdownConsumers() {
		shutdownTasks(consumerTasks);
	}

	/**
	 * Extracts the current state of the LoadBalancer.
	 *
	 * @return the current state as a LoadBalancerState object
	 */
	public LoadBalancerState extractState() {
		return new LoadBalancerState(getProducerIntervals(), getConsumerIntervals(), buffer.getCapacity(),
				buffer.getCurrentSize());
	}

	/**
	 * Applies a given state to the LoadBalancer.
	 *
	 * @param state The state to apply to the load balancer
	 */
	public void applyState(LoadBalancerState state) {
		shutdownConsumers();
		shutdownProducers();

		buffer.clear();
		buffer.setCapacityAndFill(state.bufferCapacity(), state.currentBufferSize());

		state.producerDelays().forEach(delay -> addProducer(delay, new Item()));
		state.consumerDelays().forEach(this::initializeSingleConsumer);

		propertyChangeSupport.firePropertyChange("producerCount", -1, getProducerCount());
	}

	/**
	 * Initializes a single consumer with a specified delay.
	 *
	 * @param delay The delay for the consumer
	 */
	private void initializeSingleConsumer(int delay) {
		Consumer consumer = new Consumer(delay, buffer);
		consumerTasks.add(consumer);
		executor.execute(consumer);
	}

	// Add a listener for property changes
	public void addPropertyChangeListener(PropertyChangeListener listener) {
		propertyChangeSupport.addPropertyChangeListener(listener);
	}

	// Remove a listener for property changes
	public void removePropertyChangeListener(PropertyChangeListener listener) {
		propertyChangeSupport.removePropertyChangeListener(listener);
	}

	// Getters for buffer, producer count, and intervals
	public Buffer getBuffer() {
		return this.buffer;
	}

	public int getProducerCount() {
		return producerTasks.size();
	}

	public List<Integer> getProducerIntervals() {
		return producerTasks.stream().filter(task -> task instanceof Producer).map(task -> ((Producer) task).getDelay())
				.collect(Collectors.toList());
	}

	public List<Integer> getConsumerIntervals() {
		return consumerTasks.stream().filter(task -> task instanceof Consumer).map(task -> ((Consumer) task).getDelay())
				.collect(Collectors.toList());
	}

	// BufferSizeProvider interface methods to get the current size and capacity of
	// the buffer
	@Override
	public int getCurrentSize() {
		return buffer.getCurrentSize();
	}

	@Override
	public int getCapacity() {
		return buffer.getCapacity();
	}
}
