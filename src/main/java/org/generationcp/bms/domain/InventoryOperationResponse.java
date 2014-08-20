package org.generationcp.bms.domain;

public class InventoryOperationResponse {

	private String message;
	private Integer lotId;
	private Integer transactionId;

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public Integer getLotId() {
		return lotId;
	}

	public void setLotId(Integer lotId) {
		this.lotId = lotId;
	}

	public Integer getTransactionId() {
		return transactionId;
	}

	public void setTransactionId(Integer transactionId) {
		this.transactionId = transactionId;
	}

}
