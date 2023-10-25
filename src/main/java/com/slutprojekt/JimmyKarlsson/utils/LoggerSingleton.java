package com.slutprojekt.JimmyKarlsson.utils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class LoggerSingleton {
	private static LoggerSingleton instance;

	private Logger logger;

	private LoggerSingleton() {
		logger = LogManager.getLogger(LoggerSingleton.class);
	}

	// Public method to get the singleton instance
	public static synchronized LoggerSingleton getInstance() {
		if (instance == null) {
			instance = new LoggerSingleton();
		}
		return instance;
	}

	// Logging methods
	public void info(String message) {
		logger.info(message);
	}

	public void error(String message) {
		logger.error(message);
	}

	// Add more methods to expose other logging levels if needed
}
