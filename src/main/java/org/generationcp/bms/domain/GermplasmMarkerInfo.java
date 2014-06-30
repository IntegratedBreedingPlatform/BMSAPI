package org.generationcp.bms.domain;

import java.util.ArrayList;
import java.util.List;

public class GermplasmMarkerInfo {

	private final Integer gid;
	private final List<MarkerCount> markerCounts = new ArrayList<MarkerCount>();

	public GermplasmMarkerInfo(Integer gid) {
		if(gid == null) {
			throw new IllegalArgumentException("gid is required to construct GermplasmMarkerInfo");
		}
		this.gid = gid;
	}

	public void addMarkerCount(MarkerCount markerCount) {
		if (markerCount != null) {
			this.markerCounts.add(markerCount);
		}
	}

	public Integer getGid() {
		return gid;
	}

	public List<MarkerCount> getMarkerCounts() {
		return markerCounts;
	}

}
