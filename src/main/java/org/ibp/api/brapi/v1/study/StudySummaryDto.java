package org.ibp.api.brapi.v1.study;

import java.util.List;
import java.util.Map;

import org.pojomatic.Pojomatic;
import org.pojomatic.annotations.AutoProperty;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@AutoProperty
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "studyDbId", "studyName", "studyType", "years", "seasons", "locationDbId", "locationName", "programDbId",
	"optionalInfo" })
public class StudySummaryDto {

	private Integer studyDbId;

	private String studyName;

	private String studyType;

	private List<String> years;

	private List<String> seasons;

	private String locationDbId;

	private String locationName;

	private String programDbId;

	private Map<String, String> optionalInfo;

	/**
	 *
	 * @return The study db id
	 */
	public Integer getStudyDbId() {
		return studyDbId;
	}

	/**
	 *
	 * @param studyDbId
	 * @return this
	 */
	public StudySummaryDto setStudyDbId(Integer studyDbId) {
		this.studyDbId = studyDbId;
		return this;
	}

	/**
	 *
	 * @return The studyName
	 */
	public String getStudyName() {
		return studyName;
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
		return studyType;
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
		return years;
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
		return seasons;
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
		return locationDbId;
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

	public StudySummaryDto setLocationName(String locationName) {
		this.locationName = locationName;
		return this;
	}

	/**
	 *
	 * @return The program db id
	 */
	public String getProgramDbId() {
		return programDbId;
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
		return optionalInfo;
	}

	/**
	 *
	 * @param name
	 *            Key of the optional info
	 * @param value
	 *            Value of the optional info
	 */
	public void setOptionalInfo(final String name, final String value) {
		optionalInfo.put(name, value);
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
		optionalInfo.put(name, value);
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
