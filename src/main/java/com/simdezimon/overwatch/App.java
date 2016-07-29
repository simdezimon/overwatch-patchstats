package com.simdezimon.overwatch;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.sound.midi.ControllerEventListener;

import org.apache.commons.collections4.map.LazyMap;
import org.apache.commons.io.FileUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.simdezimon.overwatch.watchergg.Watchergg;

/**
 * Hello world!
 *
 */
public class App 
{
	
	private static void removeLow(OverwatchStats stats) throws IOException{
		Set<PlayerId> set = stats.getLists().get("random");
    	Set<PlayerId> del = new HashSet<>();
    	
    	for(PlayerId playerId: set){
    		Entry entry = stats.getIndex().getPlayer(playerId).before(Instant.now());
    		double played = entry.getData().getHeroes().get("ALL HEROES").get("Games Played");
    		if(played <= 100){
    			del.add(playerId);
    		}
    	}
    	set.removeAll(del);
    	stats.saveIndex();
	}
	
	private static void removeUnsetted(OverwatchStats stats) throws IOException{
		Set<PlayerId> set = new HashSet<>();
		for(Set<PlayerId> set2 : stats.getLists().getLists().values()){
			set.addAll(set2);
		}
		Set<PlayerId> del = new HashSet<>();
    	for(Player player : stats.getIndex().getPlayers().values()){
    		if(!set.contains(player.getId())){
    			del.add(player.getId());
    		}
    	}
    	for(PlayerId id : del){
    		stats.getIndex().getPlayers().remove(id.toString());
    	}
    	stats.saveIndex();
	}
	
    public static void main( String[] args ) throws IOException
    {
    	OverwatchStats stats = new OverwatchStats();

    	stats.updateSet("test", 10,"top","games_played");
    	
//    	stats.updateSet("topactive", 10000,"top","games_played");
//    	stats.updateSetContent("topactive", 10000);
    	
    	Stats statistic = new Stats();

    	for(PlayerId playerId : stats.getLists().get("test")){
    		statistic.process(stats.getIndex().getPlayer(playerId));
    	}
    	statistic.saveSummary();
    	
    }
    
  
}
