package com.slutprojekt.JimmyKarlsson.utils;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

import com.slutprojekt.JimmyKarlsson.model.WorkerLogDTO;

public class LoggerSingleton {
	private static LoggerSingleton instance;
	private BufferedWriter writer;

	private LoggerSingleton() {
		try {
			writer = new BufferedWriter(new FileWriter("log.txt", true));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static synchronized LoggerSingleton getInstance() {
		if (instance == null) {
			instance = new LoggerSingleton();
		}
		return instance;
	}

	public synchronized void log(WorkerLogDTO logData) {
		try {
			writer.write(logData.toString());
			writer.newLine();
			writer.flush(); // Flush för att säkerställa att all data är skriven
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
