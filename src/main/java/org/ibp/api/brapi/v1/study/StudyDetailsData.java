package org.ibp.api.brapi.v1.study;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonView;
import org.generationcp.middleware.service.api.BrapiView;
import org.ibp.api.brapi.v1.location.Location;
import org.ibp.api.brapi.v2.study.EnvironmentParameter;
import org.ibp.api.brapi.v2.study.ExperimentalDesign;
import org.pojomatic.Pojomatic;
import org.pojomatic.annotations.AutoProperty;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@AutoProperty
@JsonInclude(JsonInclude.Include.NON_NULL)
public class StudyDetailsData {

	private Integer studyDbId;

	private String studyName;

	@JsonView(BrapiView.BrapiV2.class)
	private String studyDescription;

	private String studyType;

	@JsonView(BrapiView.BrapiV2.class)
	private String lastUpdate;

	@JsonView(BrapiView.BrapiV2.class)
	private String commonCropName;

	@JsonView(BrapiView.BrapiV2.class)
	private String studyPUI;

	private List<String> seasons;

	private Integer trialDbId;

	private String trialName;

	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
	private Date startDate;

	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
	private Date endDate;

	private Boolean active;

	private Location location;

	@JsonView(BrapiView.BrapiV2.class)
	private String culturalPractices;

	@JsonView(BrapiView.BrapiV2.class)
	private List<String> dataLinks;

	@JsonView(BrapiView.BrapiV2.class)
	private String documentationURL;

	@JsonView(BrapiView.BrapiV2.class)
	private List<EnvironmentParameter> environmentParameters;

	@JsonView(BrapiView.BrapiV2.class)
	private ExperimentalDesign experimentalDesign;

	@JsonView(BrapiView.BrapiV2.class)
	private List<String> externalReferences;

	@JsonView(BrapiView.BrapiV2.class)
	private String growthFacility;

	@JsonView(BrapiView.BrapiV2.class)
	private String license;

	@JsonView(BrapiView.BrapiV2.class)
	private String observationUnitsDescription;

	private List<Contact> contacts = new ArrayList<>();

	private Map<String, String> additionalInfo = new HashMap<>();

	/**
	 * Empty constructor
	 */
	public StudyDetailsData() {
	}

	/**
	 * Full constructor
	 *
	 * @param studyDbId
	 * @param studyName
	 * @param studyType
	 * @param seasons
	 * @param trialDbId
	 * @param trialName
	 * @param startDate
	 * @param endDate
	 * @param active
	 * @param location
	 * @param contacts
	 * @param additionalInfo
	 */
	public StudyDetailsData(final Integer studyDbId, final String studyName, final String studyDescription, final String studyType, final String studyPUI,
		final List<String> seasons,	final Integer trialDbId, final String trialName, final Date startDate, final Date endDate,
		final Boolean active, final Location location, final String culturalPractices, final List<String> dataLinks,
		final String documentationURL, final List<EnvironmentParameter> environmentParameters, final ExperimentalDesign experimentalDesign,
		final List<String> externalReferences, final String growthFacility, final String lastUpdate, final String license,
		final String observationUnitsDescription, final List<Contact> contacts, final Map<String, String> additionalInfo) {
		this.studyDbId = studyDbId;
		this.studyName = studyName;
		this.studyDescription = studyDescription;
		this.studyType = studyType;
		this.studyPUI = studyPUI;
		this.seasons = seasons;
		this.trialDbId = trialDbId;
		this.trialName = trialName;
		this.startDate = startDate;
		this.endDate = endDate;
		this.active = active;
		this.location = location;
		this.culturalPractices = culturalPractices;
		this.dataLinks = dataLinks;
		this.documentationURL = documentationURL;
		this.environmentParameters = environmentParameters;
		this.experimentalDesign = experimentalDesign;
		this.externalReferences = externalReferences;
		this.growthFacility = growthFacility;
		this.lastUpdate = lastUpdate;
		this.license = license;
		this.observationUnitsDescription = observationUnitsDescription;
		this.contacts = contacts;
		this.additionalInfo = additionalInfo;
	}

	/**
	 * @return the studyDdId
	 */
	public Integer getStudyDbId() {
		return studyDbId;
	}

	/**
	 * @param studyDbId
	 * @return StudyDetailsData
	 */
	public StudyDetailsData setStudyDbId(final Integer studyDbId) {
		this.studyDbId = studyDbId;
		return this;
	}

	/**
	 * @return the Study name
	 */
	public String getStudyName() {
		return studyName;
	}

	/**
	 * @param studyName
	 * @return StudyDetailsData
	 */
	public StudyDetailsData setStudyName(final String studyName) {
		this.studyName = studyName;
		return this;
	}

	/**
	 * @return the Study type
	 */
	public String getStudyType() {
		return studyType;
	}

	/**
	 * @param studyType
	 * @return StudyDetailsData
	 */
	public StudyDetailsData setStudyType(final String studyType) {
		this.studyType = studyType;
		return this;
	}

	/**
	 * @return the Study PUI
	 */
	public String getStudyPUI() {
		return studyPUI;
	}

	/**
	 * @param studyPUI
	 * @return StudyDetailsData
	 */
	public StudyDetailsData setStudyPUI(final String studyPUI) {
		this.studyPUI = studyPUI;
		return this;
	}

	/**
	 * @return the list of seasons
	 */
	public List<String> getSeasons() {
		return seasons;
	}

	/**
	 * @param seasons
	 * @return StudyDetailsData
	 */
	public StudyDetailsData setSeasons(final List<String> seasons) {
		this.seasons = seasons;
		return this;
	}

	/**
	 * @return the TrialDbId
	 */
	public Integer getTrialDbId() {
		return trialDbId;
	}

	/**
	 * @param trialDbId
	 * @return StudyDetailsData
	 */
	public StudyDetailsData setTrialDbId(final Integer trialDbId) {
		this.trialDbId = trialDbId;
		return this;
	}

	/**
	 * @return the additional info
	 */
	public Map<String, String> getAdditionalInfo() {
		return additionalInfo;
	}

	/**
	 * @param additionalInfo
	 * @return StudyDetailsData
	 */
	public StudyDetailsData setAdditionalInfo(final Map<String, String> additionalInfo) {
		this.additionalInfo = additionalInfo;
		return this;
	}

	/**
	 * @return the list of contacts
	 */
	public List<Contact> getContacts() {
		return contacts;
	}

	/**
	 * @param contacts
	 * @return StudyDetailsData
	 */
	public StudyDetailsData setContacts(final List<Contact> contacts) {
		this.contacts = contacts;
		return this;
	}

	/**
	 * @return active status
	 */
	public Boolean getActive() {
		return active;
	}

	/**
	 * @param active
	 * @return StudyDetailsData
	 */
	public StudyDetailsData setActive(final Boolean active) {
		this.active = active;
		return this;
	}

	/**
	 * @return the location
	 */
	public Location getLocation() {
		return location;
	}

	/**
	 * @param location
	 * @return StudyDetailsData
	 */
	public StudyDetailsData setLocation(final Location location) {
		this.location = location;
		return this;
	}

	/**
	 * @return the end date
	 */
	public Date getEndDate() {
		return endDate;
	}

	/**
	 * @param endDate
	 * @return StudyDetailsData
	 */
	public StudyDetailsData setEndDate(final Date endDate) {
		this.endDate = endDate;
		return this;
	}

	/**
	 * @return the start date
	 */
	public Date getStartDate() {
		return startDate;
	}

	/**
	 * @param startDate
	 * @return StudyDetailsData
	 */
	public StudyDetailsData setStartDate(final Date startDate) {
		this.startDate = startDate;
		return this;
	}

	/**
	 * @return the trial name
	 */
	public String getTrialName() {
		return trialName;
	}

	/**
	 * @param trialName
	 * @return StudyDetailsData
	 */
	public StudyDetailsData setTrialName(final String trialName) {
		this.trialName = trialName;
		return this;
	}

	public String getStudyDescription() {
		return studyDescription;
	}

	public StudyDetailsData setStudyDescription(final String studyDescription) {
		this.studyDescription = studyDescription;
		return this;
	}

	public String getCulturalPractices() {
		return culturalPractices;
	}

	public StudyDetailsData setCulturalPractices(final String culturalPractices) {
		this.culturalPractices = culturalPractices;
		return this;
	}

	public List<String> getDataLinks() {
		return dataLinks;
	}

	public StudyDetailsData setDataLinks(final List<String> dataLinks) {
		this.dataLinks = dataLinks;
		return this;
	}

	public String getDocumentationURL() {
		return documentationURL;
	}

	public StudyDetailsData setDocumentationURL(final String documentationURL) {
		this.documentationURL = documentationURL;
		return this;
	}

	public List<EnvironmentParameter> getEnvironmentParameters() {
		return environmentParameters;
	}

	public StudyDetailsData setEnvironmentParameters(final List<EnvironmentParameter> environmentParameters) {
		this.environmentParameters = environmentParameters;
		return this;
	}

	public ExperimentalDesign getExperimentalDesign() {
		return experimentalDesign;
	}

	public StudyDetailsData setExperimentalDesign(final ExperimentalDesign experimentalDesign) {
		this.experimentalDesign = experimentalDesign;
		return this;
	}

	public List<String> getExternalReferences() {
		return externalReferences;
	}

	public StudyDetailsData setExternalReferences(final List<String> externalReferences) {
		this.externalReferences = externalReferences;
		return this;
	}

	public String getGrowthFacility() {
		return growthFacility;
	}

	public StudyDetailsData setGrowthFacility(final String growthFacility) {
		this.growthFacility = growthFacility;
		return this;
	}

	public String getLastUpdate() {
		return lastUpdate;
	}

	public StudyDetailsData setLastUpdate(final String lastUpdate) {
		this.lastUpdate = lastUpdate;
		return this;
	}

	public String getCommonCropName() {
		return commonCropName;
	}

	public StudyDetailsData setCommonCropName(final String commonCropName) {
		this.commonCropName = commonCropName;
		return this;
	}

	public String getLicense() {
		return license;
	}

	public StudyDetailsData setLicense(final String license) {
		this.license = license;
		return this;
	}

	public String getObservationUnitsDescription() {
		return observationUnitsDescription;
	}

	public StudyDetailsData setObservationUnitsDescription(final String observationUnitsDescription) {
		this.observationUnitsDescription = observationUnitsDescription;
		return this;
	}

	public void addContact(final Contact contact) {
		this.contacts.add(contact);
	}

	@Override public int hashCode() {
		return Pojomatic.hashCode(this);
	}

	@Override public String toString() {
		return Pojomatic.toString(this);
	}

	@Override public boolean equals(Object o) {
		return Pojomatic.equals(this, o);
	}

}
