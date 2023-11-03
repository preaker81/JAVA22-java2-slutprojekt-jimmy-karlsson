package com.slutprojekt.JimmyKarlsson.model;

import java.io.Serializable;
import java.util.List;

/**
 * Immutable record representing the state of a LoadBalancer at a snapshot in
 * time. It is useful for monitoring and managing the flow of items between
 * producers and consumers by providing essential details about their operation.
 * This record is serializable, allowing it to be easily stored or transmitted.
 */
public record LoadBalancerState(List<Integer> producerDelays, // List of delays for producers, representing the time in
																// seconds before a producer can produce the next item.
		List<Integer> consumerDelays, // List of delays for consumers, representing the time in seconds before a
										// consumer can consume the next item.
		int bufferCapacity, // The maximum number of items the buffer can hold at any given time.
		int currentBufferSize // The current count of items present in the buffer.
) implements Serializable {

	// Serial Version UID for serialization. If any change is made to this record,
	// consider altering the UID to maintain the integrity of serialized objects.
	private static final long serialVersionUID = 1L;
}
