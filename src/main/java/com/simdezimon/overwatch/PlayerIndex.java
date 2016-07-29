package com.simdezimon.overwatch;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class PlayerIndex {
	
	private Map<String,Player> players = new HashMap<>();
	
	public PlayerIndex() {
	}
	
	public Map<String,Player> getPlayers() {
		return players;
	}

	public Player getPlayer(PlayerId id) {
		if(!players.containsKey(id.toString())){
			players.put(id.toString(), new Player(id));
		}

		return players.get(id.toString());
	}
	
}
