package com.slutprojekt.JimmyKarlsson.model;

import java.util.List;

public class WorkerLogDTO {
	private List<Integer> producerIntervals;
	private int producedItems;
	private int consumedItems;
	private double averageBufferStatus;

	public List<Integer> getProducerIntervals() {
		return producerIntervals;
	}

	public void setProducerIntervals(List<Integer> producerIntervals) {
		this.producerIntervals = producerIntervals;
	}

	public int getProducedItems() {
		return producedItems;
	}

	public void setProducedItems(int producedItems) {
		this.producedItems = producedItems;
	}

	public int getConsumedItems() {
		return consumedItems;
	}

	public void setConsumedItems(int consumedItems) {
		this.consumedItems = consumedItems;
	}

	public double getAverageBufferStatus() {
		return averageBufferStatus;
	}

	public void setAverageBufferStatus(double averageBufferStatus) {
		this.averageBufferStatus = averageBufferStatus;
	}

	@Override
	public String toString() {
		return "WorkerLogDTO{" + "producerIntervals=" + producerIntervals + ", producedItems=" + producedItems
				+ ", consumedItems=" + consumedItems + ", averageBufferStatus=" + averageBufferStatus + '}';
	}
}
