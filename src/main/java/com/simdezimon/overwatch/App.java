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

    	stats.updateSet("topactive", 10000, "top","games_played");
    	
    	Stats statistic = new Stats();

    	for(PlayerId playerId : stats.getLists().get("topactive")){
    		statistic.process(stats.getIndex().getPlayer(playerId));
    	}
    	statistic.saveSummary();
    	
//    	removeUnsetted(stats);
    
//    	stats.saveIndex();
//    	stats.updateSetContent("topactive", 10000);
//    	stats.updateSetContent("top100", 100);
//    	stats.updateSetContent("top", 1000);
    	
    	
    	
//    	
//		LazyMap<String, Statistic> statistics = LazyMap.lazyMap(new LinkedHashMap<String,Statistic>(), ()->new Statistic());
//		
//    	for(PlayerId playerId : stats.getLists().get("topactive")){
//    		Entry entry = stats.getIndex().getPlayer(playerId).before(Instant.now());
//    		statistics.get("Player").addValue(1, entry.getData().getRank());
//    		if(entry!= null){
//    			for(java.util.Map.Entry<String, HeroData> heroEntry : entry.getData().getHeroes().entrySet()){
//    				HeroData hero = heroEntry.getValue();
//    				statistics.get(heroEntry.getKey()).addPercentage(hero.get("Games Played"), hero.get("Critical Hit Accuracy"));
//    			}
//    		}
//    	}
//    	
//    	for(PlayerId playerId : stats.getLists().get("topactive")){
//    		Entry second = stats.getIndex().getPlayer(playerId).before(Instant.now());
//    		Entry first = stats.getIndex().getPlayer(playerId).after(Instant.now().minus(Duration.ofDays(10)));
//    		if(first!= null && second != null && first != second){
//    			for(java.util.Map.Entry<String, HeroData> heroEntry : first.getData().getHeroes().entrySet()){
//    				HeroData firstHero = heroEntry.getValue();
//    				HeroData secondHero = second.getData().getHeroes().get(heroEntry.getKey());
//    				statistics.get(heroEntry.getKey()+" patch").addPercentage(firstHero,secondHero,"Critical Hit Accuracy");
//    			}
//    		}
//    	}
//    	
//    	double patchGames = statistics.get("ALL HEROES patch").rounds;
//    	double games = statistics.get("ALL HEROES").rounds;
//    	
//    	for(Map.Entry<String, Statistic>  entry : statistics.entrySet()){
//    		double playRate = entry.getValue().rounds / (entry.getKey().contains("patch") ? patchGames:games) * 100.;
//    		System.out.printf("%20s: %10.3f %8.0f %5.1f %2.0f\n",entry.getKey(),entry.getValue().getAverage(),entry.getValue().rounds,playRate,playRate*6.);
//    	}
    	
    }
    
  
}
