package org.ibp.api.domain.design;

import java.util.List;
import java.util.Map;

public class BVDesignTrialInstance {

	private Integer instanceNumber;

	private List<Map<String, String>> rows;


	public BVDesignTrialInstance(final Integer instanceNumber, final List<Map<String, String>> rows) {
		super();
		this.instanceNumber = instanceNumber;
		this.rows = rows;
	}

	public Integer getInstanceNumber() {
		return instanceNumber;
	}


	public void setInstanceNumber(Integer instanceNumber) {
		this.instanceNumber = instanceNumber;
	}


	public List<Map<String, String>> getRows() {
		return rows;
	}

}
