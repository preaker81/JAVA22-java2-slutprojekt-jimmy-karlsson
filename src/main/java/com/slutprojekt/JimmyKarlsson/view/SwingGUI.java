package com.slutprojekt.JimmyKarlsson.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Deque;
import java.util.concurrent.ConcurrentLinkedDeque;

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

public class SwingGUI implements PropertyChangeListener {
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
	private Deque<String> logStack = new ConcurrentLinkedDeque<>();
	private static final int MAX_LOG_COUNT = 100;

	public SwingGUI(final Facade facade) {
		this.facade = facade;
		this.numberOfProducers = 0;
		initFrame();
		initComponents();
		layoutComponents();
	}

	private void initFrame() {
		frame = new JFrame("Production regulator");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setSize(600, 500);
		frame.setLocationRelativeTo(null);
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
				try {
					facade.addProducer();
					incrementNumberOfProducers();
				} catch (Exception ex) {
					appendToLog("Failed to add producer: " + ex.getMessage());
				}
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
				decrementNumberOfProducers();
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
		progressBar.setPreferredSize(dim);
		progressBar.setMinimumSize(dim);
		progressBar.setMaximumSize(dim);
		progressBar.setStringPainted(true);
	}

	private void initTextArea() {
		textArea = new JTextArea();
	}

	private void layoutComponents() {
		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout());

		int panel1Height = 100;
		int panel3Height = 50;

		JPanel panel1 = new JPanel(new BorderLayout());
		JPanel buttonPanel = new JPanel();
		buttonPanel.add(plusButton);
		buttonPanel.add(numberLabel);
		buttonPanel.add(minusButton);
		panel1.add(buttonPanel, BorderLayout.NORTH);
		panel1.add(progressBar, BorderLayout.SOUTH);
		panel1.setPreferredSize(new Dimension(frame.getWidth(), panel1Height));

		JPanel panel2 = new JPanel(new BorderLayout());
		JScrollPane scroll = new JScrollPane(textArea, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
				JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

		panel2.add(scroll, BorderLayout.CENTER);

		JPanel panel3 = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		panel3.add(loadButton);
		panel3.add(saveButton);
		panel3.setPreferredSize(new Dimension(frame.getWidth(), panel3Height));

		panel.add(panel1, BorderLayout.NORTH);
		panel.add(panel2, BorderLayout.CENTER);
		panel.add(panel3, BorderLayout.SOUTH);

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
		updateProgressBarColor();
	}

	public void appendToLog(String message) {
		int caretPosition = textArea.getCaretPosition();

		if (logStack.size() >= MAX_LOG_COUNT) {
			logStack.pollLast();
		}

		logStack.offerFirst(message);

		StringBuilder logs = new StringBuilder();
		for (String log : logStack) {
			logs.append(log).append("\n");
		}

		textArea.setText(logs.toString());

		textArea.setCaretPosition(caretPosition);
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		String propertyName = evt.getPropertyName();

		if ("log".equals(propertyName)) {
			String newLogMessage = (String) evt.getNewValue();
			appendToLog(newLogMessage);
		} else if ("progress".equals(propertyName)) {
			// You can add progress bar update logic here if needed
		}
	}

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
