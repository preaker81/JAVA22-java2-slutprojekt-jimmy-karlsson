package com.slutprojekt.JimmyKarlsson.model;

import java.io.Serializable;
import java.util.List;

public record LoadBalancerState(List<Integer> producerDelays, List<Integer> consumerDelays, int bufferCapacity,
		int currentBufferSize) implements Serializable {

	private static final long serialVersionUID = 1L;
}