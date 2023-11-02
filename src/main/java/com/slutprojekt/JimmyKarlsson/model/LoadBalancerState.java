package com.slutprojekt.JimmyKarlsson.model;

import java.io.Serializable;
import java.util.List;

/**
 * Immutable record that encapsulates the state of a LoadBalancer. This includes
 * the current delays for producers and consumers, the capacity of the buffer,
 * and its current size. Implements Serializable so that its state can be
 * serialized for persistence or network transmission.
 */
public record LoadBalancerState(List<Integer> producerDelays, // Delays for each producer in seconds.
		List<Integer> consumerDelays, // Delays for each consumer in seconds.
		int bufferCapacity, // Maximum number of items the buffer can hold.
		int currentBufferSize // Current number of items in the buffer.
) implements Serializable {

	private static final long serialVersionUID = 1L; // UID for serialization, ensuring version compatibility.
}
