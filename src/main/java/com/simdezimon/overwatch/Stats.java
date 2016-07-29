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
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.collections4.map.LazyMap;
import org.apache.commons.io.FileUtils;
import org.apache.commons.math3.stat.interval.ConfidenceInterval;
import org.apache.commons.math3.stat.interval.IntervalUtils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.SerializedName;

public class Stats {

	public static class StatisticValue {
		public double d;

		public void add(double value) {
			d += value;

		}
	}

	public static class StatisticKey {
		private final String hero;
		private final int patch;
		private final String stat;

		public StatisticKey(String hero, int patch, String stat) {
			super();
			this.hero = hero;
			this.patch = patch;
			this.stat = stat;
		}

		public String getHero() {
			return hero;
		}

		public int getPatch() {
			return patch;
		}

		public String getStat() {
			return stat;
		}
			
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((hero == null) ? 0 : hero.hashCode());
			result = prime * result + patch;
			result = prime * result + ((stat == null) ? 0 : stat.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (!(obj instanceof StatisticKey))
				return false;
			StatisticKey other = (StatisticKey) obj;
			if (hero == null) {
				if (other.hero != null)
					return false;
			} else if (!hero.equals(other.hero))
				return false;
			if (patch != other.patch)
				return false;
			if (stat == null) {
				if (other.stat != null)
					return false;
			} else if (!stat.equals(other.stat))
				return false;
			return true;
		}
	}

	public class Summary {
		
		@SerializedName("Patches") private Map<Integer,String> patches = new HashMap<>();
		
		@SerializedName("Heroes") private Map<String, Map<String, Map<Integer,Double>>> heroes  = LazyMap.lazyMap(new TreeMap<>(), () -> LazyMap.lazyMap(new TreeMap<>(), () -> new TreeMap<>()));
		@SerializedName("Stats") private Map<String, Map<String, Map<Integer,Double>>> stats= LazyMap.lazyMap(new TreeMap<>(), () -> LazyMap.lazyMap(new TreeMap<>(), () -> new TreeMap<>()));
		@SerializedName("Games Played") private Map<String,	 Map<Integer,Double>> gamesPlayed =  LazyMap.lazyMap(new TreeMap<>(), () -> new TreeMap<>());
		
		@SerializedName("Heroes, Confidence Interval") private Map<String, Map<String, Map<Integer,String>>> heroesExtra  = LazyMap.lazyMap(new TreeMap<>(), () -> LazyMap.lazyMap(new TreeMap<>(), () -> new TreeMap<>()));
		@SerializedName("Stats, Confidence Interval") private Map<String, Map<String, Map<Integer,String>>> statsExtra  = LazyMap.lazyMap(new TreeMap<>(), () -> LazyMap.lazyMap(new TreeMap<>(), () -> new TreeMap<>()));
		@SerializedName("Games Played, Confidence Interval") private Map<String,  Map<Integer,String>> gamesPlayedExtra =  LazyMap.lazyMap(new TreeMap<>(), () -> new TreeMap<>());
		
		
		public Summary() {
			Instant before = Instant.parse("2016-06-28T00:00:00Z");
	    	DateTimeFormatter formatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.LONG).withLocale(Locale.UK).withZone(ZoneId.systemDefault());
	    	int i = 0;
	    	for(Instant patch : Player.patches){
	    		patches.put(i++,formatter.format(before) + " - " + formatter.format(patch));
	    		before = patch;
	    	}
			
		}
		
		public void addGames(String hero,int patch,double d){
			gamesPlayed.get(hero).put(patch,d);
		}
		
		public void calcGames(){
			for(int i = 0;i<Player.patches.size();i++){
				int sum = 0;
				for(java.util.Map.Entry<String, Map<Integer, Double>> entry : gamesPlayed.entrySet()){
					if(!entry.getKey().equals("ALL HEROES")){
						sum += entry.getValue().get(i);
					}
				}
				for(java.util.Map.Entry<String, Map<Integer, Double>> entry : gamesPlayed.entrySet()){
					int value = (int) (double)entry.getValue().get(i);

					if(!entry.getKey().equals("ALL HEROES")){
						ConfidenceInterval interval = IntervalUtils.getClopperPearsonInterval(sum,value, 0.9);
						gamesPlayedExtra.get(entry.getKey()).put(i, String.format("%7d, %5.2f%% (%5.2f%% - %5.2f%%), in %4.1f%% of all teams", value,value/(double)sum * 100.,interval.getLowerBound() * 100.,interval.getUpperBound() * 100.,value/(double)sum * 600.));
					}
					}
			}
		}
		
		public void addStat(String hero,String stat,int patch, double value){
			if(stat.equals("Games Won") || stat.equals("Win Percentage")) {
				double fac =  0.5 / Stats.this.getStat("ALL HEROES", patch, "Games Won");
				int number = (int) Math.round(gamesPlayed.get(hero).get(patch) * fac);
				int success = (int) Math.round(number * value * fac);
				ConfidenceInterval interval = IntervalUtils.getClopperPearsonInterval(number,success, 0.9);
				String str = String.format("%5.2f%% (%5.2f%% - %5.2f%%)", value * fac * 100.,interval.getLowerBound() * 100.,interval.getUpperBound() * 100.);
				heroesExtra.get(hero).get(stat).put(patch,str);
				statsExtra.get(stat).get(hero).put(patch,str);
			} else {
				int number = (int) Math.round(gamesPlayed.get(hero).get(patch))/2;
				ConfidenceInterval interval = IntervalUtils.getNormalApproximationInterval(number*2,number, 0.9);
				String str = String.format("%.2f (%.2f - %.2f)", value , (0.5+interval.getLowerBound() ) * value,(0.5+interval.getUpperBound())* value );
				heroesExtra.get(hero).get(stat).put(patch,str);
				statsExtra.get(stat).get(hero).put(patch,str);
			}
			
			heroes.get(hero).get(stat).put(patch,value);
			stats.get(stat).get(hero).put(patch,value);
		}
	}

	public void saveSummary() throws IOException {
		// patch - hero - stats
//		Map<String, Map<String, HeroStats>> summary = new HashMap<>();
//		for (int i = 0; i < Player.patches.size(); i++) {
//			Map<String, HeroStats> patchMap = new HashMap<>();
//			summary.put(i+"",  patchMap);
//			
//			for (String hero : heroes) {
//				patchMap.put(hero, new HeroStats(getSum(hero, i+"", "Games Played")));
//			}
//		}
		Summary summary = new Summary();

		for (int i = 0; i < Player.patches.size(); i++) {
			for (String hero : heroes) {
				summary.addGames(hero, i, getSum(hero, i, "Games Played"));
			}
		}
		summary.calcGames();
		
		for(StatisticKey k : stats.keySet()){
			if(occurences.get(k) .d > 100){
				summary.addStat(k.hero, k.stat,k.patch, getStat(k.hero, k.patch, k.stat));
			} else {
				System.out.println(k.hero+" "+k.stat+" "+occurences.get(k).d);
			}
		}
		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		String str = gson.toJson(summary);
		FileUtils.writeStringToFile(new File("stats/summary.json"),str, Charset.defaultCharset());
	}

	private static final String[] heroes = new String[] { "ALL HEROES", "Torbjörn", "Widowmaker", "Bastion", "Reaper",
			"Tracer", "Symmetra", "Junkrat", "D.Va", "Hanzo", "Roadhog", "Mei", "Zarya", "Winston", "Ana", "Genji",
			"Lúcio", "Pharah", "Mercy", "Reinhardt", "Soldier: 76", "Zenyatta", "McCree" };

	private Map<StatisticKey, StatisticValue> stats = LazyMap.lazyMap(new LinkedHashMap<>(), () -> new StatisticValue());
	private Map<StatisticKey, StatisticValue> occurences = LazyMap.lazyMap(new LinkedHashMap<>(), () -> new StatisticValue());

	
	private void addStat(String hero, int patch, String stat, double value) {
		stats.get(new StatisticKey(hero, patch, stat)).add(value);
		occurences.get(new StatisticKey(hero, patch, stat)).add(1);
	}

	public double getStat(String hero, int patch, String stat) {
		double d = stats.get(new StatisticKey(hero, patch, stat)).d;
		double g = stats.get(new StatisticKey(hero, patch, "Games Played")).d;
		return d / g;
	}

	public double getSum(String hero, int patch, String stat) {
		return stats.get(new StatisticKey(hero, patch, stat)).d;
	}

	private boolean isProportion(String key) {
		return key.contains("Average") || key.contains("Accuracy") || key.contains("Percentage")|| key.contains("Percentage")|| key.contains("Rank");
	}

	private boolean isUseful(String key) {
		return !key.contains("Most in") && !key.contains("Best") && !key.contains("per Life");
	}
	
	private String normalize(String s) {
		if(s.endsWith("Kill") || s.endsWith("Hit")|| s.endsWith("Elimination")|| s.endsWith("kill")|| s.endsWith("Assist")|| s.endsWith("Death")|| s.endsWith("Card")){
			return s +"s";
		}
		return s;
		
	}

	public void process(Player p) {
		if (p.getEntries().isEmpty()) {
			return;
		}
		int patchcount = Player.patches.size();
		Entry[] matches = new Entry[patchcount];
		for (int i = 0; i < patchcount; i++) {
			Instant patch = Player.patches.get(i);
			Entry nearest = null;
			for (Entry entry : p.getEntries()) {
				if (nearest == null || Duration.between(entry.getTimestamp(), patch).abs()
						.compareTo(Duration.between(nearest.getTimestamp(), patch).abs()) < 0) {
					nearest = entry;
				}
			}
			matches[i] = nearest;
		}

		for (String hero : heroes) {
			Map<String, Double> previous = LazyMap.lazyMap(new HashMap<>(), () -> 0.);
			double prevGames = 0;
			for (int i = 0; i < patchcount; i++) {
				HeroData data = matches[i].getData().getHero(hero);
				double current = data.get("Games Played");
				for (java.util.Map.Entry<String, Double> entry : data.getStats().entrySet()) {
					String key = normalize(entry.getKey());
					double value = entry.getValue();
					if (isUseful(key)) {
						double prev = previous.get(key);
						double statValue;
						if (isProportion(key)) {
							statValue = value * current - prev * prevGames;
						} else {
							statValue = value - prev;
						}
						previous.put(key, value);
						addStat(hero, i , key, statValue);
					}
				}
				if(!hero.equals("ALL HEROES")) {
					addStat("ALL HEROES", i , "Rank", matches[i].getData().getRank() * (current - prevGames));
				}
				prevGames = current;
			}
		}
	}
}
