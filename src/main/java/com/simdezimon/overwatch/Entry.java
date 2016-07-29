package com.simdezimon.overwatch;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

public class Entry {
	private PlayerData data;
	private Instant timestamp;
	public Entry() {
	}

	public Entry(PlayerData data) {
		this.data = data;
		timestamp = Instant.now().truncatedTo(ChronoUnit.SECONDS);
	}

	public PlayerData getData() {
		return data;
	}

	public Instant getTimestamp() {
		return timestamp;
	}

	
	
}
