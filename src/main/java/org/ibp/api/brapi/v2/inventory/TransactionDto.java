package org.ibp.api.brapi.v2.inventory;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import org.pojomatic.annotations.AutoProperty;

import java.util.Date;
import java.util.Map;

@AutoProperty
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TransactionDto {

	private Map<String, Object> additionalInfo;
	private Double amount;
	private String transactionDescription;
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyyMMdd")
	private Date transactionTimestamp;

	private String units;
	private String transactionDbId;

	public Map<String, Object> getAdditionalInfo() {
		return additionalInfo;
	}

	public void setAdditionalInfo(final Map<String, Object> additionalInfo) {
		this.additionalInfo = additionalInfo;
	}

	public Double getAmount() {
		return amount;
	}

	public void setAmount(final Double amount) {
		this.amount = amount;
	}

	public String getTransactionDescription() {
		return transactionDescription;
	}

	public void setTransactionDescription(final String transactionDescription) {
		this.transactionDescription = transactionDescription;
	}

	public Date getTransactionTimestamp() {
		return transactionTimestamp;
	}

	public void setTransactionTimestamp(final Date transactionTimestamp) {
		this.transactionTimestamp = transactionTimestamp;
	}

	public String getUnits() {
		return units;
	}

	public void setUnits(final String units) {
		this.units = units;
	}

	public String getTransactionDbId() {
		return transactionDbId;
	}

	public void setTransactionDbId(final String transactionDbId) {
		this.transactionDbId = transactionDbId;
	}
}
