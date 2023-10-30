package com.slutprojekt.JimmyKarlsson.utils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class LoggerSingleton {
	private static LoggerSingleton instance;
	private static final Logger logger = LogManager.getLogger(LoggerSingleton.class);

	private LoggerSingleton() {
		// Här kan du initialisera andra resurser om det behövs
	}

	public static synchronized LoggerSingleton getInstance() {
		if (instance == null) {
			instance = new LoggerSingleton();
		}
		return instance;
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
