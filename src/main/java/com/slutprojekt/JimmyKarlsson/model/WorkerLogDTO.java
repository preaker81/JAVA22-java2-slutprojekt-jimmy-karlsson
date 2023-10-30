package com.slutprojekt.JimmyKarlsson.model;

public class WorkerLogDTO {
	private int numberOfProducers;
	private int numberOfConsumers;
	private long producerInterval;
	private long consumerInterval;
	private int workerDifference;
	private double lowResourceWarning;
	private double highResourceWarning;
	private double averageResources;

	@Override
	public String toString() {
		return "WorkerLogDTO{" + "numberOfProducers=" + numberOfProducers + ", numberOfConsumers=" + numberOfConsumers
				+ ", producerInterval=" + producerInterval + ", consumerInterval=" + consumerInterval
				+ ", workerDifference=" + workerDifference + ", lowResourceWarning=" + lowResourceWarning
				+ ", highResourceWarning=" + highResourceWarning + ", averageResources=" + averageResources + '}';
	}

	public int getNumberOfProducers() {
		return numberOfProducers;
	}

	public void setNumberOfProducers(int numberOfProducers) {
		this.numberOfProducers = numberOfProducers;
	}

	public int getNumberOfConsumers() {
		return numberOfConsumers;
	}

	public void setNumberOfConsumers(int numberOfConsumers) {
		this.numberOfConsumers = numberOfConsumers;
	}

	public long getProducerInterval() {
		return producerInterval;
	}

	public void setProducerInterval(long producerInterval) {
		this.producerInterval = producerInterval;
	}

	public long getConsumerInterval() {
		return consumerInterval;
	}

	public void setConsumerInterval(long consumerInterval) {
		this.consumerInterval = consumerInterval;
	}

	public int getWorkerDifference() {
		return workerDifference;
	}

	public void setWorkerDifference(int workerDifference) {
		this.workerDifference = workerDifference;
	}

	public double getLowResourceWarning() {
		return lowResourceWarning;
	}

	public void setLowResourceWarning(double lowResourceWarning) {
		this.lowResourceWarning = lowResourceWarning;
	}

	public double getHighResourceWarning() {
		return highResourceWarning;
	}

	public void setHighResourceWarning(double highResourceWarning) {
		this.highResourceWarning = highResourceWarning;
	}

	public double getAverageResources() {
		return averageResources;
	}

	public void setAverageResources(double averageResources) {
		this.averageResources = averageResources;
	}

}