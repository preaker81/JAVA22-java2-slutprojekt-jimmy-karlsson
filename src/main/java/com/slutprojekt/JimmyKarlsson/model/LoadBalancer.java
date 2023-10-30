package com.slutprojekt.JimmyKarlsson.model;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import com.slutprojekt.JimmyKarlsson.utils.HelperMethods;

public class LoadBalancer {

	private final List<Thread> producerThreads = new ArrayList<>();
	private final List<Thread> consumerThreads = new ArrayList<>();
	private final List<Producer> producerInstances = new ArrayList<>();
	private final Buffer buffer;
	private final ExecutorService executor;

	public LoadBalancer(int bufferCapacity) {
		this.buffer = new Buffer(bufferCapacity);
		this.executor = Executors.newCachedThreadPool();
	}

	public void initializeConsumers() {
		int randomConsumerCount = HelperMethods.getRandomIntBetween(3, 15);
		for (int i = 0; i < randomConsumerCount; i++) {
			Consumer consumer = new Consumer(1, buffer);
			Thread consumerThread = new Thread(consumer);
			consumerThreads.add(consumerThread);
			executor.execute(consumerThread);
		}
	}

	public void addProducer(int delay, Item item) {
		Producer producer = new Producer(delay, buffer, item);
		Thread producerThread = new Thread(producer);
		producerInstances.add(producer);
		producerThreads.add(producerThread);
		executor.execute(producerThread);
	}

	// Remove the last added Producer thread
	public void removeProducer() {
		if (!producerThreads.isEmpty()) {
			Thread toRemove = producerThreads.remove(producerThreads.size() - 1);
			Producer producerInstance = producerInstances.remove(producerInstances.size() - 1);
			producerInstance.shutdown();
			toRemove.interrupt();
		}
	}

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
