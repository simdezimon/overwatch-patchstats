package com.simdezimon.overwatch.watchergg;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.io.IOUtils;
import org.apache.http.ParseException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import com.google.gson.Gson;
import com.simdezimon.overwatch.PlayerId;

public class Watchergg {
	
	public Watchergg() {
	}
	
	public Set<PlayerId> sample(int n, int pages, int perpage,String stat) throws IOException{
		
		final Set<PlayerId> ids = new HashSet<>();
		if(n <= 0){
			return ids;
		}
		Random r = new Random();
		while(true){
			String string = fetch(r.nextInt(pages),stat);
			Gson gson = new Gson();
			LeaderboardResponse response = gson.fromJson(string, LeaderboardResponse.class);
			for(int i = 0; i<perpage;i++){
				LeaderboardPlayer p = response.data.players.get(r.nextInt(response.data.players.size()));
				ids.add(new PlayerId(p.tag(),p.region,p.platform));
				if(ids.size() >= n){
					return ids;
				}
			}
		}
	}
	
	public Set<PlayerId> top(int n,String stat) throws IOException{
		final Set<PlayerId> ids = new HashSet<>();
		
		for (int i = 0; true;i++){
			String string = fetch(i,stat);
			Gson gson = new Gson();
			LeaderboardResponse response = gson.fromJson(string, LeaderboardResponse.class);
			for(LeaderboardPlayer p : response.data.players){
				ids.add(new PlayerId(p.tag(),p.region,p.platform));
				if(ids.size() >= n){
					return ids;
				}
			}
		}
	}
	
	public String fetch(int page,String stat) throws IOException{
		CloseableHttpClient client = HttpClients.createDefault();
		
		HttpGet get = new HttpGet("http://api.watcher.gg/leaderboards/"+stat+"/-1/1/pc?page="+page);
		CloseableHttpResponse response = client.execute(get);
		return EntityUtils.toString(response.getEntity(),"UTF-8");

	}
	
	public class LeaderboardPlayer {
		private String name;
		private String region;
		private String platform;
		
		public String tag(){
			int idx = name.lastIndexOf("#");
			if(idx == -1) {
				return name;
			}
			return name.substring(0,idx)+"-"+name.substring(idx+1);
		}
		
	}
	private class LeaderboardResponse{
		public LeaderboardData data;
	}
	private class LeaderboardData{
		public List<LeaderboardPlayer> players;
	}

}
