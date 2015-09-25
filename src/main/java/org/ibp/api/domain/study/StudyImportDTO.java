
package org.ibp.api.domain.study;

import java.util.ArrayList;
import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

/**
 * DTO used for importing studies.
 *
 */
public class StudyImportDTO {

	@NotNull
	@Size(min = 1)
	@Pattern(regexp = "[A-Za-z 0-9]+")
	private String name;

	private String objective;

	@NotNull
	@Size(min = 1)
	private String title;

	@Pattern(regexp = "[0-9]{8}")
	private String startDate;

	@Pattern(regexp = "[0-9]{8}")
	private String endDate;

	@NotNull
	@Size(min = 1)
	@Pattern(regexp = "[N|T]")
	private String studyType;

	@Pattern(regexp = "[A-Za-z 0-9]+")
	private String siteName;

	// Default value of 1 is for the virtual "Root study folder" which is always present.
	private Long folderId = 1L;

	// Id of the user record in crop database to associate this study's data (e.g. the Germplasm list that is created) with.
	@NotNull
	private Integer userId;

	@Valid
	private List<StudyGermplasm> germplasms = new ArrayList<>();

	@Valid
	private List<Trait> traits = new ArrayList<>();

	@Valid
	private List<ObservationImportDTO> observations = new ArrayList<>();

	public List<Trait> getTraits() {
		return this.traits;
	}

	public void setTraits(final List<Trait> traits) {
		this.traits = traits;
	}

	public List<StudyGermplasm> getGermplasms() {
		return this.germplasms;
	}

	public void setGermplasms(final List<StudyGermplasm> germplasms) {
		this.germplasms = germplasms;
	}

	public String getName() {
		return this.name;
	}

	public void setName(final String name) {
		this.name = name;
	}

	public String getObjective() {
		return this.objective;
	}

	public void setObjective(final String objective) {
		this.objective = objective;
	}

	public String getStartDate() {
		return this.startDate;
	}

	public void setStartDate(final String startDate) {
		this.startDate = startDate;
	}

	public String getEndDate() {
		return this.endDate;
	}

	public void setEndDate(final String endDate) {
		this.endDate = endDate;
	}

	public String getTitle() {
		return this.title;
	}

	public void setTitle(final String title) {
		this.title = title;
	}

	public String getStudyType() {
		return this.studyType;
	}

	public void setStudyType(final String studyType) {
		this.studyType = studyType;
	}

	public String getSiteName() {
		return this.siteName;
	}

	public void setSiteName(final String siteName) {
		this.siteName = siteName;
	}

	public Long getFolderId() {
		return this.folderId;
	}

	public void setFolderId(final Long folderId) {
		this.folderId = folderId;
	}

	public List<ObservationImportDTO> getObservations() {
		return this.observations;
	}

	public Integer getUserId() {
		return this.userId;
	}

	public void setUserId(final Integer userId) {
		this.userId = userId;
	}

	public void setObservations(final List<ObservationImportDTO> observations) {
		this.observations = observations;
	}

	public String findTraitValue(final Integer gid, final Integer traitId) {
		for (final ObservationImportDTO observation : this.observations) {
			if (observation.getGid().equals(gid)) {
				for (final MeasurementImportDTO measurement : observation.getMeasurements()) {
					if (measurement.getTraitId().equals(traitId)) {
						return measurement.getTraitValue();
					}
				}
			}
		}
		return null;
	}
}
