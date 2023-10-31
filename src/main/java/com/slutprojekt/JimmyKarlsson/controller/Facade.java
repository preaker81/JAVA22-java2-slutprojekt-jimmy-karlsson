package com.slutprojekt.JimmyKarlsson.controller;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import com.slutprojekt.JimmyKarlsson.model.Item;
import com.slutprojekt.JimmyKarlsson.model.LoadBalancer;
import com.slutprojekt.JimmyKarlsson.utils.HelperMethods;
import com.slutprojekt.JimmyKarlsson.utils.LoggerSingleton;
import com.slutprojekt.JimmyKarlsson.view.SwingGUI;

public class Facade implements PropertyChangeListener {

	private final LoadBalancer loadBalancer; // Model
	private final LoggerSingleton loggerSingleton; // Logger utility
	private final SwingGUI swingGUI; // View

	// Constructor
	public Facade(int bufferCapacity) {
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
		loggerSingleton.addObserver(swingGUI);
	}

	// Listener to observe changes in the model (Buffer)
	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		int bufferSize = (int) evt.getNewValue();
		int bufferCapacity = loadBalancer.getBuffer().getCapacity();
		// Update the progress bar in the view
		swingGUI.updateProgressBar(bufferSize, bufferCapacity);
		// Log warnings if needed
		logBufferWarnings(bufferSize, bufferCapacity);
	}

	// Log buffer warnings if conditions are met
	private void logBufferWarnings(int bufferSize, int bufferCapacity) {
		double bufferRatio = (double) bufferSize / bufferCapacity;
		if (bufferRatio <= 0.10) {
			loggerSingleton.logLowBufferWarning();
		} else if (bufferRatio >= 0.90) {
			loggerSingleton.logHighBufferWarning();
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
