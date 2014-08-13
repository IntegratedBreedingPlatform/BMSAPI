package org.generationcp.bms.domain;

import org.generationcp.middleware.domain.oms.TermSummary;
import org.generationcp.middleware.pojos.ims.LotStatus;

public class GermplasmInventoryInfo {

	private final Integer gid;
	
	private Integer lotId;

	private Double quantityAvailable;
	private Double quantityReserved;

	private TermSummary quantityUnit;

	private Integer userId;
	private String userName;

	private LocationInfo location;

	private LotStatus lotStatus;

	private String comments;

	public GermplasmInventoryInfo(Integer gid) {
		this.gid = gid;
	}

	public Integer getGid() {
		return gid;
	}
	
	public Integer getLotId() {
		return lotId;
	}

	public void setLotId(Integer lotId) {
		this.lotId = lotId;
	}

	public Double getQuantityAvailable() {
		return quantityAvailable;
	}

	public void setQuantityAvailable(Double quantityAvailable) {
		this.quantityAvailable = quantityAvailable;
	}

	public Double getQuantityReserved() {
		return quantityReserved;
	}

	public void setQuantityReserved(Double quantityReserved) {
		this.quantityReserved = quantityReserved;
	}
	
	public Double getQuantityTotal() {
		return quantityAvailable + quantityReserved;
	}

	public TermSummary getQuantityUnit() {
		return quantityUnit;
	}

	public void setQuantityUnit(TermSummary quantityUnit) {
		this.quantityUnit = quantityUnit;
	}

	public Integer getUserId() {
		return userId;
	}

	public void setUserId(Integer userId) {
		this.userId = userId;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public LocationInfo getLocation() {
		return location;
	}

	public void setLocation(LocationInfo locationInfo) {
		this.location = locationInfo;
	}

	public LotStatus getLotStatus() {
		return lotStatus;
	}

	public void setLotStatus(LotStatus lotStatus) {
		this.lotStatus = lotStatus;
	}

	public String getComments() {
		return comments;
	}

	public void setComments(String comments) {
		this.comments = comments;
	}

}
