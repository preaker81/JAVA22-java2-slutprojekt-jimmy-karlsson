package com.slutprojekt.JimmyKarlsson.view;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.Deque;
import java.util.concurrent.ConcurrentLinkedDeque;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import com.slutprojekt.JimmyKarlsson.controller.Facade;

/**
 * The SwingGUI class provides a graphical user interface for interacting with
 * the production regulator system. It displays logs, manages producer count,
 * and provides load and save functionality.
 */
public class SwingGUI implements PropertyChangeListener {
	// Member variables for the components and data used in the GUI
	private JFrame frame;
	private JProgressBar progressBar;
	private JTextArea textArea;
	private JButton plusButton, minusButton, loadButton, saveButton;
	private JLabel numberLabel;
	private Facade facade;
	private int numberOfProducers;
	private Deque<String> logStack = new ConcurrentLinkedDeque<>();
	private static final int MAX_LOG_COUNT = 100;

	/**
	 * Constructor for SwingGUI.
	 * 
	 * @param facade The facade pattern instance to handle business logic.
	 */
	public SwingGUI(final Facade facade) {
		this.facade = facade;
		this.numberOfProducers = 0;
		// Registers this GUI as a listener to property changes from the load balancer.
		this.facade.getLoadBalancer().addPropertyChangeListener(this);
		// Initialize the main frame and its components.
		initFrame();
		initComponents();
		layoutComponents();
	}

	/**
	 * Initializes the main frame of the GUI.
	 */
	private void initFrame() {
		frame = new JFrame("Production regulator");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setSize(600, 500);
		frame.setLocationRelativeTo(null); // Center the window
	}

	/**
	 * Initializes all components that will be used in the GUI.
	 */
	private void initComponents() {
		// Component initializers are broken down into individual methods for
		// readability.
		initButtons();
		initLabels();
		initProgressBar();
		initTextArea();
	}

	/**
	 * Initializes buttons and their respective action listeners.
	 */
	private void initButtons() {
		// Initializes the plus button with an action to add a producer.
		plusButton = new JButton("+");
		plusButton.setBackground(Color.GREEN);
		plusButton.setPreferredSize(new Dimension(80, 40));
		plusButton.addActionListener(new AbstractAction() {
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					facade.addProducer();
					incrementNumberOfProducers();
				} catch (Exception ex) {
					appendToLog("Failed to add producer: " + ex.getMessage());
				}
			}
		});

		// Initializes the minus button with an action to stop a producer.
		minusButton = new JButton("-");
		minusButton.setBackground(Color.RED);
		minusButton.setPreferredSize(new Dimension(80, 40));
		minusButton.addActionListener(new AbstractAction() {
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent e) {
				facade.stopProducer();
				decrementNumberOfProducers();
			}
		});

		// Setup for the file chooser used in load/save actions.
		FileNameExtensionFilter filter = new FileNameExtensionFilter("DAT files", "dat");
		File desktop = new File(System.getProperty("user.home"), "Desktop");

		// Initializes the load button to allow loading state from a file.
		loadButton = new JButton("Load");
		loadButton.addActionListener(new AbstractAction() {
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent e) {
				JFileChooser fileChooser = new JFileChooser();
				fileChooser.setCurrentDirectory(desktop);
				fileChooser.setFileFilter(filter);
				int returnValue = fileChooser.showOpenDialog(null);
				if (returnValue == JFileChooser.APPROVE_OPTION) {
					String filePath = fileChooser.getSelectedFile().getAbsolutePath();
					facade.loadStateFromFile(filePath);
				}
			}
		});

		// Initializes the save button to save the current state to a file.
		saveButton = new JButton("Save");
		saveButton.addActionListener(new AbstractAction() {
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent e) {
				JFileChooser fileChooser = new JFileChooser();
				fileChooser.setCurrentDirectory(desktop);
				fileChooser.setFileFilter(filter);
				int returnValue = fileChooser.showSaveDialog(null);
				if (returnValue == JFileChooser.APPROVE_OPTION) {
					String filePath = fileChooser.getSelectedFile().getAbsolutePath();
					if (!filePath.toLowerCase().endsWith(".dat")) {
						filePath += ".dat";
					}
					facade.saveStateToFile(filePath);
				}
			}
		});
	}

	/**
	 * Initializes the JLabel used for displaying the number of producers. This
	 * label will be updated every time the number of producers changes.
	 */
	private void initLabels() {
		numberLabel = new JLabel("0"); // Initialize with zero, representing the starting number of producers.
		numberLabel.setPreferredSize(new Dimension(50, 40)); // Setting the preferred size for uniformity.
		numberLabel.setHorizontalAlignment(SwingConstants.CENTER); // Align text to the center of the label.
	}

	/**
	 * Initializes the JProgressBar which shows the current load of the production
	 * process. The progress bar is updated whenever there's a change in the
	 * production buffer size.
	 */
	private void initProgressBar() {
		progressBar = new JProgressBar();
		Dimension dim = new Dimension(400, 20); // Define the dimensions of the progress bar.
		progressBar.setPreferredSize(dim); // Apply the dimensions to ensure consistent size.
		progressBar.setMinimumSize(dim);
		progressBar.setMaximumSize(dim);
		progressBar.setStringPainted(true); // Display the percentage of progress as a string on the bar.
	}

	/**
	 * Initializes the JTextArea which serves as a log display where messages and
	 * system events will be shown.
	 */
	private void initTextArea() {
		textArea = new JTextArea(); // Create a new text area that will hold the logs.
	}

	/**
	 * Initializes and lays out the GUI components.
	 */
	private void layoutComponents() {
		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout());

		// Set the preferred height for the top and bottom panels
		int panel1Height = 100;
		int panel3Height = 50;

		// Panel 1: Contains buttons and progress bar
		JPanel panel1 = new JPanel(new BorderLayout());
		JPanel buttonPanel = new JPanel();
		// Add buttons and label to the buttonPanel
		buttonPanel.add(plusButton);
		buttonPanel.add(numberLabel);
		buttonPanel.add(minusButton);
		// Add the buttonPanel to the top (NORTH) of panel1 and the progressBar to the
		// bottom (SOUTH)
		panel1.add(buttonPanel, BorderLayout.NORTH);
		panel1.add(progressBar, BorderLayout.SOUTH);
		// Set the preferred size of panel1 based on frame width and predefined height
		panel1.setPreferredSize(new Dimension(frame.getWidth(), panel1Height));

		// Panel 2: Contains a scrollable text area
		JPanel panel2 = new JPanel(new BorderLayout());
		JScrollPane scroll = new JScrollPane(textArea, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
				JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		// Add the scroll pane to the center of panel2
		panel2.add(scroll, BorderLayout.CENTER);

		// Panel 3: Contains load and save buttons
		JPanel panel3 = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		// Add buttons to panel3
		panel3.add(loadButton);
		panel3.add(saveButton);
		// Set the preferred size of panel3 based on frame width and predefined height
		panel3.setPreferredSize(new Dimension(frame.getWidth(), panel3Height));

		// Add all the panels to the main panel
		panel.add(panel1, BorderLayout.NORTH);
		panel.add(panel2, BorderLayout.CENTER);
		panel.add(panel3, BorderLayout.SOUTH);

		// Add the main panel to the frame
		frame.add(panel);
	}

	/**
	 * Sets the number of producers and updates the label.
	 *
	 * @param numberOfProducers the new number of producers
	 */
	public void setNumberOfProducers(int numberOfProducers) {
		this.numberOfProducers = numberOfProducers;
		numberLabel.setText(Integer.toString(numberOfProducers));
	}

	/**
	 * Increments the number of producers by one and updates the label.
	 */
	private void incrementNumberOfProducers() {
		numberOfProducers++;
		numberLabel.setText(Integer.toString(numberOfProducers));
	}

	/**
	 * Decrements the number of producers by one, if greater than zero, and updates
	 * the label.
	 */
	private void decrementNumberOfProducers() {
		if (numberOfProducers > 0) {
			numberOfProducers--;
			numberLabel.setText(Integer.toString(numberOfProducers));
		}
	}

	/**
	 * Updates the progress bar with the current value and maximum.
	 *
	 * @param value   the current value of the progress
	 * @param maximum the maximum value of the progress
	 */
	public void updateProgressBar(int value, int maximum) {
		progressBar.setMaximum(maximum);
		progressBar.setValue(value);
		// Additionally, update the color of the progress bar based on the current value
		updateProgressBarColor();
	}

	/**
	 * Appends a new log message to the text area.
	 *
	 * @param message the message to be appended to the log
	 */
	public void appendToLog(String message) {
		int caretPosition = textArea.getCaretPosition();

		// Ensure the log does not exceed MAX_LOG_COUNT messages
		if (logStack.size() >= MAX_LOG_COUNT) {
			logStack.pollLast();
		}

		logStack.offerFirst(message);

		// Rebuild the text to display from the log stack
		StringBuilder logs = new StringBuilder();
		for (String log : logStack) {
			logs.append(log).append("\n");
		}

		// Update the text area with the new log messages
		textArea.setText(logs.toString());

		// Try to maintain the caret position to avoid jumping in the text area
		textArea.setCaretPosition(caretPosition);
	}

	/**
	 * Responds to property changes and updates the GUI accordingly.
	 *
	 * @param evt the property change event
	 */
	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		String propertyName = evt.getPropertyName();

		// Handle log updates
		if ("log".equals(propertyName)) {
			String newLogMessage = (String) evt.getNewValue();
			appendToLog(newLogMessage);
		}
		// Handle updates to the buffer size
		else if ("bufferSize".equals(propertyName)) {
			int newBufferSize = (Integer) evt.getNewValue();
			// Assume facade and getLoadBalancer() are implemented elsewhere in the
			// application
			int bufferCapacity = facade.getLoadBalancer().getBuffer().getCapacity();
			updateProgressBar(newBufferSize, bufferCapacity);
		}

		// Handle updates to producer count
		if ("producerCount".equals(propertyName)) {
			int newCount = (Integer) evt.getNewValue();
			System.out.println("PropertyChange fired for producerCount: " + newCount);
			setNumberOfProducers(newCount);
		}
	}

	/**
	 * Updates the progress bar's color based on the current progress value.
	 */
	public void updateProgressBarColor() {
		int value = progressBar.getValue();
		int max = progressBar.getMaximum();
		float percent = (float) value / max * 100;

		// Define custom colors
		Color dustyRed = new Color(153, 0, 0);
		Color dustyGreen = new Color(0, 153, 0);
		Color dustyYellow = new Color(153, 153, 0);

		// Change the color of the progress bar based on the percentage
		if (percent <= 10 || percent >= 90) {
			progressBar.setForeground(dustyRed);
		} else if (percent >= 45 && percent <= 55) {
			progressBar.setForeground(dustyGreen);
		} else {
			progressBar.setForeground(dustyYellow);
		}
	}

	/**
	 * Makes the application window visible.
	 */
	public void show() {
		SwingUtilities.invokeLater(() -> frame.setVisible(true));
	}
}
