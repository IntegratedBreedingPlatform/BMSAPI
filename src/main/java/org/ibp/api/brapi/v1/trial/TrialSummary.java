
package org.ibp.api.brapi.v1.trial;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonView;
import org.generationcp.middleware.service.api.BrapiView;
import org.ibp.api.brapi.v1.study.Contact;
import org.ibp.api.brapi.v1.study.StudySummaryDto;
import org.pojomatic.Pojomatic;

import java.util.*;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class TrialSummary {

	private Integer trialDbId;

	private String trialName;

	@JsonView(BrapiView.BrapiV2.class)
	private String trialDescription;

	@JsonView(BrapiView.BrapiV2.class)
	private String trialPUI;

	@JsonView(BrapiView.BrapiV2.class)
	private String commonCropName;

	private String programDbId;

	private String programName;

	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
	private Date startDate;

	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
	private Date endDate;

	private String locationDbId;

	private boolean active;

	@JsonView(BrapiView.BrapiV1_3.class)
	private List<StudySummaryDto> studies = new ArrayList<>();

	private Map<String, String> additionalInfo = new HashMap<String, String>();

	@JsonView(BrapiView.BrapiV2.class)
	private List<Contact> contacts = new ArrayList<>();

	@JsonView(BrapiView.BrapiV2.class)
	private String documentationURL;

	@JsonView(BrapiView.BrapiV2.class)
	private List<String> externalReferences;

	@JsonView(BrapiView.BrapiV2.class)
	private String publications;

	@JsonView(BrapiView.BrapiV2.class)
	private String datasetAuthorships;

	public TrialSummary() {

	}

	public Integer getTrialDbId() {
		return this.trialDbId;
	}

	public void setTrialDbId(final Integer trialDbId) {
		this.trialDbId = trialDbId;
	}

	public String getTrialName() {
		return this.trialName;
	}

	public void setTrialName(final String trialName) {
		this.trialName = trialName;
	}

	public String getProgramDbId() {
		return this.programDbId;
	}

	public String getProgramName() {
		return this.programName;
	}

	public void setProgramName(String programName) {
		this.programName = programName;
	}

	public void setProgramDbId(final String programDbId) {
		this.programDbId = programDbId;
	}

	public Date getStartDate() {
		return this.startDate;
	}

	public void setStartDate(final Date startDate) {
		this.startDate = startDate;
	}

	public Date getEndDate() {
		return this.endDate;
	}

	public void setEndDate(final Date endDate) {
		this.endDate = endDate;
	}

	public boolean isActive() {
		return this.active;
	}

	public void setActive(final boolean active) {
		this.active = active;
	}

	public List<StudySummaryDto> getStudies() {
		return this.studies;
	}

	public void setStudies(List<StudySummaryDto> studies) {
		this.studies = studies;
	}

	public void addStudy(final StudySummaryDto study) {
		this.studies.add(study);
	}

	public Map<String, String> getAdditionalInfo() {
		return this.additionalInfo;
	}

	public void setAdditionalInfo(Map<String, String> additionalInfo) {
		this.additionalInfo = additionalInfo;
	}

	public void addAdditionalInfo(final String name, final String value) {
		this.additionalInfo.put(name, value);
	}

	public String getLocationDbId() {
		return locationDbId;
	}

	public TrialSummary setLocationDbId(final String locationDbId) {
		this.locationDbId = locationDbId;
		return this;
	}

	public String getTrialDescription() {
		return trialDescription;
	}

	public void setTrialDescription(final String trialDescription) {
		this.trialDescription = trialDescription;
	}

	public String getTrialPUI() {
		return trialPUI;
	}

	public void setTrialPUI(final String trialPUI) {
		this.trialPUI = trialPUI;
	}

	public String getCommonCropName() {
		return commonCropName;
	}

	public void setCommonCropName(final String commonCropName) {
		this.commonCropName = commonCropName;
	}

	public List<Contact> getContacts() {
		return contacts;
	}

	public void setContacts(final List<Contact> contacts) {
		this.contacts = contacts;
	}

	public String getDocumentationURL() {
		return documentationURL;
	}

	public void setDocumentationURL(final String documentationURL) {
		this.documentationURL = documentationURL;
	}

	public List<String> getExternalReferences() {
		return externalReferences;
	}

	public void setExternalReferences(final List<String> externalReferences) {
		this.externalReferences = externalReferences;
	}

	public String getPublications() {
		return publications;
	}

	public void setPublications(final String publications) {
		this.publications = publications;
	}

	public String getDatasetAuthorships() {
		return datasetAuthorships;
	}

	public void setDatasetAuthorships(final String datasetAuthorships) {
		this.datasetAuthorships = datasetAuthorships;
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
	public boolean equals(Object o) {
		return Pojomatic.equals(this, o);
	}
}
