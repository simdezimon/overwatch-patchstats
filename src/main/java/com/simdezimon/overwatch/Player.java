package com.simdezimon.overwatch;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Player {
	public static final List<Instant> patches = new ArrayList<Instant>(){{
		add(Instant.parse("2016-07-19T00:00:00Z"));
		add(Instant.parse("2016-07-26T00:00:00Z"));
		add(Instant.now());
	}};
	
	private PlayerId id;
	private List<Entry> entries;

	public Player() {
	}
	
	public Player(PlayerId id) {
		super();
		this.id = id;
		this.entries = new ArrayList<>();
	}

	public PlayerId getId() {
		return id;
	}

	public List<Entry> getEntries() {
		return entries;
	}
	
	public synchronized void addEntry(Entry newEntry){
		entries.add(newEntry);
		List<Entry> reduced = new ArrayList<>();
		for(Instant patch : patches){
			Entry nearest = null;
			for(Entry entry: entries){
				if(nearest == null || Duration.between(entry.getTimestamp(), patch).abs().compareTo(Duration.between(nearest.getTimestamp(), patch).abs())<0){
					nearest = entry;
				}
			}
			if(!reduced.contains(nearest)){
				reduced.add(nearest);
			}
		}
		if(entries.size()- reduced.size() > 0) {
			System.out.println("Removed "+(entries.size() - reduced.size())+" of "+entries.size()+" entries." );
			entries = reduced;
		}
	}

	public Entry after(Instant when){
		Entry ret = null;
		for(Entry entry : entries){
			if(entry.getTimestamp().isAfter(when) && (ret == null || entry.getTimestamp().isBefore(ret.getTimestamp()))){
				ret = entry;
			}
		}
		return ret;
	}
	
	public Entry before(Instant when){
		Entry ret = null;
		for(Entry entry : entries){
			if(entry.getTimestamp().isBefore(when) && (ret == null || entry.getTimestamp().isAfter(ret.getTimestamp()))){
				ret = entry;
			}
		}
		return ret;
	}
	
	public boolean hasRecent() {
		for(Entry entry : entries){
			if(entry.getTimestamp().isAfter(Instant.now().minus(Duration.ofHours(6)))){
				return true;
			}
		}
		return false;
	}
	
	
}
