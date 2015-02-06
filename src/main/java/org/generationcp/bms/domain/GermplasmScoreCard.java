package org.generationcp.bms.domain;

import org.generationcp.middleware.domain.h2h.Observation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;



public class GermplasmScoreCard implements Comparable<GermplasmScoreCard> {
	
	private final Integer gid;
	private final String name;
	private List<Observation> observations = new ArrayList<Observation>();
	//private Map<Observation, Integer> weightings;
	private List<String> inventoryLocations = Arrays.asList(new String[] {"Shed 5, Bin 2", "Lab 32, Shelf 5, Bag 36", "Storage Room 34, Drawer 25", "Lab 23, Shelf 2, Canister 5"});
	
	public GermplasmScoreCard(Integer gid, String name) {
		this.gid = gid;
		this.name = name;
	}
	
	public GermplasmScoreCard(Integer gid, String name, List<Observation> observations) {
		this.gid = gid;
		this.name = name;
		this.observations = observations;
	}
	
	public double getScore() {
		int total = 0;
		for (Observation observation : observations) {
			total += Double.parseDouble(observation.getValue());
		}
		return total;
	}

	public Integer getGermplasmId() {
		return gid;
	}

	public String getGermplasmName() {
		return name;
	}
	
	public List<Observation> getIndividualObservations() {
		return observations;
	}
	
	public String getInventoryLocation() {
		return inventoryLocations.get((int)(Math.random()*inventoryLocations.size()));
	}
	
	public void addObservation(Observation o) {
		observations.add(o);
	}

	@Override
	public int compareTo(GermplasmScoreCard other) {
		return Double.compare(other.getScore(), this.getScore());
	}

	@Override
	public String toString() {
		return "GermplasmScoreCard [gid=" + gid + ", name=" + name + ", score=" + getScore() + "]";
	}

	
	

}
