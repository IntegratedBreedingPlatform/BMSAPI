package org.ibp.api.brapi.v1.study;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonView;
import org.generationcp.middleware.service.api.BrapiView;
import org.ibp.api.brapi.v1.location.Location;
import org.generationcp.middleware.service.api.study.EnvironmentParameter;
import org.generationcp.middleware.service.api.study.ExperimentalDesign;
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

	private String studyDbId;

	private String studyName;

	private String studyDescription;

	private String studyType;

	private String studyTypeDbId;

	private String studyTypeName;

	@JsonView(BrapiView.BrapiV2.class)
	private String lastUpdate;

	@JsonView(BrapiView.BrapiV2.class)
	private String commonCropName;

	@JsonView(BrapiView.BrapiV2.class)
	private String studyPUI;

	private List<String> seasons;

	private String trialDbId;

	private String trialName;

	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
	private Date startDate;

	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
	private Date endDate;

	private String active;

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
	public StudyDetailsData(final String studyDbId, final String studyName, final String studyDescription, final String studyType,
		final String studyPUI,
		final List<String> seasons, final String trialDbId, final String trialName, final Date startDate, final Date endDate,
		final String active, final Location location, final String culturalPractices, final List<String> dataLinks,
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
	public String getStudyDbId() {
		return this.studyDbId;
	}

	/**
	 * @param studyDbId
	 * @return StudyDetailsData
	 */
	public StudyDetailsData setStudyDbId(final String studyDbId) {
		this.studyDbId = studyDbId;
		return this;
	}

	/**
	 * @return the Study name
	 */
	public String getStudyName() {
		return this.studyName;
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
		return this.studyType;
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
		return this.studyPUI;
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
		return this.seasons;
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
	public String getTrialDbId() {
		return this.trialDbId;
	}

	/**
	 * @param trialDbId
	 * @return StudyDetailsData
	 */
	public StudyDetailsData setTrialDbId(final String trialDbId) {
		this.trialDbId = trialDbId;
		return this;
	}

	/**
	 * @return the additional info
	 */
	public Map<String, String> getAdditionalInfo() {
		return this.additionalInfo;
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
		return this.contacts;
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
	public String getActive() {
		return this.active;
	}

	/**
	 * @param active
	 * @return StudyDetailsData
	 */
	public StudyDetailsData setActive(final String active) {
		this.active = active;
		return this;
	}

	/**
	 * @return the location
	 */
	public Location getLocation() {
		return this.location;
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
		return this.endDate;
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
		return this.startDate;
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
		return this.trialName;
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
		return this.studyDescription;
	}

	public StudyDetailsData setStudyDescription(final String studyDescription) {
		this.studyDescription = studyDescription;
		return this;
	}

	public String getCulturalPractices() {
		return this.culturalPractices;
	}

	public StudyDetailsData setCulturalPractices(final String culturalPractices) {
		this.culturalPractices = culturalPractices;
		return this;
	}

	public List<String> getDataLinks() {
		return this.dataLinks;
	}

	public StudyDetailsData setDataLinks(final List<String> dataLinks) {
		this.dataLinks = dataLinks;
		return this;
	}

	public String getDocumentationURL() {
		return this.documentationURL;
	}

	public StudyDetailsData setDocumentationURL(final String documentationURL) {
		this.documentationURL = documentationURL;
		return this;
	}

	public List<EnvironmentParameter> getEnvironmentParameters() {
		return this.environmentParameters;
	}

	public StudyDetailsData setEnvironmentParameters(final List<EnvironmentParameter> environmentParameters) {
		this.environmentParameters = environmentParameters;
		return this;
	}

	public ExperimentalDesign getExperimentalDesign() {
		return this.experimentalDesign;
	}

	public StudyDetailsData setExperimentalDesign(final ExperimentalDesign experimentalDesign) {
		this.experimentalDesign = experimentalDesign;
		return this;
	}

	public List<String> getExternalReferences() {
		return this.externalReferences;
	}

	public StudyDetailsData setExternalReferences(final List<String> externalReferences) {
		this.externalReferences = externalReferences;
		return this;
	}

	public String getGrowthFacility() {
		return this.growthFacility;
	}

	public StudyDetailsData setGrowthFacility(final String growthFacility) {
		this.growthFacility = growthFacility;
		return this;
	}

	public String getLastUpdate() {
		return this.lastUpdate;
	}

	public StudyDetailsData setLastUpdate(final String lastUpdate) {
		this.lastUpdate = lastUpdate;
		return this;
	}

	public String getCommonCropName() {
		return this.commonCropName;
	}

	public StudyDetailsData setCommonCropName(final String commonCropName) {
		this.commonCropName = commonCropName;
		return this;
	}

	public String getLicense() {
		return this.license;
	}

	public StudyDetailsData setLicense(final String license) {
		this.license = license;
		return this;
	}

	public String getObservationUnitsDescription() {
		return this.observationUnitsDescription;
	}

	public StudyDetailsData setObservationUnitsDescription(final String observationUnitsDescription) {
		this.observationUnitsDescription = observationUnitsDescription;
		return this;
	}

	public String getStudyTypeDbId() {
		return this.studyTypeDbId;
	}

	public void setStudyTypeDbId(final String studyTypeDbId) {
		this.studyTypeDbId = studyTypeDbId;
	}

	public String getStudyTypeName() {
		return this.studyTypeName;
	}

	public void setStudyTypeName(final String studyTypeName) {
		this.studyTypeName = studyTypeName;
	}

	public void addContact(final Contact contact) {
		this.contacts.add(contact);
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
