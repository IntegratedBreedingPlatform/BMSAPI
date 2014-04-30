package org.generationcp.bms.domain;



public class GermplasmScoreCard implements Comparable<GermplasmScoreCard> {
	
	private final Integer gid;
	private final String name;
	private int score = 0;
	
	public GermplasmScoreCard(Integer gid, String name) {
		this.gid = gid;
		this.name = name;
	}
	
	public int getScore() {
		return score;
	}

	public void setScore(int score) {
		this.score = score;
	}

	public Integer getGid() {
		return gid;
	}

	public String getName() {
		return name;
	}

	public void incrementScore(int valueToAdd) {
		this.score += valueToAdd;		
	}
	
	@Override
	public int compareTo(GermplasmScoreCard other) {
		return other.getScore() - this.score;
	}

	@Override
	public String toString() {
		return "GermplasmScoreCard [gid=" + gid + ", name=" + name + ", score=" + score + "]";
	}

	
	

}
