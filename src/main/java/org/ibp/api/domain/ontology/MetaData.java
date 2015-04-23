package org.ibp.api.domain.ontology;

import org.generationcp.middleware.util.ISO8601DateParser;
import java.util.Date;

public class MetaData {
	private String dateCreated;
	private String dateLastModified;
	private Integer observations;

	public String getDateCreated() {
		return this.dateCreated;
	}

	public void setDateCreated(Date dateCreated) {
		if(dateCreated != null){
			this.dateCreated = ISO8601DateParser.toString(dateCreated);
		}
	}

	public String getDateLastModified() {
		return this.dateLastModified;
	}

	public void setDateLastModified(Date dateLastModified) {
		if(dateLastModified != null){
			this.dateLastModified = ISO8601DateParser.toString(dateLastModified);
		}
	}

	public Integer getObservations() {
		return this.observations;
	}

	public void setObservations(Integer observations) {
		this.observations = observations;
	}
}
