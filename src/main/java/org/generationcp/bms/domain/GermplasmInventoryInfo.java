package org.generationcp.bms.domain;

import com.wordnik.swagger.annotations.ApiModel;
import org.generationcp.middleware.pojos.ims.LotStatus;

@ApiModel("Germplasm Inventory Information")
public class GermplasmInventoryInfo {

	private Integer gid;
	private Integer lotId;
	private Double quantityAvailable;
	private Double quantityReserved;
	private Double quantityTotal;
	private TermSummary quantityUnit;
	private Integer userId;
	private String userName;
	private LocationInfo location;
	private LotStatus lotStatus;
	private String comments;

	public GermplasmInventoryInfo() { }

	public GermplasmInventoryInfo(Integer gid) {
		this.gid = gid;
	}

	public Integer getGid() {
		return gid;
	}
	
	public void setGid(Integer gid) {
		this.gid = gid;
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
		return quantityTotal;
	}

	public void setQuantityTotal(Double quantityTotal) {
		this.quantityTotal = quantityTotal;
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

	@Override
	public String toString() {
		return "GermplasmInventoryInfo [gid=" + gid + ", lotId=" + lotId + ", quantityAvailable="
				+ quantityAvailable + ", quantityReserved=" + quantityReserved + ", quantityUnit="
				+ quantityUnit + ", userId=" + userId + ", userName=" + userName + ", location="
				+ location + ", lotStatus=" + lotStatus + ", comments=" + comments + "]";
	}

}
