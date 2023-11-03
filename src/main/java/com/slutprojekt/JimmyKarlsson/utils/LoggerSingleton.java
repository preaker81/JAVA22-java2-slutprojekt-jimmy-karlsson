package com.slutprojekt.JimmyKarlsson.utils;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.slutprojekt.JimmyKarlsson.model.LoadBalancer;
import com.slutprojekt.JimmyKarlsson.model.interfaces.BufferSizeProvider;

/**
 * A singleton class responsible for logging buffer size samples and statistics
 * in a LoadBalancer environment. It provides mechanisms to track and log
 * changes in the buffer over time.
 */
public class LoggerSingleton {

	private static final Logger logger = LogManager.getLogger(LoggerSingleton.class);
	private static final int MAX_HISTORY_SIZE = 10; // Maximum number of buffer sizes to keep in history.
	private static final int SAMPLE_THRESHOLD = 10; // Number of samples to collect before calculating the average.
	private static LoggerSingleton instance;

	private final ScheduledExecutorService scheduler; // Handles the scheduling of buffer sampling.
	private final BufferSizeProvider bufferSizeProvider; // Provides buffer size and capacity data.
	private final ConcurrentLinkedQueue<Integer> bufferSizeHistory; // Stores the history of buffer sizes.
	private final PropertyChangeSupport logSupport; // Used for observer pattern implementation.
	private int sampleCounter; // Counts the number of samples taken.

	/**
	 * Private constructor for LoggerSingleton.
	 * 
	 * @param bufferSizeProvider A provider for buffer size information, typically a
	 *                           LoadBalancer.
	 */
	private LoggerSingleton(BufferSizeProvider bufferSizeProvider) {
		this.bufferSizeProvider = bufferSizeProvider;
		this.bufferSizeHistory = new ConcurrentLinkedQueue<>();
		this.logSupport = new PropertyChangeSupport(this);
		this.scheduler = Executors.newScheduledThreadPool(1); // Initializes a single-threaded scheduler.
		scheduleBufferSampling(); // Start the periodic buffer size sampling.
	}

	/**
	 * Returns the single instance of LoggerSingleton, creating it if it does not
	 * exist.
	 * 
	 * @param loadBalancer The LoadBalancer that will be used as a
	 *                     BufferSizeProvider.
	 * @return The single instance of LoggerSingleton.
	 */
	public static synchronized LoggerSingleton getInstance(LoadBalancer loadBalancer) {
		if (instance == null) {
			instance = new LoggerSingleton(loadBalancer);
		}
		return instance;
	}

	/**
	 * Schedules the buffer size sampling task to run every second.
	 */
	private void scheduleBufferSampling() {
		// Schedules the task to sample buffer size every second.
		scheduler.scheduleAtFixedRate(this::sampleBuffer, 0, 1, TimeUnit.SECONDS);
	}

	/**
	 * Samples the current buffer size, updating history and logs if necessary.
	 */
	private void sampleBuffer() {
		int currentBufferSize = bufferSizeProvider.getCurrentSize();
		updateBufferSizeHistory(currentBufferSize);
		if (shouldLogAverage()) {
			logAverageBuffer();
			resetSampleCounter();
		}
	}

	/**
	 * Updates the history of buffer sizes and increments the sample counter.
	 * 
	 * @param bufferSize The new buffer size to be added to the history.
	 */
	private void updateBufferSizeHistory(int bufferSize) {
		bufferSizeHistory.add(bufferSize);
		if (bufferSizeHistory.size() > MAX_HISTORY_SIZE) {
			bufferSizeHistory.poll(); // Remove the oldest buffer size.
		}
		sampleCounter++;
	}

	/**
	 * Determines if the average buffer size should be logged based on the sample
	 * threshold.
	 * 
	 * @return True if the sample counter has reached the threshold, false
	 *         otherwise.
	 */
	private boolean shouldLogAverage() {
		return sampleCounter >= SAMPLE_THRESHOLD;
	}

	/**
	 * Resets the sample counter to zero.
	 */
	private void resetSampleCounter() {
		sampleCounter = 0;
	}

	/**
	 * Calculates, formats, and logs the average buffer size as a percentage of
	 * total capacity.
	 */
	private synchronized void logAverageBuffer() {
		double avgBuffer = calculateAverageBufferSize();
		double avgBufferPercentage = calculateBufferPercentage(avgBuffer);
		String logMessage = formatLogMessage(avgBufferPercentage);
		logInformation(logMessage);
	}

	/**
	 * Calculates the average buffer size based on the recorded history.
	 * 
	 * @return The average buffer size.
	 */
	private double calculateAverageBufferSize() {
		return bufferSizeHistory.stream().mapToInt(Integer::intValue).average().orElse(0.0);
	}

	/**
	 * Calculates the buffer usage percentage based on the average buffer size.
	 * 
	 * @param averageBufferSize The average buffer size to calculate the percentage
	 *                          from.
	 * @return The buffer usage percentage.
	 */
	private double calculateBufferPercentage(double averageBufferSize) {
		int bufferCapacity = bufferSizeProvider.getCapacity();
		return Math.min((averageBufferSize / bufferCapacity) * 100, 100.0);
	}

	/**
	 * Formats the log message to include the average buffer usage percentage.
	 * 
	 * @param avgBufferPercentage The average buffer usage percentage.
	 * @return The formatted log message.
	 */
	private String formatLogMessage(double avgBufferPercentage) {
		return String.format("Avg Buffer: %.2f%%", avgBufferPercentage);
	}

	/**
	 * Logs information with INFO level and notifies listeners of a new log message.
	 * 
	 * @param message The message to log and notify about.
	 */
	private void logInformation(String message) {
		logger.info(message);
		fireLogChanged(message);
	}

	/**
	 * Shuts down the scheduler service, stopping any further buffer sampling.
	 */
	public void shutdown() {
		scheduler.shutdown();
	}

	/**
	 * Notifies all listeners about a change in the log.
	 * 
	 * @param newLogMessage The new log message that listeners are notified about.
	 */
	private void fireLogChanged(String newLogMessage) {
		logSupport.firePropertyChange("log", null, newLogMessage);
	}

	/**
	 * Adds a PropertyChangeListener to the list of listeners that are notified of
	 * log changes.
	 * 
	 * @param listener The PropertyChangeListener to add.
	 */
	public void addPropertyChangeListener(PropertyChangeListener listener) {
		logSupport.addPropertyChangeListener(listener);
	}

	/**
	 * Logs information about the count, addition, and removal of producers.
	 * 
	 * @param count   The total count of producers.
	 * @param added   The count of producers added.
	 * @param removed The count of producers removed.
	 */
	public void logProducerInfo(int count, int added, int removed) {
		String message = String.format("Total producers: %d, Added: %d, Removed: %d", count, added, removed);
		logInformation(message);
	}

	/**
	 * Logs information about producer intervals.
	 */
	public void logProducerIntervals() {
		String message = bufferSizeProvider.getProducerIntervals().toString();
		logInformation(message);
	}
}
