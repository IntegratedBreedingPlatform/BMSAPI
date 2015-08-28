package org.ibp.api.domain.study;

import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.Pattern;

import org.hibernate.validator.constraints.NotBlank;
import org.hibernate.validator.constraints.NotEmpty;
import org.ibp.api.rest.study.StudyResource;

/**
 * Front end bean for receiving data in saveStudy() method in {@link StudyResource} 
 * @author j-alberto
 *
 */
public class StudyWorkbook {

	@NotBlank
	@Pattern(regexp="[A-Za-z 0-9]+")
	private String name;

	@Pattern(regexp="[A-Za-z 0-9]+")
	private String objective;

	@Pattern(regexp="[0-9]{8}")
	private String startDate;
	
	@Pattern(regexp="[0-9]{8}")
	private String endDate;
	private String title;
	
	@NotEmpty
	@Pattern(regexp="[N|T]")
	private String studyType;
	
	@Pattern(regexp="[A-Za-z 0-9]+")
	private String siteName;
	
	@NotEmpty
	@Valid
	private List<StudyGermplasm> germplasms;

	@NotEmpty
	@Valid
	private List<Trait> traits;

	private String[][] traitValues;

	public List<Trait> getTraits() {
		return traits;
	}
	public void setTraits(List<Trait> traits) {
		this.traits = traits;
	}
	public String[][] getTraitValues() {
		return traitValues;
	}
	public void setTraitValues(String[][] traitValues) {
		this.traitValues = traitValues;
	}

	public List<StudyGermplasm> getGermplasms() {
		return germplasms;
	}
	public void setGermplasms(List<StudyGermplasm> germplasms) {
		this.germplasms = germplasms;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getObjective() {
		return objective;
	}
	public void setObjective(String objective) {
		this.objective = objective;
	}
	public String getStartDate() {
		return startDate;
	}
	public void setStartDate(String startDate) {
		this.startDate = startDate;
	}
	public String getEndDate() {
		return endDate;
	}
	public void setEndDate(String endDate) {
		this.endDate = endDate;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getStudyType() {
		return studyType;
	}
	public void setStudyType(String studyType) {
		this.studyType = studyType;
	}
	public String getSiteName() {
		return siteName;
	}
	public void setSiteName(String siteName) {
		this.siteName = siteName;
	}
}
