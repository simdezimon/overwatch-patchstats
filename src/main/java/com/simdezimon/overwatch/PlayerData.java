package com.simdezimon.overwatch;

import java.util.HashMap;
import java.util.Map;

public class PlayerData {
	private int rank;
	private Map<String,HeroData> heroes = new HashMap<>();
	
	public int getRank() {
		return rank;
	}
	public void setRank(int rank) {
		this.rank = rank;
	}
	public Map<String, HeroData> getHeroes() {
		return heroes;
	}
	
	public void addHero(String name, HeroData hero){
		heroes.put(name, hero);
	}

	public static final HeroData def0 = new HeroData();
	
	public HeroData getHero(String name) {
		return heroes.getOrDefault(name, def0);
	}
	
}
