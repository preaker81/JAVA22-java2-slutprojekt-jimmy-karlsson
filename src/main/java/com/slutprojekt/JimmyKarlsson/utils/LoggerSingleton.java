package com.slutprojekt.JimmyKarlsson.utils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import com.slutprojekt.JimmyKarlsson.model.WorkerLogDTO;
import com.slutprojekt.JimmyKarlsson.utils.interfaces.LogObserver;
import com.slutprojekt.JimmyKarlsson.model.LoadBalancer; // <-- Added for LoadBalancer
import java.util.concurrent.ScheduledExecutorService;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class LoggerSingleton implements LogObserver {
	private static LoggerSingleton instance;
	private static final Logger logger = LogManager.getLogger(LoggerSingleton.class);
	private ScheduledExecutorService scheduler;
	private LoadBalancer loadBalancer; // <-- Add this line
	private Queue<Integer> bufferSizeHistory; // <-- Add this line to keep track of history
	private int sampleCounter; // <-- Add this line to count samples

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

	public void shutdown() {
		scheduler.shutdown();
	}

	// Loggar genomsnittlig buffert var 10:e sekund
	public synchronized void logAverageBuffer(double avgBuffer) {
		logger.info(String.format("Avg Buffer: %.2f%%", avgBuffer));
	}

	// Loggar information om producer antal, tillagda och borttagna element
	public synchronized void logProducerInfo(int amount, int added, int removed) {
		logger.info(String.format("Producer amount: %d, added: %d, removed: %d", amount, added, removed));
	}

	// Loggar information om alla Producers fördröjningar
	public synchronized void logProducerIntervals(java.util.List<Integer> intervals) {
		logger.info(String.format("Producer intervals: %s", intervals.toString()));
	}

	// Loggar en varning om buffern är 10% eller lägre
	public synchronized void logLowBufferWarning() {
		logger.warn("Buffer is 10% or lower.");
	}

	// Loggar en varning om buffern är 90% eller högre
	public synchronized void logHighBufferWarning() {
		logger.warn("Buffer is 90% or higher.");
	}

	// Loggar statistik var 10:e sekund
	public synchronized void logStatistics(int produced, int consumed, double avgBuffer) {
		logger.info(String.format("Produced: %d, Consumed: %d, Avg Buffer: %.2f", produced, consumed, avgBuffer));
	}
}
