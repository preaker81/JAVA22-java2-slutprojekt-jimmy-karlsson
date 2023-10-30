package com.slutprojekt.JimmyKarlsson.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;

import com.slutprojekt.JimmyKarlsson.controller.Facade;

public class SwingGUI {
	private JFrame frame;
	private JProgressBar progressBar;
	private JTextArea textArea;
	private JButton plusButton;
	private JButton minusButton;
	private JButton loadButton;
	private JButton saveButton;
	private JLabel numberLabel;
	private Facade facade;
	private int numberOfProducers;

	public SwingGUI(final Facade facade) {
		this.facade = facade;
		this.numberOfProducers = 0; // Initialize to zero
		initFrame();
		initComponents();
		layoutComponents();
	}

	private void initFrame() {
		frame = new JFrame("Production regulator");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setSize(600, 500);
	}

	private void initComponents() {
		initButtons();
		initLabels();
		initProgressBar();
		initTextArea();
	}

	private void initButtons() {
		plusButton = new JButton("+");
		plusButton.setBackground(Color.GREEN);
		plusButton.setPreferredSize(new Dimension(80, 40));
		plusButton.addActionListener(new AbstractAction() {
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent e) {
				facade.addProducer();
				incrementNumberOfProducers(); // Update local state and UI
			}
		});

		minusButton = new JButton("-");
		minusButton.setBackground(Color.RED);
		minusButton.setPreferredSize(new Dimension(80, 40));
		minusButton.addActionListener(new AbstractAction() {
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent e) {
				facade.stopProducer();
				decrementNumberOfProducers(); // Update local state and UI
			}
		});

		loadButton = new JButton("Load");
		saveButton = new JButton("Save");
	}

	private void initLabels() {
		numberLabel = new JLabel("0");
		numberLabel.setPreferredSize(new Dimension(50, 40));
		numberLabel.setHorizontalAlignment(SwingConstants.CENTER);
	}

	private void initProgressBar() {
		progressBar = new JProgressBar();
		Dimension dim = new Dimension(400, 20);
		progressBar.setPreferredSize(dim); // Preferred height set to 20
		progressBar.setMinimumSize(dim); // Minimum height set to 20
		progressBar.setMaximumSize(dim); // Maximum height set to 20
		progressBar.setStringPainted(true); // Enables the painting of text
	}

	private void initTextArea() {
		textArea = new JTextArea();
		textArea.setPreferredSize(new Dimension(400, 300)); // Set width and height
	}

	private void layoutComponents() {
		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout());

		// Layout buttons and label
		JPanel buttonPanel = new JPanel();
		buttonPanel.add(plusButton);
		buttonPanel.add(numberLabel);
		buttonPanel.add(minusButton);

		// Layout load and save buttons
		JPanel loadSavePanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		loadSavePanel.add(loadButton);
		loadSavePanel.add(saveButton);

		// Layout text area
		JScrollPane scroll = new JScrollPane(textArea);
		JPanel textAndButtonsPanel = new JPanel(new BorderLayout());
		textAndButtonsPanel.add(scroll, BorderLayout.CENTER);
		textAndButtonsPanel.add(loadSavePanel, BorderLayout.SOUTH);

		// Add all to main panel
		panel.add(buttonPanel, BorderLayout.NORTH);
		panel.add(progressBar, BorderLayout.CENTER);
		panel.add(textAndButtonsPanel, BorderLayout.SOUTH);

		frame.add(panel);
	}

	private void incrementNumberOfProducers() {
		numberOfProducers++;
		numberLabel.setText(Integer.toString(numberOfProducers));
	}

	private void decrementNumberOfProducers() {
		if (numberOfProducers > 0) {
			numberOfProducers--;
			numberLabel.setText(Integer.toString(numberOfProducers));
		}
	}

	public void updateProgressBar(int value, int maximum) {
		progressBar.setMaximum(maximum);
		progressBar.setValue(value);
		updateProgressBarColor(); // Update the color based on the current value
	}

	public void appendToLog(String message) {
		textArea.append(message + "\n");
	}

	public JTextArea getTextArea() {
		return textArea;
	}

	// Method to update progress bar color based on current value
	public void updateProgressBarColor() {
		int value = progressBar.getValue();
		int max = progressBar.getMaximum();
		float percent = (float) value / max * 100;

		Color dustyRed = new Color(153, 0, 0); // Red: 153, Green: 0, Blue: 0
		Color dustyGreen = new Color(0, 153, 0); // Red: 0, Green: 153, Blue: 0
		Color dustyYellow = new Color(153, 153, 0); // Red: 153, Green: 153, Blue: 0

		if (percent <= 10 || percent >= 90) {
			progressBar.setForeground(dustyRed);
		} else if (percent >= 45 && percent <= 55) {
			progressBar.setForeground(dustyGreen);
		} else {
			progressBar.setForeground(dustyYellow);
		}
	}

	public void show() {
		frame.setVisible(true);
	}
}
