package com.slutprojekt.JimmyKarlsson.controller;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import com.slutprojekt.JimmyKarlsson.model.Item;
import com.slutprojekt.JimmyKarlsson.model.LoadBalancer;
import com.slutprojekt.JimmyKarlsson.utils.HelperMethods;
import com.slutprojekt.JimmyKarlsson.utils.LoggerSingleton;
import com.slutprojekt.JimmyKarlsson.view.SwingGUI;

public class Facade implements PropertyChangeListener {

	private final LoadBalancer loadBalancer;
	private LoggerSingleton loggerSingleton;
	private final SwingGUI swingGUI;

	public Facade(int bufferCapacity) {
		loadBalancer = new LoadBalancer(bufferCapacity);
		swingGUI = new SwingGUI(this);
		loadBalancer.getBuffer().addPropertyChangeListener(this);
		loadBalancer.initializeConsumers();
		loggerSingleton = LoggerSingleton.getInstance(loadBalancer);
		loggerSingleton.addObserver(swingGUI);
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		int bufferSize = (int) evt.getNewValue();
		int bufferCapacity = loadBalancer.getBuffer().getCapacity();
		swingGUI.updateProgressBar(bufferSize, bufferCapacity);

		if ((double) bufferSize / bufferCapacity <= 0.10) {
			loggerSingleton.logLowBufferWarning();
		} else if ((double) bufferSize / bufferCapacity >= 0.90) {
			loggerSingleton.logHighBufferWarning();
		}
	}

	public void addProducer() {

		int delay = HelperMethods.getRandomIntBetween(1, 10);
		Item item = new Item();
		loadBalancer.addProducer(delay, item);
		loggerSingleton.logProducerInfo(loadBalancer.getProducerThreads().size(), 1, 0);
		loggerSingleton.logProducerIntervals(loadBalancer.getProducerIntervals());
	}

	public void stopProducer() {
		loadBalancer.removeProducer();
		loggerSingleton.logProducerInfo(loadBalancer.getProducerThreads().size(), 0, 1);
		loggerSingleton.logProducerIntervals(loadBalancer.getProducerIntervals());
	}

	public void showGUI() {
		swingGUI.show();
	}
}
