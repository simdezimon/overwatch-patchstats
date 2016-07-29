package com.simdezimon.overwatch;

import java.text.NumberFormat;
import java.text.ParseException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.collections4.map.LazyMap;

import com.google.gson.JsonObject;

public class HeroData {
	private static final NumberFormat format = NumberFormat.getInstance(Locale.ENGLISH);
	private Map<String,Double> stats = new HashMap<>();
	private static final LazyMap<String,String> lut = LazyMap.lazyMap(new HashMap<String,String>(),( s)->s);
	
	
	public HeroData() {
	}

	public  Map<String,Double> getStats() {
		return stats;
	}
	
	public void add(String key, double value) {
		
		stats.put(lut.get(key), value);
	}

	public Double get(String key) {
		return stats.getOrDefault(key, 0.);
	}

	public void addStat(String key, String value) {
		if (value.equals("--")) {
			add(key, 0);
		} else if (value.contains("%")) {
			try {
				Number number = format.parse(value.replace("%", ""));
				add(key, number.doubleValue() / 100);
			} catch (ParseException e) {
				System.err.println("Cannot parse " + value + " for key " + key + ".");
			}
		} else if (value.contains(":")) {
			double time = 0;
			for (String timeVal : value.split(":")) {
				time = time * 60;
				time += Double.parseDouble(timeVal);
			}
			add(key, time);
		} else {
			double mult = 1;
			if (value.contains("seconds")) {
				mult = 1;
			} else if (value.contains("hours")) {
				mult = 3600;
			} else if (value.contains("minutes")) {
				mult = 60;
			}
			NumberFormat format = NumberFormat.getInstance(Locale.ENGLISH);
			try {
				Number number = format.parse(value);
				add(key, number.doubleValue() * mult);
			} catch (ParseException e) {
				System.err.println("Cannot parse " + value + " for key " + key+".");
			}

		}

	}
}
