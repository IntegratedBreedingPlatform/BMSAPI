
package org.ibp.api.brapi.v1.trial;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonView;
import org.apache.commons.lang.StringUtils;
import org.generationcp.middleware.api.brapi.v2.germplasm.ExternalReferenceDTO;
import org.generationcp.middleware.service.api.BrapiView;
import org.ibp.api.brapi.v1.study.Contact;
import org.ibp.api.brapi.v1.study.StudySummaryDto;
import org.pojomatic.Pojomatic;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class TrialSummary {

	private String trialDbId;

	private String trialName;

	@JsonView({BrapiView.BrapiV2.class, BrapiView.BrapiV2_1.class})
	private String trialDescription;

	@JsonView({BrapiView.BrapiV2.class, BrapiView.BrapiV2_1.class})
	private String trialPUI;

	@JsonView({BrapiView.BrapiV2.class, BrapiView.BrapiV2_1.class})
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

	@JsonView({BrapiView.BrapiV2.class, BrapiView.BrapiV2_1.class})
	private List<Contact> contacts = new ArrayList<>();

	@JsonView({BrapiView.BrapiV2.class, BrapiView.BrapiV2_1.class})
	private String documentationURL = StringUtils.EMPTY;

	@JsonView({BrapiView.BrapiV2.class, BrapiView.BrapiV2_1.class})
	private List<ExternalReferenceDTO> externalReferences = new ArrayList<>();

	@JsonView({BrapiView.BrapiV2.class, BrapiView.BrapiV2_1.class})
	private List<String> publications = new ArrayList<>();

	@JsonView({BrapiView.BrapiV2.class, BrapiView.BrapiV2_1.class})
	private List<String> datasetAuthorships = new ArrayList<>();

	public TrialSummary() {

	}

	public String getTrialDbId() {
		return this.trialDbId;
	}

	public void setTrialDbId(final String trialDbId) {
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

	public void setProgramName(final String programName) {
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

	public void setStudies(final List<StudySummaryDto> studies) {
		this.studies = studies;
	}

	public void addStudy(final StudySummaryDto study) {
		this.studies.add(study);
	}

	public Map<String, String> getAdditionalInfo() {
		return this.additionalInfo;
	}

	public void setAdditionalInfo(final Map<String, String> additionalInfo) {
		this.additionalInfo = additionalInfo;
	}

	public void addAdditionalInfo(final String name, final String value) {
		this.additionalInfo.put(name, value);
	}

	public String getLocationDbId() {
		return this.locationDbId;
	}

	public TrialSummary setLocationDbId(final String locationDbId) {
		this.locationDbId = locationDbId;
		return this;
	}

	public String getTrialDescription() {
		return this.trialDescription;
	}

	public void setTrialDescription(final String trialDescription) {
		this.trialDescription = trialDescription;
	}

	public String getTrialPUI() {
		return this.trialPUI;
	}

	public void setTrialPUI(final String trialPUI) {
		this.trialPUI = trialPUI;
	}

	public String getCommonCropName() {
		return this.commonCropName;
	}

	public void setCommonCropName(final String commonCropName) {
		this.commonCropName = commonCropName;
	}

	public List<Contact> getContacts() {
		return this.contacts;
	}

	public void setContacts(final List<Contact> contacts) {
		this.contacts = contacts;
	}

	public String getDocumentationURL() {
		return this.documentationURL;
	}

	public void setDocumentationURL(final String documentationURL) {
		this.documentationURL = documentationURL;
	}

	public List<ExternalReferenceDTO> getExternalReferences() {
		return this.externalReferences;
	}

	public void setExternalReferences(final List<ExternalReferenceDTO> externalReferences) {
		this.externalReferences = externalReferences;
	}

	public List<String> getPublications() {
		return this.publications;
	}

	public void setPublications(final List<String> publications) {
		this.publications = publications;
	}

	public List<String> getDatasetAuthorships() {
		return this.datasetAuthorships;
	}

	public void setDatasetAuthorships(final List<String> datasetAuthorships) {
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
	public boolean equals(final Object o) {
		return Pojomatic.equals(this, o);
	}
}
