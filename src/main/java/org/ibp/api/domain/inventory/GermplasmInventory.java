
package org.ibp.api.domain.inventory;

import io.swagger.annotations.ApiModel;
import org.generationcp.middleware.pojos.ims.LotStatus;
import org.ibp.api.domain.ontology.TermSummary;

@ApiModel("Germplasm Inventory Information")
public class GermplasmInventory {

	private Integer gid;
	private Integer lotId;
	private Double quantityAvailable;
	private Double quantityReserved;
	private Double quantityTotal;
	private TermSummary quantityUnit;
	private Integer userId;
	private String userName;
	private InventoryLocation location;
	private LotStatus lotStatus;
	private String comments;

	public GermplasmInventory() {
	}

	public GermplasmInventory(Integer gid) {
		this.gid = gid;
	}

	public Integer getGid() {
		return this.gid;
	}

	public void setGid(Integer gid) {
		this.gid = gid;
	}

	public Integer getLotId() {
		return this.lotId;
	}

	public void setLotId(Integer lotId) {
		this.lotId = lotId;
	}

	public Double getQuantityAvailable() {
		return this.quantityAvailable;
	}

	public void setQuantityAvailable(Double quantityAvailable) {
		this.quantityAvailable = quantityAvailable;
	}

	public Double getQuantityReserved() {
		return this.quantityReserved;
	}

	public void setQuantityReserved(Double quantityReserved) {
		this.quantityReserved = quantityReserved;
	}

	public Double getQuantityTotal() {
		return this.quantityTotal;
	}

	public void setQuantityTotal(Double quantityTotal) {
		this.quantityTotal = quantityTotal;
	}

	public TermSummary getQuantityUnit() {
		return this.quantityUnit;
	}

	public void setQuantityUnit(TermSummary quantityUnit) {
		this.quantityUnit = quantityUnit;
	}

	public Integer getUserId() {
		return this.userId;
	}

	public void setUserId(Integer userId) {
		this.userId = userId;
	}

	public String getUserName() {
		return this.userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public InventoryLocation getLocation() {
		return this.location;
	}

	public void setLocation(InventoryLocation inventoryLocation) {
		this.location = inventoryLocation;
	}

	public LotStatus getLotStatus() {
		return this.lotStatus;
	}

	public void setLotStatus(LotStatus lotStatus) {
		this.lotStatus = lotStatus;
	}

	public String getComments() {
		return this.comments;
	}

	public void setComments(String comments) {
		this.comments = comments;
	}

	@Override
	public String toString() {
		return "GermplasmInventoryInfo [gid=" + this.gid + ", lotId=" + this.lotId + ", quantityAvailable=" + this.quantityAvailable
				+ ", quantityReserved=" + this.quantityReserved + ", quantityUnit=" + this.quantityUnit + ", userId=" + this.userId
				+ ", userName=" + this.userName + ", location=" + this.location + ", lotStatus=" + this.lotStatus + ", comments="
				+ this.comments + "]";
	}

}
