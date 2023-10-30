package com.slutprojekt.JimmyKarlsson.controller;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Timer;
import java.util.TimerTask;

import com.slutprojekt.JimmyKarlsson.model.Item;
import com.slutprojekt.JimmyKarlsson.model.LoadBalancer;
import com.slutprojekt.JimmyKarlsson.model.WorkerLogDTO;
import com.slutprojekt.JimmyKarlsson.utils.HelperMethods;
import com.slutprojekt.JimmyKarlsson.utils.LoggerSingleton;
import com.slutprojekt.JimmyKarlsson.view.SwingGUI;

public class Facade implements PropertyChangeListener {

	private final LoadBalancer loadBalancer;
	private LoggerSingleton loggerSingleton;
	private final SwingGUI swingGUI;
	private Timer timer;

	public Facade(int bufferCapacity) {
		loadBalancer = new LoadBalancer(bufferCapacity);
		swingGUI = new SwingGUI(this);
		loadBalancer.getBuffer().addPropertyChangeListener(this);
		loadBalancer.initializeConsumers();
		loggerSingleton = LoggerSingleton.getInstance();

		timer = new Timer();
		timer.schedule(new TimerTask() {
			@Override
			public void run() {
				WorkerLogDTO logData = collectLogData();
				loggerSingleton.logStatistics(logData.getProducedItems(), logData.getConsumedItems(),
						logData.getAverageBufferStatus());

				swingGUI.getTextArea().append(logData.toString() + "\n");
			}
		}, 0, 10000);
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
	}

	public void stopProducer() {
		loadBalancer.removeProducer();
		loggerSingleton.logProducerInfo(loadBalancer.getProducerThreads().size(), 0, 1);
	}

	public WorkerLogDTO collectLogData() {
		WorkerLogDTO logData = new WorkerLogDTO();
		logData.setProducerIntervals(loadBalancer.getProducerIntervals());

		// TODO: Replace with dynamic value
		logData.setProducedItems(50);
		logData.setConsumedItems(40);
		logData.setAverageBufferStatus(60);

		// Log producer intervals
		loggerSingleton.logProducerIntervals(logData.getProducerIntervals());

		return logData;
	}

	public void showGUI() {
		swingGUI.show();
	}
}
