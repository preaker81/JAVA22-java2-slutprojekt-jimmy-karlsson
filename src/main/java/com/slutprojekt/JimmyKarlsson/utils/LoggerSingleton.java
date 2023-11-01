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

public class LoggerSingleton {

	private static final Logger logger = LogManager.getLogger(LoggerSingleton.class);
	private static LoggerSingleton instance;
	private ScheduledExecutorService scheduler;
	private LoadBalancer loadBalancer;
	private ConcurrentLinkedQueue<Integer> bufferSizeHistory;
	private int sampleCounter;
	private final PropertyChangeSupport logSupport;

	private LoggerSingleton(LoadBalancer loadBalancer) {
		this.loadBalancer = loadBalancer;
		this.bufferSizeHistory = new ConcurrentLinkedQueue<>();
		this.sampleCounter = 0;
		this.scheduler = Executors.newScheduledThreadPool(1);
		scheduler.scheduleAtFixedRate(this::sampleBuffer, 0, 1, TimeUnit.SECONDS);
		this.logSupport = new PropertyChangeSupport(this);
	}

	public static synchronized LoggerSingleton getInstance(LoadBalancer loadBalancer) {
		if (instance == null) {
			instance = new LoggerSingleton(loadBalancer);
		}
		return instance;
	}

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

	public void shutdown() {
		scheduler.shutdown();
	}

	private synchronized void logAverageBuffer() {
		double avgBuffer = bufferSizeHistory.stream().mapToInt(Integer::intValue).average().orElse(0.0);
		String logMessage = String.format("Avg Buffer: %.2f%%", avgBuffer);
		logger.info(logMessage);
		fireLogChanged(logMessage);
	}

	public void logProducerInfo(int count, int added, int removed) {
		String logMessage = "Total producers: " + count + ", Added: " + added + ", Removed: " + removed;
		logger.info(logMessage);
		fireLogChanged(logMessage);
	}

	public void logProducerIntervals() {
		String logMessage = loadBalancer.getProducerIntervals().toString();
		logger.info(logMessage);
		fireLogChanged(logMessage);
	}

	private void fireLogChanged(String newLogMessage) {
		logSupport.firePropertyChange("log", null, newLogMessage);
	}

	public void addPropertyChangeListener(PropertyChangeListener listener) {
		logSupport.addPropertyChangeListener(listener);
	}

}
