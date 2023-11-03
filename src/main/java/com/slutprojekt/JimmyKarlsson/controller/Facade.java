package com.slutprojekt.JimmyKarlsson.controller;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import javax.swing.SwingUtilities;

import com.slutprojekt.JimmyKarlsson.model.Item;
import com.slutprojekt.JimmyKarlsson.model.LoadBalancer;
import com.slutprojekt.JimmyKarlsson.model.LoadBalancerState;
import com.slutprojekt.JimmyKarlsson.utils.Utilities;
import com.slutprojekt.JimmyKarlsson.utils.LoggerSingleton;
import com.slutprojekt.JimmyKarlsson.view.SwingGUI;

/**
 * The Facade class acts as an intermediary between the model and view
 * components, managing updates to the GUI, and handling user interactions by
 * updating the system's state.
 */
public class Facade implements PropertyChangeListener {

	private LoadBalancer loadBalancer;
	private final LoggerSingleton loggerSingleton;
	private final SwingGUI swingGUI;
	private final PropertyChangeSupport support;
	private final String LOG_PROPERTY = "log";

	/**
	 * Constructor for Facade.
	 * 
	 * @param bufferCapacity The initial capacity of the LoadBalancer's buffer.
	 */
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
		runOnEDT(() -> updateGUIBasedOnPropertyChange(evt));
	}

	// Updates the GUI when a property change event occurs.
	private void updateGUIBasedOnPropertyChange(PropertyChangeEvent evt) {
		int bufferSize = (int) evt.getNewValue();
		int bufferCapacity = loadBalancer.getBuffer().getCapacity();
		swingGUI.updateProgressBar(bufferSize, bufferCapacity);
		possiblyLogBufferWarnings(bufferSize, bufferCapacity);
	}

	// Logs warnings if the buffer size is below 10% or above 90% of its capacity.
	private void possiblyLogBufferWarnings(int bufferSize, int bufferCapacity) {
		double bufferRatio = (double) bufferSize / bufferCapacity;
		if (bufferRatio <= 0.10 || bufferRatio >= 0.90) {
			String warningMessage = bufferRatio <= 0.10 ? "Low buffer warning!" : "High buffer warning!";
			support.firePropertyChange(LOG_PROPERTY, null, warningMessage);
		}
	}

	// Adds a new producer to the load balancer with a random delay.
	public void addProducer() {
		runOnEDT(() -> {
			int delay = Utilities.getRandomIntBetween(1, 10);
			Item item = new Item();
			loadBalancer.addProducer(delay, item);
			logProducerChanges(1, 0);
		});
	}

	// Stops a producer from the load balancer.
	public void stopProducer() {
		runOnEDT(() -> {
			loadBalancer.removeProducer();
			logProducerChanges(0, 1);
		});
	}

	// Logs changes in the number of producers to the logger.
	private void logProducerChanges(int added, int removed) {
		int producerCount = loadBalancer.getProducerCount();
		loggerSingleton.logProducerInfo(producerCount, added, removed);
		loggerSingleton.logProducerIntervals();
	}

	// Displays the GUI.
	public void showGUI() {
		runOnEDT(() -> swingGUI.show());
	}

	// Saves the current state of the load balancer to a file.
	public void saveStateToFile(String filePath) {
		LoadBalancerState state = loadBalancer.extractState();
		writeObjectToFile(filePath, state, "Failed to save state to file");
	}

	// Helper method for writing objects to files.
	private void writeObjectToFile(String filePath, Serializable object, String errorMessage) {
		try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(filePath))) {
			oos.writeObject(object);
		} catch (IOException e) {
			e.printStackTrace();
			System.err.println(errorMessage);
		}
	}

	// Loads the state of the load balancer from a file.
	public void loadStateFromFile(String filePath) {
		LoadBalancerState state = readObjectFromFile(filePath, "Failed to load state from file");
		if (state != null) {
			loadBalancer.applyState(state);
			runOnEDT(this::updateGUIAfterStateLoad);
		}
	}

	// Helper method for reading objects from files.
	private LoadBalancerState readObjectFromFile(String filePath, String errorMessage) {
		try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(filePath))) {
			return (LoadBalancerState) ois.readObject();
		} catch (IOException | ClassNotFoundException e) {
			e.printStackTrace();
			System.err.println(errorMessage);
			return null;
		}
	}

	// Updates the GUI after the state of the load balancer has been loaded from a
	// file.
	private void updateGUIAfterStateLoad() {
		int actualProducerCount = loadBalancer.getProducerCount();
		swingGUI.setNumberOfProducers(actualProducerCount);
		int bufferSize = loadBalancer.getBuffer().getCurrentSize();
		int bufferCapacity = loadBalancer.getBuffer().getCapacity();
		swingGUI.updateProgressBar(bufferSize, bufferCapacity);
		support.firePropertyChange("bufferSize", -1, bufferSize);
	}

	// Executes a task on the Event Dispatch Thread (EDT) of Swing.
	private void runOnEDT(Runnable task) {
		SwingUtilities.invokeLater(task);
	}

	// Getter for accessing the load balancer outside of this class.
	public LoadBalancer getLoadBalancer() {
		return loadBalancer;
	}

}
