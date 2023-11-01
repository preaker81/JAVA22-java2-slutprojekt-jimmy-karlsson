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

	private final LoadBalancer loadBalancer; // Model
	private final LoggerSingleton loggerSingleton; // Logger utility
	private final SwingGUI swingGUI; // View
	private final PropertyChangeSupport support;
	private static final String LOG_PROPERTY = "log";

	// Constructor
	public Facade(int bufferCapacity) {
		support = new PropertyChangeSupport(this);
		// Initialize the model (LoadBalancer)
		this.loadBalancer = new LoadBalancer(bufferCapacity);
		// Initialize the view (SwingGUI)
		this.swingGUI = new SwingGUI(this);
		// Attach property change listener to the model
		loadBalancer.getBuffer().addPropertyChangeListener(this);
		// Initialize consumers in the model
		loadBalancer.initializeConsumers();
		// Initialize logger and add observer
		this.loggerSingleton = LoggerSingleton.getInstance(loadBalancer);
		// Attach SwingGUI as a property change listener to LoggerSingleton
		this.loggerSingleton.addPropertyChangeListener(swingGUI); // Add this line
		support.addPropertyChangeListener(swingGUI);
	}

	// Listener to observe changes in the model (Buffer)
	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		int bufferSize = (int) evt.getNewValue();
		int bufferCapacity = loadBalancer.getBuffer().getCapacity();
		swingGUI.updateProgressBar(bufferSize, bufferCapacity);

		// Use method to encapsulate logging logic
		possiblyLogBufferWarnings(bufferSize, bufferCapacity);
	}

	private void possiblyLogBufferWarnings(int bufferSize, int bufferCapacity) {
		double bufferRatio = (double) bufferSize / bufferCapacity;
		if (bufferRatio <= 0.10 || bufferRatio >= 0.90) {
			String warningMessage = bufferRatio <= 0.10 ? "Low buffer warning!" : "High buffer warning!";
			support.firePropertyChange(LOG_PROPERTY, null, warningMessage); // Using a constant for the property name
		}
	}

	// Add a producer to the LoadBalancer model
	public void addProducer() {
		int delay = HelperMethods.getRandomIntBetween(1, 10);
		Item item = new Item();
		loadBalancer.addProducer(delay, item);
		logProducerChanges(1, 0);
	}

	// Remove a producer from the LoadBalancer model
	public void stopProducer() {
		loadBalancer.removeProducer();
		logProducerChanges(0, 1);
	}

	// Log producer addition or removal
	private void logProducerChanges(int added, int removed) {
		int producerCount = loadBalancer.getProducerThreads().size();
		loggerSingleton.logProducerInfo(producerCount, added, removed);
		loggerSingleton.logProducerIntervals(loadBalancer.getProducerIntervals());
	}

	// Display the GUI
	public void showGUI() {
		swingGUI.show();
	}
}
