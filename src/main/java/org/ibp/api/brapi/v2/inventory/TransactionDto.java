package org.ibp.api.brapi.v2.inventory;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.generationcp.middleware.util.serializer.DatePropertySerializer;
import org.pojomatic.annotations.AutoProperty;

import java.util.Date;
import java.util.Map;

@AutoProperty
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TransactionDto {

	private Map<String, Object> additionalInfo;
	private Double amount;
	private String transactionDescription;
	@JsonSerialize(using = DatePropertySerializer.class)
	private Date transactionTimestamp;

	private String units;
	private String transactionDbId;

	public Map<String, Object> getAdditionalInfo() {
		return this.additionalInfo;
	}

	public void setAdditionalInfo(final Map<String, Object> additionalInfo) {
		this.additionalInfo = additionalInfo;
	}

	public Double getAmount() {
		return this.amount;
	}

	public void setAmount(final Double amount) {
		this.amount = amount;
	}

	public String getTransactionDescription() {
		return this.transactionDescription;
	}

	public void setTransactionDescription(final String transactionDescription) {
		this.transactionDescription = transactionDescription;
	}

	public Date getTransactionTimestamp() {
		return this.transactionTimestamp;
	}

	public void setTransactionTimestamp(final Date transactionTimestamp) {
		this.transactionTimestamp = transactionTimestamp;
	}

	public String getUnits() {
		return this.units;
	}

	public void setUnits(final String units) {
		this.units = units;
	}

	public String getTransactionDbId() {
		return this.transactionDbId;
	}

	public void setTransactionDbId(final String transactionDbId) {
		this.transactionDbId = transactionDbId;
	}
}
