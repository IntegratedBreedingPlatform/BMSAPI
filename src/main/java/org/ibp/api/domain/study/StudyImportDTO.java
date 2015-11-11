
package org.ibp.api.domain.study;

import java.util.ArrayList;
import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import org.hibernate.validator.constraints.NotBlank;
import org.hibernate.validator.constraints.NotEmpty;

import com.wordnik.swagger.annotations.ApiModelProperty;

/**
 * DTO used for importing studies.
 *
 */
public class StudyImportDTO {

	@NotBlank
	@Pattern(regexp = "[A-Za-z 0-9]+")
	private String name;

	private String objective;

	@NotBlank
	private String title;

	@ApiModelProperty(value = "Study start date in YYYYMMDD format.")
	@Pattern(regexp = "[0-9]{8}")
	private String startDate;

	@ApiModelProperty(value = "Study end date in YYYYMMDD format.")
	@Pattern(regexp = "[0-9]{8}")
	private String endDate;

	@ApiModelProperty(value = "The type of study. N for nursery. T for trials.")
	@NotNull
	@Size(min = 1, max = 1)
	@Pattern(regexp = "[N|T]")
	private String studyType;

	@Pattern(regexp = "[A-Za-z 0-9]+")
	private String siteName;

	private String studyInstitute;

	@ApiModelProperty(
			value = "The identifier of the study folder to import the study into. Default value of 1 is for the virtual \"Root study folder\" which is always present.")
	private Long folderId = 1L;

	@ApiModelProperty(
			value = "Id of the user record in crop database to associate this study's data with such as the owning user of the germplasm list which is created. "
					+ "Use the /study/{cropname}/folders service to retrieve a list of folders with folders identifiers.")
	@NotNull
	private Integer userId;

	@ApiModelProperty(
			value = "The germplasm used in the study. Each germplasm specified here are assumed to be present in the database already. "
					+ "A germplasm list will be created with the germplasm specified here.")
	@Valid
	@NotEmpty
	private List<StudyGermplasm> germplasm = new ArrayList<>();

	@ApiModelProperty(value = "The traits measured in the study.")
	@Valid
	@NotEmpty
	private List<Trait> traits = new ArrayList<>();

	@ApiModelProperty(value = "The measurement values for each trait for each germplasm used in the study.")
	@Valid
	@NotEmpty
	private List<ObservationImportDTO> observations = new ArrayList<>();

	@Valid
	private EnvironmentDetails environmentDetails = new EnvironmentDetails();

	public List<Trait> getTraits() {
		return this.traits;
	}

	public void setTraits(final List<Trait> traits) {
		this.traits = traits;
	}

	public List<StudyGermplasm> getGermplasm() {
		return this.germplasm;
	}

	public void setGermplasm(final List<StudyGermplasm> germplasm) {
		this.germplasm = germplasm;
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

	public String getStudyInstitute() {
		return this.studyInstitute;
	}

	public void setStudyInstitute(final String studyInstitute) {
		this.studyInstitute = studyInstitute;
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

	public EnvironmentDetails getEnvironmentDetails() {
		return this.environmentDetails;
	}

	public void setEnvironmentDetails(final EnvironmentDetails environmentDetails) {
		this.environmentDetails = environmentDetails;
	}

	public StudyGermplasm findStudyGermplasm(final Integer gid) {
		for (final StudyGermplasm studyGermplasm : this.getGermplasm()) {
			if (studyGermplasm.getGermplasmListEntrySummary().getGid().equals(gid)) {
				return studyGermplasm;
			}
		}
		return null;
	}
}
