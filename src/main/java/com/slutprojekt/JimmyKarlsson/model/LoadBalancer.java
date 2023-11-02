package com.slutprojekt.JimmyKarlsson.model;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import com.slutprojekt.JimmyKarlsson.utils.HelperMethods;

public class LoadBalancer {
	private final List<Thread> producerThreads = new CopyOnWriteArrayList<>();
	private final List<Thread> consumerThreads = new ArrayList<>();
	private final List<Producer> producerInstances = new ArrayList<>();
	private final List<Consumer> consumerInstances = new ArrayList<>();
	private PropertyChangeSupport pcs = new PropertyChangeSupport(this);
	private final Buffer buffer;
	private final ExecutorService executor;

	public LoadBalancer(int bufferCapacity) {
		this.buffer = new Buffer(bufferCapacity);
		this.executor = Executors.newCachedThreadPool();
	}

	public void initializeConsumers() {
		int randomConsumerCount = HelperMethods.getRandomIntBetween(3, 15);
		for (int i = 0; i < randomConsumerCount; i++) {
			Consumer consumer = new Consumer(HelperMethods.getRandomIntBetween(1, 10), buffer);
			Thread consumerThread = new Thread(consumer);
			consumerThreads.add(consumerThread);
			executor.execute(consumerThread);
		}
	}

	public void shutdownConsumers() {
		consumerThreads.forEach(Thread::interrupt);
		consumerInstances.forEach(Consumer::shutdown);
		consumerInstances.clear();
		consumerThreads.clear();
	}

	public void addProducer(int delay, Item item) {
		Producer producer = new Producer(delay, buffer, item);
		Thread producerThread = new Thread(producer);
		producerInstances.add(producer);
		producerThreads.add(producerThread);
		executor.execute(producerThread);
	}

	public void removeProducer() {
		if (!producerThreads.isEmpty()) {
			Thread toRemove = producerThreads.remove(producerThreads.size() - 1);
			Producer producerInstance = producerInstances.remove(producerInstances.size() - 1);
			producerInstance.shutdown();
			toRemove.interrupt();
		}
	}

	public void shutdownProducers() {
		for (Producer producer : producerInstances) {
			producer.shutdown();
		}
		for (Thread producerThread : producerThreads) {
			producerThread.interrupt();
			try {
				producerThread.join();
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();

			}
		}

		producerInstances.clear();
		producerThreads.clear();
	}

	public LoadBalancerState extractState() {
		return new LoadBalancerState(getProducerIntervals(), getConsumerIntervals(), buffer.getCapacity(),
				buffer.getCurrentSize());
	}

	public void applyState(LoadBalancerState state) {
		shutdownConsumers();
		shutdownProducers();

		producerInstances.clear();
		producerThreads.clear();
		consumerInstances.clear();
		consumerThreads.clear();

		buffer.clear();
		buffer.setCapacity(state.bufferCapacity());

		for (int delay : state.producerDelays()) {
			addProducer(delay, new Item());
		}

		for (int delay : state.consumerDelays()) {
			Consumer consumer = new Consumer(delay, buffer);
			Thread consumerThread = new Thread(consumer);
			consumerInstances.add(consumer);
			consumerThreads.add(consumerThread);
			executor.execute(consumerThread);
		}

		pcs.firePropertyChange("producerCount", -1, getProducerCount());
	}

	public void addPropertyChangeListener(PropertyChangeListener listener) {
		pcs.addPropertyChangeListener(listener);
	}

	// Getter methods

	public Buffer getBuffer() {
		return this.buffer;
	}

	public int getProducerCount() {
		return producerThreads.size();
	}

	public List<Thread> getProducerThreads() {
		return producerThreads;
	}

	public List<Integer> getProducerIntervals() {
		return producerInstances.stream().map(Producer::getDelay).collect(Collectors.toList());
	}

	public List<Integer> getConsumerIntervals() {
		return consumerInstances.stream().map(Consumer::getDelay).collect(Collectors.toList());
	}
}
