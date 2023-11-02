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

/**
 * The LoggerSingleton class is designed to keep track of buffer size statistics
 * of a LoadBalancer and log this information periodically. It is implemented as
 * a singleton to ensure only one instance manages the logging throughout the
 * application. It also uses a scheduler to perform logging actions at fixed
 * intervals.
 */
public class LoggerSingleton {

	private static final Logger logger = LogManager.getLogger(LoggerSingleton.class); // Logger instance to log
																						// messages.
	private static final int MAX_HISTORY_SIZE = 10; // Maximum number of historical buffer sizes to keep.
	private static final int SAMPLE_THRESHOLD = 10; // Number of buffer samples after which the average should be
													// logged.

	private static LoggerSingleton instance; // Singleton instance of this class.

	private final ScheduledExecutorService scheduler; // Scheduler to perform tasks at fixed intervals.
	private final LoadBalancer loadBalancer; // Reference to the LoadBalancer whose buffer is being monitored.
	private final ConcurrentLinkedQueue<Integer> bufferSizeHistory; // Thread-safe queue to store buffer size history.
	private final PropertyChangeSupport logSupport; // PropertyChangeSupport to notify listeners of log changes.
	private int sampleCounter; // Counter for the number of samples taken.

	private LoggerSingleton(LoadBalancer loadBalancer) {
		this.loadBalancer = loadBalancer;
		this.bufferSizeHistory = new ConcurrentLinkedQueue<>();
		this.logSupport = new PropertyChangeSupport(this);
		this.scheduler = Executors.newScheduledThreadPool(1); // Creates a single-threaded scheduler.
		scheduleBufferSampling(); // Initialize the periodic buffer sampling task.
	}

	/**
	 * Synchronized method to get the single instance of LoggerSingleton, creating
	 * it if necessary.
	 *
	 * @param loadBalancer The LoadBalancer whose buffer size will be logged.
	 * @return The singleton instance of LoggerSingleton.
	 */
	public static synchronized LoggerSingleton getInstance(LoadBalancer loadBalancer) {
		if (instance == null) {
			instance = new LoggerSingleton(loadBalancer);
		}
		return instance;
	}

	/**
	 * Schedules the buffer sampling task to run at fixed intervals.
	 */
	private void scheduleBufferSampling() {
		// Schedules a task to run every second starting immediately.
		scheduler.scheduleAtFixedRate(this::sampleBuffer, 0, 1, TimeUnit.SECONDS);
	}

	/**
	 * Samples the current buffer size from the LoadBalancer, updates history, and
	 * logs if necessary.
	 */
	private void sampleBuffer() {
		int currentBufferSize = loadBalancer.getBuffer().getCurrentSize(); // Get current buffer size.
		updateBufferSizeHistory(currentBufferSize); // Update the history of buffer sizes.
		if (shouldLogAverage()) {
			logAverageBuffer(); // Log the average buffer size if the sample threshold is reached.
			resetSampleCounter(); // Reset the sample counter.
		}
	}

	/**
	 * Updates the buffer size history queue and increments the sample counter.
	 *
	 * @param bufferSize The new buffer size to add to the history.
	 */
	private void updateBufferSizeHistory(int bufferSize) {
		bufferSizeHistory.add(bufferSize); // Adds the new buffer size to the history.
		if (bufferSizeHistory.size() > MAX_HISTORY_SIZE) {
			bufferSizeHistory.poll(); // Removes the oldest buffer size if history exceeds max size.
		}
		sampleCounter++; // Increment the number of samples taken.
	}

	/**
	 * Checks if the number of samples taken has reached the threshold to log the
	 * average buffer size.
	 *
	 * @return True if the sample counter is greater than or equal to the threshold.
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
	 * Calculates the average buffer size, formats a log message, and logs it.
	 */
	private synchronized void logAverageBuffer() {
		double avgBuffer = calculateAverageBufferSize(); // Calculate the average buffer size.
		double avgBufferPercentage = calculateBufferPercentage(avgBuffer); // Calculate the percentage of the average
																			// buffer size.
		String logMessage = formatLogMessage(avgBufferPercentage); // Format the log message.
		logInformation(logMessage); // Log the formatted message.
	}

	/**
	 * Calculates the average buffer size from the buffer size history.
	 *
	 * @return The average buffer size.
	 */
	private double calculateAverageBufferSize() {
		return bufferSizeHistory.stream().mapToInt(Integer::intValue).average().orElse(0.0);
	}

	/**
	 * Calculates the percentage of the average buffer size relative to its
	 * capacity.
	 *
	 * @param averageBufferSize The average buffer size.
	 * @return The percentage of the average buffer size.
	 */
	private double calculateBufferPercentage(double averageBufferSize) {
		int bufferCapacity = loadBalancer.getBuffer().getCapacity(); // Get the buffer's capacity.
		double bufferPercentage = (averageBufferSize / bufferCapacity) * 100; // Calculate the buffer usage percentage.
		return Math.min(bufferPercentage, 100.0); // Ensure the percentage does not exceed 100.
	}

	/**
	 * Formats the log message for the average buffer size percentage.
	 *
	 * @param avgBufferPercentage The average buffer size percentage to log.
	 * @return The formatted log message.
	 */
	private String formatLogMessage(double avgBufferPercentage) {
		return String.format("Avg Buffer: %.2f%%", avgBufferPercentage);
	}

	/**
	 * Logs the information message and notifies listeners.
	 *
	 * @param message The message to log and notify about.
	 */
	private void logInformation(String message) {
		logger.info(message); // Logs the message with INFO level.
		fireLogChanged(message); // Notify listeners about the new log message.
	}

	/**
	 * Shuts down the scheduler and stops any further logging.
	 */
	public void shutdown() {
		scheduler.shutdown();
	}

	/**
	 * Notifies listeners that the log has changed.
	 *
	 * @param newLogMessage The new log message that has been generated.
	 */
	private void fireLogChanged(String newLogMessage) {
		// Notify all listeners about the log change.
		logSupport.firePropertyChange("log", null, newLogMessage);
	}

	/**
	 * Adds a new PropertyChangeListener to listen for log changes.
	 *
	 * @param listener The listener to add.
	 */
	public void addPropertyChangeListener(PropertyChangeListener listener) {
		logSupport.addPropertyChangeListener(listener);
	}

	/**
	 * Logs the information about producer count, how many have been added, and how
	 * many removed.
	 *
	 * @param count   The total number of producers.
	 * @param added   The number of producers added.
	 * @param removed The number of producers removed.
	 */
	public void logProducerInfo(int count, int added, int removed) {
		String message = String.format("Total producers: %d, Added: %d, Removed: %d", count, added, removed);
		logInformation(message);
	}

	/**
	 * Logs the intervals at which producers are generating data.
	 */
	public void logProducerIntervals() {
		String message = loadBalancer.getProducerIntervals().toString();
		logInformation(message);
	}
}
