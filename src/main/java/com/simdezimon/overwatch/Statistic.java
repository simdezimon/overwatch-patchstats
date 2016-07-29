package com.simdezimon.overwatch;

public class Statistic {
	public double rounds = 0;
	public double sum = 0;
	
	public double getAverage(){
		return sum / rounds;
	}
	
	public void addValue(double rounds, double value){
		this.rounds += rounds;
		sum += value;
	}
	
	public void addPercentage(double rounds, double percentage){
		this.rounds += rounds;
		sum += rounds * percentage;
	}
	
	public void addValue(HeroData hero1,HeroData hero2, String valueName){
		addValue(hero1.get("Games Played"), hero2.get("Games Played"),hero1.get(valueName),hero2.get(valueName));
	}
	public void addPercentage(HeroData hero1,HeroData hero2, String valueName){
		addPercentage(hero1.get("Games Played"), hero2.get("Games Played"),hero1.get(valueName),hero2.get(valueName));
	}
	
	public void addValue(double rounds1,double rounds2, double value1,double value2){
		this.rounds += rounds2 - rounds1;
		sum += value2- value1;
	}
	
	public void addPercentage(double rounds1,double rounds2, double percentage1,double percentage2){
		this.rounds += rounds2 - rounds1;
		sum += percentage2 * rounds2 - percentage1 * rounds1;
	}
}
