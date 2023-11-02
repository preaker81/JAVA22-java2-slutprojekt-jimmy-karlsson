package com.slutprojekt.JimmyKarlsson.model;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.stream.Collectors;

import com.slutprojekt.JimmyKarlsson.utils.Utilities;

/**
 * LoadBalancer class manages producer and consumer tasks using a buffer. It
 * utilizes a ThreadPoolExecutor to manage threads and PropertyChangeSupport for
 * event handling.
 */
public class LoadBalancer {
	// A thread-safe list to hold the producer tasks.
	private final List<Runnable> producerTasks = new CopyOnWriteArrayList<>();
	// A regular list to hold the consumer tasks.
	private final List<Runnable> consumerTasks = new ArrayList<>();
	// Support for property change events to handle changes and notify listeners.
	private final PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);
	// The buffer instance to be used by producers and consumers for storing items.
	private final Buffer buffer;
	// A thread pool executor to manage threading for producers and consumers.
	private final ThreadPoolExecutor executor;

	/**
	 * Constructor for LoadBalancer initializing the buffer and executor.
	 *
	 * @param bufferCapacity the capacity for the buffer.
	 */
	public LoadBalancer(int bufferCapacity) {
		this.buffer = new Buffer(bufferCapacity);
		this.executor = (ThreadPoolExecutor) Executors.newCachedThreadPool();
	}

	/**
	 * Initializes a random number of consumer tasks and starts their execution.
	 */
	public void initializeConsumers() {
		// Generate a random number of consumers between 3 and 15.
		int randomConsumerCount = Utilities.getRandomIntBetween(3, 15);
		for (int i = 0; i < randomConsumerCount; i++) {
			// Create a new consumer with a random delay and the shared buffer.
			Consumer consumer = new Consumer(Utilities.getRandomIntBetween(1, 10), buffer);
			// Add to the list of consumer tasks.
			consumerTasks.add(consumer);
			// Start the consumer's execution in the thread pool.
			executor.execute(consumer);
		}
	}

	/**
	 * Shuts down and clears the list of tasks provided, either producer or consumer
	 * tasks.
	 *
	 * @param tasks the list of tasks to shut down.
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
	 * Adds a producer task to the list and starts its execution.
	 *
	 * @param delay the delay for the producer.
	 * @param item  the item that the producer will work with.
	 */
	public void addProducer(int delay, Item item) {
		Producer producer = new Producer(delay, buffer, item);
		// Add the new producer to the producer tasks list.
		producerTasks.add(producer);
		// Execute the producer in the thread pool.
		executor.execute(producer);
	}

	/**
	 * Removes the last producer from the list of producer tasks and shuts it down.
	 */
	public void removeProducer() {
		if (!producerTasks.isEmpty()) {
			// Remove the last producer from the list.
			Runnable toRemove = producerTasks.remove(producerTasks.size() - 1);
			if (toRemove instanceof Producer) {
				// Shut down the producer.
				((Producer) toRemove).shutdown();
			}
		}
	}

	/**
	 * Shuts down all producer tasks.
	 */
	public void shutdownProducers() {
		shutdownTasks(producerTasks);
	}

	/**
	 * Shuts down all consumer tasks.
	 */
	public void shutdownConsumers() {
		shutdownTasks(consumerTasks);
	}

	/**
	 * Extracts the current state of the load balancer, including the intervals and
	 * buffer status.
	 *
	 * @return a new instance of LoadBalancerState with the current state
	 *         information.
	 */
	public LoadBalancerState extractState() {
		// Create and return a LoadBalancerState object with current producer/consumer
		// intervals and buffer details.
		return new LoadBalancerState(getProducerIntervals(), getConsumerIntervals(), buffer.getCapacity(),
				buffer.getCurrentSize());
	}

	/**
	 * Applies a previously extracted state to the load balancer.
	 *
	 * @param state the state to apply to the load balancer.
	 */
	public void applyState(LoadBalancerState state) {
		// Shutdown all current consumers and producers before applying the new state.
		shutdownConsumers();
		shutdownProducers();

		// Clear the buffer and set its capacity and current fill state to match the
		// provided state.
		buffer.clear();
		buffer.setCapacityAndFill(state.bufferCapacity(), state.currentBufferSize());

		// Add producers and consumers as per the state's recorded delays.
		state.producerDelays().forEach(delay -> addProducer(delay, new Item()));
		state.consumerDelays().forEach(this::initializeSingleConsumer);

		// Notify any listeners about the change in producer count.
		propertyChangeSupport.firePropertyChange("producerCount", -1, getProducerCount());
	}

	/**
	 * Initializes a single consumer with the provided delay.
	 *
	 * @param delay the delay for the consumer.
	 */
	private void initializeSingleConsumer(int delay) {
		// Create a new consumer with the specified delay and the shared buffer.
		Consumer consumer = new Consumer(delay, buffer);
		// Add to the list of consumer tasks.
		consumerTasks.add(consumer);
		// Start the consumer's execution in the thread pool.
		executor.execute(consumer);
	}

	/**
	 * Adds a PropertyChangeListener to the list of listeners.
	 *
	 * @param listener the PropertyChangeListener to be added.
	 */
	public void addPropertyChangeListener(PropertyChangeListener listener) {
		propertyChangeSupport.addPropertyChangeListener(listener);
	}

	/**
	 * Removes a PropertyChangeListener from the list of listeners.
	 *
	 * @param listener the PropertyChangeListener to be removed.
	 */
	public void removePropertyChangeListener(PropertyChangeListener listener) {
		propertyChangeSupport.removePropertyChangeListener(listener);
	}

	// Getter methods with comments explaining what they do.

	/**
	 * Retrieves the buffer used by the load balancer.
	 *
	 * @return the buffer instance.
	 */
	public Buffer getBuffer() {
		return this.buffer;
	}

	/**
	 * Gets the count of producer tasks currently managed by the load balancer.
	 *
	 * @return the number of producer tasks.
	 */
	public int getProducerCount() {
		return producerTasks.size();
	}

	/**
	 * Retrieves the intervals (delays) for all producer tasks.
	 *
	 * @return a list of intervals for all producers.
	 */
	public List<Integer> getProducerIntervals() {
		// Stream the producerTasks, filter by Producer type, map to their delay, and
		// collect to a list.
		return producerTasks.stream().filter(task -> task instanceof Producer).map(task -> ((Producer) task).getDelay())
				.collect(Collectors.toList());
	}

	/**
	 * Retrieves the intervals (delays) for all consumer tasks.
	 *
	 * @return a list of intervals for all consumers.
	 */
	public List<Integer> getConsumerIntervals() {
		// Stream the consumerTasks, filter by Consumer type, map to their delay, and
		// collect to a list.
		return consumerTasks.stream().filter(task -> task instanceof Consumer).map(task -> ((Consumer) task).getDelay())
				.collect(Collectors.toList());
	}
}
