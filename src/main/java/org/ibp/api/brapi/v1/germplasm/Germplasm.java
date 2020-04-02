package org.ibp.api.brapi.v1.germplasm;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonView;
import org.generationcp.middleware.service.api.BrapiView;
import org.pojomatic.Pojomatic;
import org.pojomatic.annotations.AutoProperty;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
@AutoProperty
public class Germplasm {

	private String germplasmDbId;

	private String defaultDisplayName;

	private String accessionNumber;

	private String germplasmName;

	private String germplasmPUI;

	private String pedigree;

	@JsonView(BrapiView.BrapiV1_2.class)
	private String germplasmSeedSource;

	private List<String> synonyms = new ArrayList<>();

	private String commonCropName;

	private String instituteCode;

	private String instituteName;

	private Integer biologicalStatusOfAccessionCode;

	private String countryOfOriginCode;

	private List<String> typeOfGermplasmStorageCode = new ArrayList<>();

	@JsonView(BrapiView.BrapiV1_2.class)
	private String genus;

	@JsonView(BrapiView.BrapiV1_2.class)
	private String species;

	private List<Taxon> taxonIds = new ArrayList<>();

	private String speciesAuthority;

	private String subtaxa;

	private String subtaxaAuthority;

	private List<Donor> donors = new ArrayList<>();

	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
	private Date acquisitionDate;

	@JsonView(BrapiView.BrapiV1_3.class)
	private String breedingMethodDbId;

	@JsonView(BrapiView.BrapiV1_3.class)
	private String germplasmGenus;

	@JsonView(BrapiView.BrapiV1_3.class)
	private String germplasmSpecies;

	@JsonView(BrapiView.BrapiV1_3.class)
	private String seedSource;

	@JsonView(BrapiView.BrapiV1_3.class)
	private final static String documentationURL = null;

	@JsonView(BrapiView.BrapiV1_3.class)
	private String entryNumber;

	@JsonView(BrapiView.BrapiV1_3.class)
	private String donorAccessionNumber;

	@JsonView(BrapiView.BrapiV1_3.class)
	private String donorInstituteCode;

	@JsonView(BrapiView.BrapiV1_3.class)
	private String sourceName;

	public Germplasm() {
	}

	public String getBreedingMethodDbId() {
		return this.breedingMethodDbId;
	}

	public void setBreedingMethodDbId(final String breedingMethodDbId) {
		this.breedingMethodDbId = breedingMethodDbId;
	}

	public String getGermplasmGenus() {
		return this.germplasmGenus;
	}

	public void setGermplasmGenus(final String germplasmGenus) {
		this.germplasmGenus = germplasmGenus;
	}

	public String getGermplasmSpecies() {
		return this.germplasmSpecies;
	}

	public void setGermplasmSpecies(final String germplasmSpecies) {
		this.germplasmSpecies = germplasmSpecies;
	}

	public String getSeedSource() {
		return this.seedSource;
	}

	public void setSeedSource(final String seedSource) {
		this.seedSource = seedSource;
	}

	public String getDocumentationURL() {
		return this.documentationURL;
	}

	public String getGermplasmDbId() {
		return this.germplasmDbId;
	}

	public void setGermplasmDbId(final String germplasmDbId) {
		this.germplasmDbId = germplasmDbId;
	}

	public String getDefaultDisplayName() {
		return this.defaultDisplayName;
	}

	public void setDefaultDisplayName(final String defaultDisplayName) {
		this.defaultDisplayName = defaultDisplayName;
	}

	public String getAccessionNumber() {
		return this.accessionNumber;
	}

	public void setAccessionNumber(final String accessionNumber) {
		this.accessionNumber = accessionNumber;
	}

	public String getGermplasmName() {
		return this.germplasmName;
	}

	public void setGermplasmName(final String germplasmName) {
		this.germplasmName = germplasmName;
	}

	public String getGermplasmPUI() {
		return this.germplasmPUI;
	}

	public void setGermplasmPUI(final String germplasmPUI) {
		this.germplasmPUI = germplasmPUI;
	}

	public String getPedigree() {
		return this.pedigree;
	}

	public void setPedigree(final String pedigree) {
		this.pedigree = pedigree;
	}

	public String getGermplasmSeedSource() {
		return this.germplasmSeedSource;
	}

	public void setGermplasmSeedSource(final String germplasmSeedSource) {
		this.germplasmSeedSource = germplasmSeedSource;
	}

	public List<String> getSynonyms() {
		return this.synonyms;
	}

	public void setSynonyms(final List<String> synonyms) {
		this.synonyms = synonyms;
	}

	public String getCommonCropName() {
		return this.commonCropName;
	}

	public void setCommonCropName(final String commonCropName) {
		this.commonCropName = commonCropName;
	}

	public String getInstituteCode() {
		return this.instituteCode;
	}

	public void setInstituteCode(final String instituteCode) {
		this.instituteCode = instituteCode;
	}

	public String getInstituteName() {
		return this.instituteName;
	}

	public void setInstituteName(final String instituteName) {
		this.instituteName = instituteName;
	}

	public Integer getBiologicalStatusOfAccessionCode() {
		return this.biologicalStatusOfAccessionCode;
	}

	public void setBiologicalStatusOfAccessionCode(final Integer biologicalStatusOfAccessionCode) {
		this.biologicalStatusOfAccessionCode = biologicalStatusOfAccessionCode;
	}

	public String getCountryOfOriginCode() {
		return this.countryOfOriginCode;
	}

	public void setCountryOfOriginCode(final String countryOfOriginCode) {
		this.countryOfOriginCode = countryOfOriginCode;
	}

	public List<String> getTypeOfGermplasmStorageCode() {
		return this.typeOfGermplasmStorageCode;
	}

	public void setTypeOfGermplasmStorageCode(final List<String> typeOfGermplasmStorageCode) {
		this.typeOfGermplasmStorageCode = typeOfGermplasmStorageCode;
	}

	public String getGenus() {
		return this.genus;
	}

	public void setGenus(final String genus) {
		this.genus = genus;
	}

	public String getSpecies() {
		return this.species;
	}

	public void setSpecies(final String species) {
		this.species = species;
	}

	public List<Taxon> getTaxonIds() {
		return this.taxonIds;
	}

	public void setTaxonIds(final List<Taxon> taxonIds) {
		this.taxonIds = taxonIds;
	}

	public String getSpeciesAuthority() {
		return this.speciesAuthority;
	}

	public void setSpeciesAuthority(final String speciesAuthority) {
		this.speciesAuthority = speciesAuthority;
	}

	public String getSubtaxa() {
		return this.subtaxa;
	}

	public void setSubtaxa(final String subtaxa) {
		this.subtaxa = subtaxa;
	}

	public String getSubtaxaAuthority() {
		return this.subtaxaAuthority;
	}

	public void setSubtaxaAuthority(final String subtaxaAuthority) {
		this.subtaxaAuthority = subtaxaAuthority;
	}

	public List<Donor> getDonors() {
		return this.donors;
	}

	public void setDonors(final List<Donor> donors) {
		this.donors = donors;
	}

	public Date getAcquisitionDate() {
		return this.acquisitionDate;
	}

	public void setAcquisitionDate(final Date acquisitionDate) {
		this.acquisitionDate = acquisitionDate;
	}

	public String getEntryNumber() {
		return this.entryNumber;
	}

	public void setEntryNumber(final String entryNumber) {
		this.entryNumber = entryNumber;
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
