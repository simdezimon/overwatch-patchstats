package com.simdezimon.overwatch;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections4.map.LazyMap;

public class PlayerIdLists {
	private Map<String,Set<PlayerId>> lists = new HashMap<>();
	
	
	
	public Set<PlayerId> get(String key) {
		return lists.get(key);
	}



	public Set<PlayerId> put(String key, Set<PlayerId> value) {
		return lists.put(key, value);
	}



	public Map<String, Set<PlayerId>> getLists() {
		return lists;
	}
	
	public void setLists(Map<String, Set<PlayerId>> sets) {
		this.lists = sets;
	}
	
}
