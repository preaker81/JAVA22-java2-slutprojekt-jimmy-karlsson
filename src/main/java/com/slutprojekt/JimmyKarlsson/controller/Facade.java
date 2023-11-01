package com.slutprojekt.JimmyKarlsson.controller;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import com.slutprojekt.JimmyKarlsson.model.Item;
import com.slutprojekt.JimmyKarlsson.model.LoadBalancer;
import com.slutprojekt.JimmyKarlsson.model.LoadBalancerState;
import com.slutprojekt.JimmyKarlsson.utils.HelperMethods;
import com.slutprojekt.JimmyKarlsson.utils.LoggerSingleton;
import com.slutprojekt.JimmyKarlsson.view.SwingGUI;

public class Facade implements PropertyChangeListener {

	private LoadBalancer loadBalancer;
	private final LoggerSingleton loggerSingleton;
	private final SwingGUI swingGUI;
	private final PropertyChangeSupport support;
	private final String LOG_PROPERTY = "log";

	public Facade(int bufferCapacity) {
		this.loadBalancer = new LoadBalancer(bufferCapacity);
		this.swingGUI = new SwingGUI(this);
		this.loggerSingleton = LoggerSingleton.getInstance(loadBalancer);
		this.loggerSingleton.addPropertyChangeListener(swingGUI);
		loadBalancer.getBuffer().addPropertyChangeListener(this);
		loadBalancer.initializeConsumers();
		support = new PropertyChangeSupport(this);
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

	public void saveStateToFile(String filePath) {
		LoadBalancerState state = loadBalancer.extractState();
		try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(filePath))) {
			oos.writeObject(state);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void loadStateFromFile(String filePath) {
		try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(filePath))) {
			LoadBalancerState savedState = (LoadBalancerState) ois.readObject();
			loadBalancer.applyState(savedState);
		} catch (IOException | ClassNotFoundException e) {
			e.printStackTrace();
		}
	}

}
