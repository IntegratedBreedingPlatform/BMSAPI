package org.generationcp.bms.domain;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.generationcp.middleware.domain.h2h.Observation;



public class GermplasmScoreCard implements Comparable<GermplasmScoreCard> {
	
	private final Integer gid;
	private final String name;
	private List<Observation> observations = new ArrayList<Observation>();
	private Map<Observation, Integer> weightings;
	//private int score = 0;
	
	public GermplasmScoreCard(Integer gid, String name) {
		this.gid = gid;
		this.name = name;
	}
	
	public GermplasmScoreCard(Integer gid, String name, List<Observation> observations) {
		this.gid = gid;
		this.name = name;
		this.observations = observations;
	}
	
	public int getScore() {
		int total = 0;
		for (Observation observation : observations) {
			total += Integer.parseInt(observation.getValue());
		}
		return total;
	}

	public Integer getGid() {
		return gid;
	}

	public String getName() {
		return name;
	}
	
	public void addObservation(Observation o) {
		observations.add(o);
	}

	@Override
	public int compareTo(GermplasmScoreCard other) {
		return other.getScore() - this.getScore();
	}

	@Override
	public String toString() {
		return "GermplasmScoreCard [gid=" + gid + ", name=" + name + ", score=" + getScore() + "]";
	}

	
	

}
