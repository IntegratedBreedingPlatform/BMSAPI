
package org.ibp.api.domain.study;

import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.Pattern;

import org.hibernate.validator.constraints.NotBlank;
import org.hibernate.validator.constraints.NotEmpty;

/**
 * DTO used for importing studies.
 *
 */
public class StudyImportDTO {

	@NotBlank
	@Pattern(regexp = "[A-Za-z 0-9]+")
	private String name;

	@Pattern(regexp = "[A-Za-z 0-9]+")
	private String objective;

	@Pattern(regexp = "[0-9]{8}")
	private String startDate;

	@Pattern(regexp = "[0-9]{8}")
	private String endDate;
	private String title;

	@NotEmpty
	@Pattern(regexp = "[N|T]")
	private String studyType;

	@Pattern(regexp = "[A-Za-z 0-9]+")
	private String siteName;

	@NotEmpty
	@Valid
	private List<StudyGermplasm> germplasms;

	@NotEmpty
	@Valid
	private List<Trait> traits;

	private String[][] traitValues;

	public List<Trait> getTraits() {
		return this.traits;
	}

	public void setTraits(final List<Trait> traits) {
		this.traits = traits;
	}

	public String[][] getTraitValues() {
		return this.traitValues;
	}

	public void setTraitValues(final String[][] traitValues) {
		this.traitValues = traitValues;
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
}
