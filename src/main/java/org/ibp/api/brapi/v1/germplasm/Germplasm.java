package org.ibp.api.brapi.v1.germplasm;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.pojomatic.Pojomatic;

@JsonInclude(JsonInclude.Include.ALWAYS)
@JsonPropertyOrder({"germplasmDbId", "defaultDisplayName", "accessionNumber", "germplasmName", "germplasmPUI", "pedigree",
		"germplasmSeedSource", "synonyms", "commonCropName", "instituteCode", "instituteName", "biologicalStatusOfAccessionCode",
		"countryOfOriginCode", "typeOfGermplasmStorageCode", "genus", "species", "taxonIds", "speciesAuthority", "subtaxa",
		"subtaxaAuthority", "donors", "acquisitionDate"})
public class Germplasm {

	private String germplasmDbId;

	private String defaultDisplayName;

	private String accessionNumber;

	private String germplasmName;

	private String germplasmPUI;

	private String pedigree;

	private String germplasmSeedSource;

	private List<String> synonyms = new ArrayList<>();

	private String commonCropName;

	private String instituteCode;

	private String instituteName;

	private Integer biologicalStatusOfAccessionCode;

	private String countryOfOriginCode;

	private List<String> typeOfGermplasmStorageCode = new ArrayList<>();

	private String genus;

	private String species;

	private List<Taxon> taxonIds = new ArrayList<>();

	private String speciesAuthority;

	private String subtaxa;

	private String subtaxaAuthority;

	private List<Donor> donors = new ArrayList<>();

	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
	private Date acquisitionDate;

	public Germplasm() {
	}

	public String getGermplasmDbId() {
		return germplasmDbId;
	}

	public void setGermplasmDbId(final String germplasmDbId) {
		this.germplasmDbId = germplasmDbId;
	}

	public String getDefaultDisplayName() {
		return defaultDisplayName;
	}

	public void setDefaultDisplayName(final String defaultDisplayName) {
		this.defaultDisplayName = defaultDisplayName;
	}

	public String getAccessionNumber() {
		return accessionNumber;
	}

	public void setAccessionNumber(final String accessionNumber) {
		this.accessionNumber = accessionNumber;
	}

	public String getGermplasmName() {
		return germplasmName;
	}

	public void setGermplasmName(final String germplasmName) {
		this.germplasmName = germplasmName;
	}

	public String getGermplasmPUI() {
		return germplasmPUI;
	}

	public void setGermplasmPUI(final String germplasmPUI) {
		this.germplasmPUI = germplasmPUI;
	}

	public String getPedigree() {
		return pedigree;
	}

	public void setPedigree(final String pedigree) {
		this.pedigree = pedigree;
	}

	public String getGermplasmSeedSource() {
		return germplasmSeedSource;
	}

	public void setGermplasmSeedSource(final String germplasmSeedSource) {
		this.germplasmSeedSource = germplasmSeedSource;
	}

	public List<String> getSynonyms() {
		return synonyms;
	}

	public void setSynonyms(final List<String> synonyms) {
		this.synonyms = synonyms;
	}

	public String getCommonCropName() {
		return commonCropName;
	}

	public void setCommonCropName(final String commonCropName) {
		this.commonCropName = commonCropName;
	}

	public String getInstituteCode() {
		return instituteCode;
	}

	public void setInstituteCode(final String instituteCode) {
		this.instituteCode = instituteCode;
	}

	public String getInstituteName() {
		return instituteName;
	}

	public void setInstituteName(final String instituteName) {
		this.instituteName = instituteName;
	}

	public Integer getBiologicalStatusOfAccessionCode() {
		return biologicalStatusOfAccessionCode;
	}

	public void setBiologicalStatusOfAccessionCode(final Integer biologicalStatusOfAccessionCode) {
		this.biologicalStatusOfAccessionCode = biologicalStatusOfAccessionCode;
	}

	public String getCountryOfOriginCode() {
		return countryOfOriginCode;
	}

	public void setCountryOfOriginCode(final String countryOfOriginCode) {
		this.countryOfOriginCode = countryOfOriginCode;
	}

	public List<String> getTypeOfGermplasmStorageCode() {
		return typeOfGermplasmStorageCode;
	}

	public void setTypeOfGermplasmStorageCode(final List<String> typeOfGermplasmStorageCode) {
		this.typeOfGermplasmStorageCode = typeOfGermplasmStorageCode;
	}

	public String getGenus() {
		return genus;
	}

	public void setGenus(final String genus) {
		this.genus = genus;
	}

	public String getSpecies() {
		return species;
	}

	public void setSpecies(final String species) {
		this.species = species;
	}

	public List<Taxon> getTaxonIds() {
		return taxonIds;
	}

	public void setTaxonIds(final List<Taxon> taxonIds) {
		this.taxonIds = taxonIds;
	}

	public String getSpeciesAuthority() {
		return speciesAuthority;
	}

	public void setSpeciesAuthority(final String speciesAuthority) {
		this.speciesAuthority = speciesAuthority;
	}

	public String getSubtaxa() {
		return subtaxa;
	}

	public void setSubtaxa(final String subtaxa) {
		this.subtaxa = subtaxa;
	}

	public String getSubtaxaAuthority() {
		return subtaxaAuthority;
	}

	public void setSubtaxaAuthority(final String subtaxaAuthority) {
		this.subtaxaAuthority = subtaxaAuthority;
	}

	public List<Donor> getDonors() {
		return donors;
	}

	public void setDonors(final List<Donor> donors) {
		this.donors = donors;
	}

	public Date getAcquisitionDate() {
		return acquisitionDate;
	}

	public void setAcquisitionDate(final Date acquisitionDate) {
		this.acquisitionDate = acquisitionDate;
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
