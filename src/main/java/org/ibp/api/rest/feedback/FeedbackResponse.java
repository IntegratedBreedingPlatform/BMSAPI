package org.ibp.api.rest.feedback;

import org.generationcp.middleware.pojos.workbench.feedback.FeedbackFeature;
import org.pojomatic.Pojomatic;
import org.pojomatic.annotations.AutoProperty;

@AutoProperty
public class FeedbackResponse {

	private FeedbackFeature feedbackFeature;
	private String collectorId;

	public FeedbackResponse(final String collectorId, final FeedbackFeature feedbackFeature){
		this.feedbackFeature = feedbackFeature;
		this.collectorId = collectorId;
	}

	public String getCollectorId() {
		return this.collectorId;
	}

	public void setCollectorId(final String collectorId) {
		this.collectorId = collectorId;
	}

	@Override
	public int hashCode() {
		return Pojomatic.hashCode(this);
	}

	@Override
	public String toString() {
		return Pojomatic.toString(this);
	}

	@Override
	public boolean equals(final Object o) {
		return Pojomatic.equals(this, o);
	}

}
