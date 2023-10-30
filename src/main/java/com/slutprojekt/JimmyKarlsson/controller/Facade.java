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
	private final SwingGUI swingGUI;
	private Timer timer;

	public Facade(int bufferCapacity) {
		loadBalancer = new LoadBalancer(bufferCapacity);
		swingGUI = new SwingGUI(this);
		loadBalancer.getBuffer().addPropertyChangeListener(this);
		loadBalancer.initializeConsumers();

		timer = new Timer();
		timer.schedule(new TimerTask() {
			@Override
			public void run() {
				WorkerLogDTO logData = collectLogData(); // Collect the data you need to log
				LoggerSingleton.getInstance().log(logData); // Log data through Singleton Logger

				// Update the text area in the Swing GUI to display log data.
				swingGUI.getTextArea().append(logData.toString() + "\n");
			}
		}, 0, 10000); // Update every 10 seconds

	}

	// This method will be called whenever the buffer size changes
	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		int bufferSize = (int) evt.getNewValue();
		int bufferCapacity = loadBalancer.getBuffer().getCapacity();
		swingGUI.updateProgressBar(bufferSize, bufferCapacity);
	}

	public void addProducer() {
		// Customize these values as needed
		int delay = HelperMethods.getRandomIntBetween(1, 10);
		Item item = new Item();
		loadBalancer.addProducer(delay, item);
	}

	public void stopProducer() {
		loadBalancer.removeProducer();
	}

	// In Facade.java
	public WorkerLogDTO collectLogData() {
		WorkerLogDTO logData = new WorkerLogDTO();

		logData.setNumberOfProducers(loadBalancer.getProducerThreads().size());
		logData.setNumberOfConsumers(loadBalancer.getConsumerThreads().size());
		logData.setProducerInterval(1000);
		logData.setConsumerInterval(2000);
		logData.setWorkerDifference(5);
		logData.setLowResourceWarning(0.1);
		logData.setHighResourceWarning(0.9);
		logData.setAverageResources(0.5);
		return logData;
	}

	public void showGUI() {
		swingGUI.show();
	}
}
