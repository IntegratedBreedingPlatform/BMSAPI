package org.ibp.api.brapi.v1.study;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.ibp.api.brapi.v1.location.Location;
import org.pojomatic.Pojomatic;
import org.pojomatic.annotations.AutoProperty;

import java.util.List;
import java.util.Map;

@AutoProperty
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({"studyDbId", "studyName", "studyType", "seasons", "trialDbId", "trialName", "startDate", "endDate", "active",
		"location", "contacts", "additionalInfo"})
public class StudyDetailsData {

	private Integer studyDbId;

	private String studyName;

	private String studyType;

	private List<String> seasons;

	private Integer trialDbId;

	private String trialName;

	private String startDate;

	private String endDate;

	private Boolean active;

	private Location location;

	private List<Contact> contacts;

	private Map<String, String> additionalInfo;

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
	public StudyDetailsData(final Integer studyDbId, final String studyName, final String studyType, final List<String> seasons,
			final Integer trialDbId, final String trialName, final String startDate, final String endDate, final Boolean active,
			final Location location, final List<Contact> contacts, final Map<String, String> additionalInfo) {
		this.studyDbId = studyDbId;
		this.studyName = studyName;
		this.studyType = studyType;
		this.seasons = seasons;
		this.trialDbId = trialDbId;
		this.trialName = trialName;
		this.startDate = startDate;
		this.endDate = endDate;
		this.active = active;
		this.location = location;
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
	public String getEndDate() {
		return endDate;
	}

	/**
	 * @param endDate
	 * @return StudyDetailsData
	 */
	public StudyDetailsData setEndDate(final String endDate) {
		this.endDate = endDate;
		return this;
	}

	/**
	 * @return the start date
	 */
	public String getStartDate() {
		return startDate;
	}

	/**
	 * @param startDate
	 * @return StudyDetailsData
	 */
	public StudyDetailsData setStartDate(final String startDate) {
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
