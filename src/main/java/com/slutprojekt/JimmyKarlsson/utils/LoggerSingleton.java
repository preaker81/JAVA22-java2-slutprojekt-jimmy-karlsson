package com.slutprojekt.JimmyKarlsson.utils;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.slutprojekt.JimmyKarlsson.model.LoadBalancer;
import com.slutprojekt.JimmyKarlsson.model.WorkerLogDTO;
import com.slutprojekt.JimmyKarlsson.utils.interfaces.LogObserver;

public class LoggerSingleton implements LogObserver {
	private static LoggerSingleton instance;
	private static final Logger logger = LogManager.getLogger(LoggerSingleton.class);
	private ScheduledExecutorService scheduler;
	private LoadBalancer loadBalancer;
	private Queue<Integer> bufferSizeHistory;
	private int sampleCounter;
	private List<LogObserver> observers = new ArrayList<>();

	private LoggerSingleton(LoadBalancer loadBalancer) {
		this.loadBalancer = loadBalancer;
		bufferSizeHistory = new LinkedList<>(); // <-- Initialize the history
		sampleCounter = 0; // <-- Initialize the sample counter
		scheduler = Executors.newScheduledThreadPool(1);
		scheduler.scheduleAtFixedRate(() -> {
			// Use LoadBalancer instance to get the latest buffer size
			int currentBufferSize = loadBalancer.getBuffer().getCurrentSize(); // <-- Use getCurrentSize()

			bufferSizeHistory.add(currentBufferSize); // <-- Add to history

			// Remove the oldest if we have more than 10 samples
			if (bufferSizeHistory.size() > 10) {
				bufferSizeHistory.poll();
			}

			// Increment the sample counter
			sampleCounter++;

			// If 10 samples have been collected, log the average and reset the counter
			if (sampleCounter >= 10) {
				double avgBuffer = bufferSizeHistory.stream().mapToInt(Integer::intValue).average().orElse(0.0);
				logAverageBuffer(avgBuffer);
				sampleCounter = 0; // Reset the counter
			}

		}, 0, 1, TimeUnit.SECONDS); // Sampling every 1 second
	}

	public static synchronized LoggerSingleton getInstance(LoadBalancer loadBalancer) {
		if (instance == null) {
			instance = new LoggerSingleton(loadBalancer);
		}
		return instance;
	}

	@Override
	public void updateLog(WorkerLogDTO logData) {
		logStatistics(logData.getProducedItems(), logData.getConsumedItems(), logData.getAverageBufferStatus());
		logProducerIntervals(logData.getProducerIntervals());
	}

	public void addObserver(LogObserver observer) {
		observers.add(observer);
	}

	public void removeObserver(LogObserver observer) {
		observers.remove(observer);
	}

	private void notifyObservers(String message) {
		for (LogObserver observer : observers) {
			observer.updateLog(message);
		}
	}

	public void shutdown() {
		scheduler.shutdown();
	}

	// Loggar genomsnittlig buffert var 10:e sekund
	public synchronized void logAverageBuffer(double avgBuffer) {
		String logMessage = String.format("Avg Buffer: %.2f%%", avgBuffer);
		logger.info(logMessage);
		notifyObservers(logMessage);
	}

	// Log Producer Info
	public synchronized void logProducerInfo(int amount, int added, int removed) {
		String logMessage = String.format("Producer amount: %d, added: %d, removed: %d", amount, added, removed);
		logger.info(logMessage);
		notifyObservers(logMessage);
	}

	// Log Producer Intervals
	public synchronized void logProducerIntervals(java.util.List<Integer> intervals) {
		String logMessage = String.format("Producer intervals: %s", intervals.toString());
		logger.info(logMessage);
		notifyObservers(logMessage);
	}

	// Log Low Buffer Warning
	public synchronized void logLowBufferWarning() {
		String logMessage = "Buffer is 10% or lower.";
		logger.warn(logMessage);
		notifyObservers(logMessage);
	}

	// Log High Buffer Warning
	public synchronized void logHighBufferWarning() {
		String logMessage = "Buffer is 90% or higher.";
		logger.warn(logMessage);
		notifyObservers(logMessage);
	}

	// Log Statistics
	public synchronized void logStatistics(int produced, int consumed, double avgBuffer) {
		String logMessage = String.format("Produced: %d, Consumed: %d, Avg Buffer: %.2f", produced, consumed,
				avgBuffer);
		logger.info(logMessage);
		notifyObservers(logMessage);
	}

	@Override
	public void updateLog(String message) {
		// TODO Auto-generated method stub

	}
}
