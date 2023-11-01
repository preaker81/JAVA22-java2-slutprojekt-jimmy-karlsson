package com.slutprojekt.JimmyKarlsson.model;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import com.slutprojekt.JimmyKarlsson.utils.HelperMethods;

public class LoadBalancer {
	// Lists to hold the producer and consumer threads and instances
	private final List<Thread> producerThreads = new CopyOnWriteArrayList<>();
	private final List<Thread> consumerThreads = new ArrayList<>();
	private final List<Producer> producerInstances = new ArrayList<>();

	private final Buffer buffer; // Shared buffer
	private final ExecutorService executor; // Executor service to manage threads

	// Constructor
	public LoadBalancer(int bufferCapacity) {
		this.buffer = new Buffer(bufferCapacity);
		this.executor = Executors.newCachedThreadPool();
	}

	// Initialize consumers with random count
	public void initializeConsumers() {
		int randomConsumerCount = HelperMethods.getRandomIntBetween(3, 15);
		for (int i = 0; i < randomConsumerCount; i++) {
			Consumer consumer = new Consumer(HelperMethods.getRandomIntBetween(1, 10), buffer);
			Thread consumerThread = new Thread(consumer);
			consumerThreads.add(consumerThread);
			executor.execute(consumerThread);
			System.out.println(consumer.getDelay());
		}
	}

	// Add a new producer
	public void addProducer(int delay, Item item) {
		Producer producer = new Producer(delay, buffer, item);
		Thread producerThread = new Thread(producer);
		producerInstances.add(producer);
		producerThreads.add(producerThread);
		executor.execute(producerThread);
	}

	// Remove the last added producer
	public void removeProducer() {
		if (!producerThreads.isEmpty()) {
			Thread toRemove = producerThreads.remove(producerThreads.size() - 1);
			Producer producerInstance = producerInstances.remove(producerInstances.size() - 1);
			producerInstance.shutdown();
			toRemove.interrupt();
		}
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

	public List<Thread> getConsumerThreads() {
		return consumerThreads;
	}

	public List<Integer> getProducerIntervals() {
		return producerInstances.stream().map(Producer::getDelay).collect(Collectors.toList());
	}
}
