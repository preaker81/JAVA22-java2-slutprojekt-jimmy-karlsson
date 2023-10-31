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

	private static final Logger logger = LogManager.getLogger(LoggerSingleton.class);
	private static LoggerSingleton instance; // Singleton instance
	private ScheduledExecutorService scheduler; // Scheduler for periodic tasks
	private LoadBalancer loadBalancer; // Reference to the LoadBalancer model
	private List<LogObserver> observers = new ArrayList<>(); // List of observers
	private Queue<Integer> bufferSizeHistory; // History of buffer sizes
	private int sampleCounter; // Count of sample taken

	// Private constructor for Singleton pattern
	private LoggerSingleton(LoadBalancer loadBalancer) {
		this.loadBalancer = loadBalancer;
		this.bufferSizeHistory = new LinkedList<>();
		this.sampleCounter = 0;
		// Initialize scheduler
		this.scheduler = Executors.newScheduledThreadPool(1);
		scheduler.scheduleAtFixedRate(this::sampleBuffer, 0, 1, TimeUnit.SECONDS);
	}

	// Singleton getInstance method
	public static synchronized LoggerSingleton getInstance(LoadBalancer loadBalancer) {
		if (instance == null) {
			instance = new LoggerSingleton(loadBalancer);
		}
		return instance;
	}

	// Method to sample buffer size at a fixed rate
	private void sampleBuffer() {
		int currentBufferSize = loadBalancer.getBuffer().getCurrentSize();
		bufferSizeHistory.add(currentBufferSize);
		if (bufferSizeHistory.size() > 10) {
			bufferSizeHistory.poll();
		}
		sampleCounter++;
		if (sampleCounter >= 10) {
			logAverageBuffer();
			sampleCounter = 0;
		}
	}

	// Update log based on WorkerLogDTO data
	@Override
	public void updateLog(WorkerLogDTO logData) {
		logProducerIntervals(logData.getProducerIntervals());
	}

	// Add observer
	public void addObserver(LogObserver observer) {
		observers.add(observer);
	}

	// Remove observer
	public void removeObserver(LogObserver observer) {
		observers.remove(observer);
	}

	// Notify all observers
	private void notifyObservers(String message) {
		for (LogObserver observer : observers) {
			observer.updateLog(message);
		}
	}

	// Shutdown the scheduler
	public void shutdown() {
		scheduler.shutdown();
	}

	// Log average buffer size
	private synchronized void logAverageBuffer() {
		double avgBuffer = bufferSizeHistory.stream().mapToInt(Integer::intValue).average().orElse(0.0);
		String logMessage = String.format("Avg Buffer: %.2f%%", avgBuffer);
		logger.info(logMessage);
		notifyObservers(logMessage);
	}

	// Logging methods (Producer Info, Intervals, Warnings)

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

	@Override
	public void updateLog(String message) {
		// TODO Auto-generated method stub

	}
}
