package org.ibp.api.brapi.v1.study;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonView;
import org.apache.commons.lang.StringUtils;
import org.generationcp.middleware.service.api.BrapiView;
import org.generationcp.middleware.service.api.study.ObservationLevel;
import org.generationcp.middleware.service.api.study.EnvironmentParameter;
import org.generationcp.middleware.service.api.study.ExperimentalDesign;
import org.pojomatic.Pojomatic;
import org.pojomatic.annotations.AutoProperty;

import com.fasterxml.jackson.annotation.JsonInclude;

@AutoProperty
@JsonInclude(JsonInclude.Include.NON_NULL)
public class StudySummaryDto {

	@JsonView(BrapiView.BrapiV2.class)
	private String commonCropName;

	@JsonView(BrapiView.BrapiV2.class)
	private String active;

	@JsonView(BrapiView.BrapiV2.class)
	private Map<String, String> additionalInfo;

	@JsonView(BrapiView.BrapiV2.class)
	private List<Contact> contacts = new ArrayList<>();

	@JsonView(BrapiView.BrapiV2.class)
	private String culturalPractices;

	@JsonView(BrapiView.BrapiV2.class)
	private List<String> dataLinks = new ArrayList<>();

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
	private Map<String, String> lastUpdate;

	@JsonView(BrapiView.BrapiV2.class)
	private String license;

	@JsonView(BrapiView.BrapiV2.class)
	private String observationUnitsDescription = StringUtils.EMPTY;

	@JsonView(BrapiView.BrapiV2.class)
	private Date startDate;

	@JsonView(BrapiView.BrapiV2.class)
	private String studyCode;

	@JsonView(BrapiView.BrapiV2.class)
	private String studyDescription;

	@JsonView(BrapiView.BrapiV2.class)
	private String studyPUI;

	@JsonView(BrapiView.BrapiV2.class)
	private Integer trialDbid;

	@JsonView(BrapiView.BrapiV2.class)
	private String trialName;

	private Integer studyDbId;

	private String studyName;

	private String studyType;

	private List<String> years;

	private List<String> seasons;

	private String locationDbId;

	private String locationName;

	@JsonView(BrapiView.BrapiV2.class)
	private List<ObservationLevel> observationLevels;

	private String programDbId;

	private Map<String, String> optionalInfo;

	/**
	 *
	 * @return The study db id
	 */
	public Integer getStudyDbId() {
		return this.studyDbId;
	}

	/**
	 *
	 * @param studyDbId
	 * @return this
	 */
	public StudySummaryDto setStudyDbId(final Integer studyDbId) {
		this.studyDbId = studyDbId;
		return this;
	}

	/**
	 *
	 * @return The studyName
	 */
	public String getStudyName() {
		return this.studyName;
	}

	/**
	 *
	 * @param studyName
	 * @return this
	 */
	public StudySummaryDto setStudyName(final String studyName) {
		this.studyName = studyName;
		return this;
	}

	/**
	 *
	 * @return The study type
	 */
	public String getStudyType() {
		return this.studyType;
	}

	/**
	 *
	 * @param studyType
	 * @return this
	 */
	public StudySummaryDto setStudyType(final String studyType) {
		this.studyType = studyType;
		return this;
	}

	/**
	 *
	 * @return The list of years
	 */
	public List<String> getYears() {
		return this.years;
	}

	/**
	 *
	 * @param years
	 * @return this
	 */
	public StudySummaryDto setYears(final List<String> years) {
		this.years = years;
		return this;
	}

	/**
	 *
	 * @return The list of seasons
	 */
	public List<String> getSeasons() {
		return this.seasons;
	}

	/**
	 *
	 * @param seasons
	 * @return this
	 */
	public StudySummaryDto setSeasons(final List<String> seasons) {
		this.seasons = seasons;
		return this;
	}

	/**
	 *
	 * @return The location db id
	 */
	public String getLocationDbId() {
		return this.locationDbId;
	}

	/**
	 *
	 * @param locationDbId
	 * @return this
	 */
	public StudySummaryDto setLocationDbId(final String locationDbId) {
		this.locationDbId = locationDbId;
		return this;
	}

	public String getLocationName() {
		return this.locationName;
	}

	public StudySummaryDto setLocationName(final String locationName) {
		this.locationName = locationName;
		return this;
	}

	/**
	 *
	 * @return The program db id
	 */
	public String getProgramDbId() {
		return this.programDbId;
	}

	/**
	 *
	 * @param programDbId
	 * @return this
	 */
	public StudySummaryDto setProgramDbId(final String programDbId) {
		this.programDbId = programDbId;
		return this;
	}

	/**
	 *
	 * @return The map with the optional info
	 */
	public Map<String, String> getOptionalInfo() {
		return this.optionalInfo;
	}

	/**
	 *
	 * @param name
	 *            Key of the optional info
	 * @param value
	 *            Value of the optional info
	 */
	public void setOptionalInfo(final String name, final String value) {
		this.optionalInfo.put(name, value);
	}

	/**
	 *
	 * @param name
	 *            Key of the optional info
	 * @param value
	 *            Value of the optional info
	 * @return this
	 */
	public StudySummaryDto addOptionalInfo(final String name, final String value) {
		this.optionalInfo.put(name, value);
		return this;
	}

	/**
	 *
	 * @param optionalInfo
	 * @return this
	 */
	public StudySummaryDto setOptionalInfo(final Map<String, String> optionalInfo) {
		this.optionalInfo = optionalInfo;
		return this;
	}

	public String getCommonCropName() {
		return this.commonCropName;
	}

	public void setCommonCropName(final String commonCropName) {
		this.commonCropName = commonCropName;
	}

	public String isActive() {
		return this.active;
	}

	public void setActive(final String active) {
		this.active = active;
	}

	public Map<String, String> getAdditionalInfo() {
		return this.additionalInfo;
	}

	public void setAdditionalInfo(final Map<String, String> additionalInfo) {
		this.additionalInfo = additionalInfo;
	}

	public List<Contact> getContacts() {
		return this.contacts;
	}

	public void setContacts(final List<Contact> contacts) {
		this.contacts = contacts;
	}

	public String getCulturalPractices() {
		return this.culturalPractices;
	}

	public void setCulturalPractices(final String culturalPractices) {
		this.culturalPractices = culturalPractices;
	}

	public List<String> getDataLinks() {
		return this.dataLinks;
	}

	public void setDataLinks(final List<String> dataLinks) {
		this.dataLinks = dataLinks;
	}

	public String getDocumentationURL() {
		return this.documentationURL;
	}

	public void setDocumentationURL(final String documentationURL) {
		this.documentationURL = documentationURL;
	}

	public List<EnvironmentParameter> getEnvironmentParameters() {
		return this.environmentParameters;
	}

	public void setEnvironmentParameters(final List<EnvironmentParameter> environmentParameters) {
		this.environmentParameters = environmentParameters;
	}

	public ExperimentalDesign getExperimentalDesign() {
		return this.experimentalDesign;
	}

	public void setExperimentalDesign(final ExperimentalDesign experimentalDesign) {
		this.experimentalDesign = experimentalDesign;
	}

	public List<String> getExternalReferences() {
		return this.externalReferences;
	}

	public void setExternalReferences(final List<String> externalReferences) {
		this.externalReferences = externalReferences;
	}

	public String getGrowthFacility() {
		return this.growthFacility;
	}

	public void setGrowthFacility(final String growthFacility) {
		this.growthFacility = growthFacility;
	}

	public Map<String, String> getLastUpdate() {
		return this.lastUpdate;
	}

	public void setLastUpdate(final Map<String, String> lastUpdate) {
		this.lastUpdate = lastUpdate;
	}

	public String getLicense() {
		return this.license;
	}

	public void setLicense(final String license) {
		this.license = license;
	}

	public String getObservationUnitsDescription() {
		return this.observationUnitsDescription;
	}

	public void setObservationUnitsDescription(final String observationUnitsDescription) {
		this.observationUnitsDescription = observationUnitsDescription;
	}

	public Date getStartDate() {
		return this.startDate;
	}

	public void setStartDate(final Date startDate) {
		this.startDate = startDate;
	}

	public String getStudyCode() {
		return this.studyCode;
	}

	public void setStudyCode(final String studyCode) {
		this.studyCode = studyCode;
	}

	public String getStudyDescription() {
		return this.studyDescription;
	}

	public void setStudyDescription(final String studyDescription) {
		this.studyDescription = studyDescription;
	}

	public String getStudyPUI() {
		return this.studyPUI;
	}

	public void setStudyPUI(final String studyPUI) {
		this.studyPUI = studyPUI;
	}

	public Integer getTrialDbid() {
		return this.trialDbid;
	}

	public void setTrialDbid(final Integer trialDbid) {
		this.trialDbid = trialDbid;
	}

	public String getTrialName() {
		return this.trialName;
	}

	public void setTrialName(final String trialName) {
		this.trialName = trialName;
	}

	public String getActive() {
		return this.active;
	}

	public List<ObservationLevel> getObservationLevels() {
		return this.observationLevels;
	}

	public void setObservationLevels(final List<ObservationLevel> observationLevels) {
		this.observationLevels = observationLevels;
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
