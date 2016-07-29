package com.simdezimon.overwatch;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.charset.Charset;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.apache.commons.collections4.map.LazyMap;
import org.apache.commons.io.FileUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import com.simdezimon.overwatch.watchergg.Watchergg;

public class OverwatchStats {
	private PlayerIndex index;
	private PlayerIdLists lists;
	private Gson gson;

	public OverwatchStats() throws IOException {

		gson = new GsonBuilder().registerTypeAdapter(Instant.class, new JsonSerializer<Instant>() {
			@Override
			public JsonElement serialize(Instant src, Type typeOfSrc, JsonSerializationContext context) {
				return new JsonPrimitive(src.toString());
			}
		}).registerTypeAdapter(Instant.class, new JsonDeserializer<Instant>() {

			@Override
			public Instant deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
					throws JsonParseException {
				return Instant.parse(json.getAsString());
			}
		}).registerTypeAdapter(HeroData.class, new JsonDeserializer<HeroData>() {

			@Override
			public HeroData deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
					throws JsonParseException {
				JsonObject object = (JsonObject) json;
				if (object.has("stats")) {
					return context.deserialize(object.get("stats"), HeroData.class);
				}
				HeroData data = new HeroData();
				for (java.util.Map.Entry<String, JsonElement> entry : object.entrySet()) {
					data.add(entry.getKey(), entry.getValue().getAsDouble());
				}
				return data;
			}
		}).registerTypeAdapter(HeroData.class, new JsonSerializer<HeroData>() {
			@Override
			public JsonElement serialize(HeroData src, Type typeOfSrc, JsonSerializationContext context) {
				return context.serialize(src.getStats());
			}
		})

		.create();

		if (new File("stats/index.json").exists()) {
			String listsString = FileUtils.readFileToString(new File("stats/lists.json"), Charset.defaultCharset());
			lists = gson.fromJson(listsString, PlayerIdLists.class);
			readIndex();
		} else {
			index = new PlayerIndex();
		}

	}

	public void readIndex() throws IOException {
		index = new PlayerIndex();
		JsonReader reader = new JsonReader(new FileReader(new File("stats/index.json")));
		reader.beginArray();
		while (reader.hasNext()) {
			Player player = gson.fromJson(reader, Player.class);
			index.getPlayers().put(player.getId().toString(), player);
		}
		reader.endArray();
		reader.close();
	}

	public void writeIndex() throws IOException {
		JsonWriter writer = new JsonWriter(new FileWriter(new File("stats/index.json")));
		writer.beginArray();
		for (Player player : index.getPlayers().values()) {
			gson.toJson(player, Player.class, writer);
		}
		writer.endArray();
		writer.close();
	}

	public void updateSetContent(String name, int n) throws IOException {
		Set<PlayerId> ids = lists.get(name);
		int i = 0;

		ExecutorService executor = Executors.newFixedThreadPool(10);

		for (PlayerId id : ids) {
			i++;
			if (i > n) {
				break;
			}
			if (!index.getPlayer(id).hasRecent()) {
				executor.submit(() -> {
					try {
						fetchPlayer(id);
					} catch (IOException e) {
						e.printStackTrace();
					}
				});
			}
		}
		executor.shutdown();
		
		try {
			executor.awaitTermination(1, TimeUnit.DAYS);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		saveIndex();
	}

	public void updateSet(String name, int n, String type, String stat) throws IOException {

		final Set<PlayerId> ids;
		Set<PlayerId> newIds;
		if (type.equals("random")) {
			if (lists.get(name) == null) {
				ids = new HashSet<>();
			} else {
				ids = lists.get(name);
			}
			newIds = new Watchergg().sample((int) ((n - ids.size()) * 1.1), 100, 5, stat);
		} else {
			ids = new HashSet<>();
			newIds = new Watchergg().top((int) (n * 1.1), stat);
		}

		ExecutorService executor = Executors.newFixedThreadPool(10);

		for (PlayerId id : newIds) {
			if (!index.getPlayer(id).hasRecent()) {
				executor.submit(() -> {
					try {
						fetchPlayer(id);
						ids.add(id);
					} catch (IOException e) {
						e.printStackTrace();
					}
				});
			} else {
				System.out.println("memory: " + id);
				ids.add(id);
			}
			if (ids.size() >= n) {
				break;
			}
		}
		executor.shutdown();
		try {
			executor.awaitTermination(1, TimeUnit.DAYS);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		lists.put(name, ids);
		saveIndex();
	}

	public void fetchPlayer(PlayerId id) throws IOException {
		System.out.println("fetch: " + id);
		Document doc;
		try {
			
		doc = Jsoup.connect(
				"https://playoverwatch.com/en-gb/career/" + id.getPlatform() + "/" + id.getRegion() + "/" + id.getTag())
				.timeout(10000).get();

		}catch(IOException e){
			doc = Jsoup.connect(
					"https://playoverwatch.com/en-gb/career/" + id.getPlatform() + "/" + id.getRegion() + "/" + id.getTag())
					.timeout(10000).get();
		}
		Entry entry = new Entry(getPlayerData(doc));
		index.getPlayer(id).addEntry(entry);
	}

	private PlayerData getPlayerData(Document doc) throws IOException {
		PlayerData playerData = new PlayerData();
		Element rankElement = doc.getElementsByClass("competitive-rank").first();
		playerData.setRank(Integer.parseInt(rankElement.text()));

		Element competitive = doc.getElementById("competitive-play");

		Element careerStatsSection = competitive.getElementsByClass("career-stats-section").first();
		Map<String, String> names = new HashMap<>();
		for (Element option : careerStatsSection.getElementsByClass("js-career-select").first().children()) {
			names.put(option.attr("value"), option.text());
		}

		Elements stats = careerStatsSection.getElementsByClass("js-stats");
		for (Element element : stats) {
			HeroData heroData = new HeroData();
			for (Element stat : element.select("tbody>tr")) {
				heroData.addStat(stat.child(0).text(), stat.child(1).text());

			}
			playerData.addHero(names.get(element.attr("data-category-id")), heroData);

		}
		return playerData;
	}

	public void saveIndex() throws IOException {
		FileUtils.writeStringToFile(new File("stats/lists.json"), gson.toJson(lists), Charset.defaultCharset());
		writeIndex();
	}

	public PlayerIndex getIndex() {
		return index;
	}

	public PlayerIdLists getLists() {
		return lists;
	}

}
