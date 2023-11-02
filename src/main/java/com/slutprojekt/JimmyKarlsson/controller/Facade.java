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
 * Facade is the controller class in a MVC pattern that manages interactions
 * between the model (LoadBalancer) and the view (SwingGUI). It handles property
 * change events, facilitates GUI updates, serialization of model state, and
 * logging.
 */
public class Facade implements PropertyChangeListener {

	private LoadBalancer loadBalancer;
	private final LoggerSingleton loggerSingleton;
	private final SwingGUI swingGUI;
	private final PropertyChangeSupport support;
	private final String LOG_PROPERTY = "log";

	public Facade(int bufferCapacity) {
		// Initializes a LoadBalancer with the given buffer capacity.
		this.loadBalancer = new LoadBalancer(bufferCapacity);
		// Initializes the GUI and associates it with this Facade.
		this.swingGUI = new SwingGUI(this);
		// Retrieves the instance of LoggerSingleton associated with the LoadBalancer.
		this.loggerSingleton = LoggerSingleton.getInstance(loadBalancer);
		// Adds the SwingGUI as a property change listener to the LoggerSingleton for
		// logging updates.
		this.loggerSingleton.addPropertyChangeListener(swingGUI);
		// Registers itself as a listener to the property changes in LoadBalancer's
		// buffer.
		loadBalancer.getBuffer().addPropertyChangeListener(this);
		// Initializes the consumers in LoadBalancer.
		loadBalancer.initializeConsumers();
		// Sets up property change support to manage listeners.
		support = new PropertyChangeSupport(this);
		// Adds SwingGUI as a property change listener to this facade for updates.
		support.addPropertyChangeListener(swingGUI);
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		// Ensures that updates to the GUI based on property changes are performed on
		// the Event Dispatch Thread (EDT).
		runOnEDT(() -> updateGUIBasedOnPropertyChange(evt));
	}

	private void updateGUIBasedOnPropertyChange(PropertyChangeEvent evt) {
		// Extracts the new buffer size from the property change event.
		int bufferSize = (int) evt.getNewValue();
		// Retrieves the total buffer capacity from the LoadBalancer.
		int bufferCapacity = loadBalancer.getBuffer().getCapacity();
		// Updates the progress bar in the GUI with the new buffer size and capacity.
		swingGUI.updateProgressBar(bufferSize, bufferCapacity);
		// Checks if logging for buffer warnings is necessary and performs it if so.
		possiblyLogBufferWarnings(bufferSize, bufferCapacity);
	}

	private void possiblyLogBufferWarnings(int bufferSize, int bufferCapacity) {
		// Calculates the ratio of the buffer size to its capacity.
		double bufferRatio = (double) bufferSize / bufferCapacity;
		// If the buffer ratio is at or below 10% or at or above 90%, log a warning.
		if (bufferRatio <= 0.10 || bufferRatio >= 0.90) {
			String warningMessage = bufferRatio <= 0.10 ? "Low buffer warning!" : "High buffer warning!";
			// Fires a property change event for logging the warning message.
			support.firePropertyChange(LOG_PROPERTY, null, warningMessage);
		}
	}

	/**
	 * Adds a new producer to the load balancer and logs the changes.
	 */
	public void addProducer() {
		// Queue the following actions to be executed on the Event Dispatch Thread
		// (EDT).
		runOnEDT(() -> {
			// Generates a random delay between 1 and 10.
			int delay = Utilities.getRandomIntBetween(1, 10);
			// Creates a new item to be produced.
			Item item = new Item();
			// Adds a new producer to the load balancer with the generated delay and item.
			loadBalancer.addProducer(delay, item);
			// Logs the addition of a new producer.
			logProducerChanges(1, 0);
		});
	}

	/**
	 * Stops a producer in the load balancer and logs the changes.
	 */
	public void stopProducer() {
		// Queue the following actions to be executed on the Event Dispatch Thread
		// (EDT).
		runOnEDT(() -> {
			// Removes a producer from the load balancer.
			loadBalancer.removeProducer();
			// Logs the removal of a producer.
			logProducerChanges(0, 1);
		});
	}

	/**
	 * Logs information related to the change in the number of producers.
	 * 
	 * @param added   the number of producers added
	 * @param removed the number of producers removed
	 */
	private void logProducerChanges(int added, int removed) {
		// Retrieves the current count of producers from the load balancer.
		int producerCount = loadBalancer.getProducerCount();
		// Logs the current producer count, along with the number added or removed.
		loggerSingleton.logProducerInfo(producerCount, added, removed);
		// Logs the intervals of all producers.
		loggerSingleton.logProducerIntervals();
	}

	/**
	 * Displays the GUI for user interaction.
	 */
	public void showGUI() {
		// Executes the GUI's show method on the Event Dispatch Thread (EDT).
		runOnEDT(() -> swingGUI.show());
	}

	/**
	 * Saves the current state of the load balancer to a file.
	 * 
	 * @param filePath the path to the file where the state is to be saved
	 */
	public void saveStateToFile(String filePath) {
		// Extracts the current state from the load balancer.
		LoadBalancerState state = loadBalancer.extractState();
		// Writes the extracted state object to the specified file path.
		writeObjectToFile(filePath, state, "Failed to save state to file");
	}

	/**
	 * Writes a serializable object to a file, handling any I/O exceptions.
	 * 
	 * @param filePath     the path to the file where the object is to be saved
	 * @param object       the serializable object to be saved
	 * @param errorMessage the error message to display if an exception occurs
	 */
	private void writeObjectToFile(String filePath, Serializable object, String errorMessage) {
		try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(filePath))) {
			// Writes the object to the file at the given file path.
			oos.writeObject(object);
		} catch (IOException e) {
			// Prints the stack trace and error message to standard error if an I/O
			// exception occurs.
			e.printStackTrace();
			System.err.println(errorMessage);
		}
	}

	/**
	 * Loads the state of the load balancer from a file.
	 * 
	 * @param filePath the path to the file from which the state is to be loaded
	 */
	public void loadStateFromFile(String filePath) {
		// Reads the LoadBalancerState object from the specified file path.
		LoadBalancerState state = readObjectFromFile(filePath, "Failed to load state from file");
		if (state != null) {
			// If the state was successfully read, applies it to the load balancer.
			loadBalancer.applyState(state);
			// Updates the GUI after the new state has been loaded.
			runOnEDT(this::updateGUIAfterStateLoad);
		}
	}

	/**
	 * Reads a serializable object from a file, handling any I/O exceptions.
	 * 
	 * @param filePath     the path to the file from which the object is to be read
	 * @param errorMessage the error message to display if an exception occurs
	 * @return the read object, or null if an exception occurred
	 */
	private LoadBalancerState readObjectFromFile(String filePath, String errorMessage) {
		try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(filePath))) {
			// Reads and returns the LoadBalancerState object from the file.
			return (LoadBalancerState) ois.readObject();
		} catch (IOException | ClassNotFoundException e) {
			// Prints the stack trace and error message to standard error if an I/O or
			// ClassNotFound exception occurs.
			e.printStackTrace();
			System.err.println(errorMessage);
			return null;
		}
	}

	/**
	 * Updates the GUI to reflect the current state after it has been loaded from a
	 * file.
	 */
	private void updateGUIAfterStateLoad() {
		// Gets the actual number of producers from the load balancer after the state
		// load.
		int actualProducerCount = loadBalancer.getProducerCount();
		// Updates the number of producers displayed on the GUI.
		swingGUI.setNumberOfProducers(actualProducerCount);
		// Retrieves the current buffer size and capacity from the load balancer.
		int bufferSize = loadBalancer.getBuffer().getCurrentSize();
		int bufferCapacity = loadBalancer.getBuffer().getCapacity();
		// Updates the progress bar on the GUI to reflect the current buffer state.
		swingGUI.updateProgressBar(bufferSize, bufferCapacity);
		// Fires a property change event to notify listeners of the buffer size update.
		support.firePropertyChange("bufferSize", -1, bufferSize);
	}

	/**
	 * Runs a task on the Event Dispatch Thread (EDT) of Swing. This ensures that
	 * all updates to the GUI are done on the EDT to prevent concurrency issues.
	 * 
	 * @param task the task to be executed on the EDT
	 */
	private void runOnEDT(Runnable task) {
		// Invokes the task asynchronously on the Swing EDT.
		SwingUtilities.invokeLater(task);
	}

	/**
	 * Gets the current instance of the load balancer.
	 * 
	 * @return the current LoadBalancer instance
	 */
	public LoadBalancer getLoadBalancer() {
		// Returns the instance of LoadBalancer managed by this Facade.
		return loadBalancer;
	}

}
