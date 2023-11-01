package com.slutprojekt.JimmyKarlsson.controller;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

import com.slutprojekt.JimmyKarlsson.model.Item;
import com.slutprojekt.JimmyKarlsson.model.LoadBalancer;
import com.slutprojekt.JimmyKarlsson.utils.HelperMethods;
import com.slutprojekt.JimmyKarlsson.utils.LoggerSingleton;
import com.slutprojekt.JimmyKarlsson.view.SwingGUI;

public class Facade implements PropertyChangeListener {

	private final LoadBalancer loadBalancer;
	private final LoggerSingleton loggerSingleton;
	private final SwingGUI swingGUI;
	private final PropertyChangeSupport support;
	private static final String LOG_PROPERTY = "log";

	public Facade(int bufferCapacity) {
		support = new PropertyChangeSupport(this);
		this.loadBalancer = new LoadBalancer(bufferCapacity);
		this.swingGUI = new SwingGUI(this);
		loadBalancer.getBuffer().addPropertyChangeListener(this);
		loadBalancer.initializeConsumers();
		this.loggerSingleton = LoggerSingleton.getInstance(loadBalancer);
		this.loggerSingleton.addPropertyChangeListener(swingGUI);
		support.addPropertyChangeListener(swingGUI);
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		int bufferSize = (int) evt.getNewValue();
		int bufferCapacity = loadBalancer.getBuffer().getCapacity();
		swingGUI.updateProgressBar(bufferSize, bufferCapacity);
		possiblyLogBufferWarnings(bufferSize, bufferCapacity);
	}

	private void possiblyLogBufferWarnings(int bufferSize, int bufferCapacity) {
		double bufferRatio = (double) bufferSize / bufferCapacity;
		if (bufferRatio <= 0.10 || bufferRatio >= 0.90) {
			String warningMessage = bufferRatio <= 0.10 ? "Low buffer warning!" : "High buffer warning!";
			support.firePropertyChange(LOG_PROPERTY, null, warningMessage);
		}
	}

	public void addProducer() {
		int delay = HelperMethods.getRandomIntBetween(1, 10);
		Item item = new Item();
		loadBalancer.addProducer(delay, item);
		logProducerChanges(1, 0);
	}

	public void stopProducer() {
		loadBalancer.removeProducer();
		logProducerChanges(0, 1);
	}

	private void logProducerChanges(int added, int removed) {
		int producerCount = loadBalancer.getProducerThreads().size();
		loggerSingleton.logProducerInfo(producerCount, added, removed);
		loggerSingleton.logProducerIntervals();
	}

	public void showGUI() {
		swingGUI.show();
	}
}
