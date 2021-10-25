package org.ibp.api.brapi.v1.germplasm;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonView;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.generationcp.middleware.api.brapi.v2.germplasm.ExternalReferenceDTO;
import org.generationcp.middleware.api.brapi.v2.germplasm.Synonym;
import org.generationcp.middleware.service.api.BrapiView;
import org.generationcp.middleware.util.serializer.SynonymPropertySerializer;
import org.pojomatic.Pojomatic;
import org.pojomatic.annotations.AutoProperty;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

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

	@JsonSerialize(using = SynonymPropertySerializer.class)
	private List<Synonym> synonyms = new ArrayList<>();

	private String commonCropName;

	private String instituteCode;

	private String instituteName;

	private Integer biologicalStatusOfAccessionCode;

	@JsonView(BrapiView.BrapiV2.class)
	private String biologicalStatusOfAccessionDescription;

	@JsonView(BrapiView.BrapiV2.class)
	private String collection;

	@JsonView(BrapiView.BrapiV2.class)
	private GermplasmOrigin germplasmOrigin;

	@JsonView(BrapiView.BrapiV2.class)
	private String germplasmPreProcessing;

	private String countryOfOriginCode;

	@JsonView({BrapiView.BrapiV1_2.class, BrapiView.BrapiV1_3.class})
	private List<String> typeOfGermplasmStorageCode = new ArrayList<>();

	@JsonView({BrapiView.BrapiV1_2.class, BrapiView.BrapiV2.class})
	private String genus;

	@JsonView({BrapiView.BrapiV1_2.class, BrapiView.BrapiV2.class})
	private String species;

	private List<Taxon> taxonIds = new ArrayList<>();

	private String speciesAuthority;

	private String subtaxa;

	private String subtaxaAuthority;

	private List<Donor> donors = new ArrayList<>();

	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
	private Date acquisitionDate;

	@JsonView({BrapiView.BrapiV1_3.class, BrapiView.BrapiV2.class})
	private String breedingMethodDbId;

	@JsonView(BrapiView.BrapiV1_3.class)
	private String germplasmGenus;

	@JsonView(BrapiView.BrapiV1_3.class)
	private String germplasmSpecies;

	@JsonView({BrapiView.BrapiV1_3.class, BrapiView.BrapiV2.class})
	private String seedSource;

	@JsonView(BrapiView.BrapiV2.class)
	private String seedSourceDescription;

	@JsonView({BrapiView.BrapiV1_3.class, BrapiView.BrapiV2.class})
	private String documentationURL;

	@JsonView(BrapiView.BrapiV1_3.class)
	private String entryNumber;

	@JsonView(BrapiView.BrapiV1_3.class)
	private String donorAccessionNumber;

	@JsonView(BrapiView.BrapiV1_3.class)
	private String donorInstituteCode;

	@JsonView(BrapiView.BrapiV1_3.class)
	private String sourceName;

	@JsonView(BrapiView.BrapiV2.class)
	private Map<String, String> additionalInfo;

	@JsonView(BrapiView.BrapiV2.class)
	private List<ExternalReferenceDTO> externalReferences;

	@JsonView(BrapiView.BrapiV2.class)
	private List<String> storageTypes = new ArrayList<>();

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

	public void setDocumentationURL(final String documentationURL) {
		this.documentationURL = documentationURL;
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

	public List<Synonym> getSynonyms() {
		return this.synonyms;
	}

	public void setSynonyms(final List<Synonym> synonyms) {
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

	public String getDonorAccessionNumber() {
		return this.donorAccessionNumber;
	}

	public void setDonorAccessionNumber(final String donorAccessionNumber) {
		this.donorAccessionNumber = donorAccessionNumber;
	}

	public String getDonorInstituteCode() {
		return this.donorInstituteCode;
	}

	public void setDonorInstituteCode(final String donorInstituteCode) {
		this.donorInstituteCode = donorInstituteCode;
	}

	public String getSourceName() {
		return this.sourceName;
	}

	public void setSourceName(final String sourceName) {
		this.sourceName = sourceName;
	}

	public String getBiologicalStatusOfAccessionDescription() {
		return this.biologicalStatusOfAccessionDescription;
	}

	public void setBiologicalStatusOfAccessionDescription(final String biologicalStatusOfAccessionDescription) {
		this.biologicalStatusOfAccessionDescription = biologicalStatusOfAccessionDescription;
	}

	public String getCollection() {
		return this.collection;
	}

	public void setCollection(final String collection) {
		this.collection = collection;
	}

	public GermplasmOrigin getGermplasmOrigin() {
		return this.germplasmOrigin;
	}

	public void setGermplasmOrigin(final GermplasmOrigin germplasmOrigin) {
		this.germplasmOrigin = germplasmOrigin;
	}

	public String getGermplasmPreProcessing() {
		return this.germplasmPreProcessing;
	}

	public void setGermplasmPreProcessing(final String germplasmPreProcessing) {
		this.germplasmPreProcessing = germplasmPreProcessing;
	}

	public String getSeedSourceDescription() {
		return this.seedSourceDescription;
	}

	public void setSeedSourceDescription(final String seedSourceDescription) {
		this.seedSourceDescription = seedSourceDescription;
	}

	public Map<String, String> getAdditionalInfo() {
		return this.additionalInfo;
	}

	public void setAdditionalInfo(final Map<String, String> additionalInfo) {
		this.additionalInfo = additionalInfo;
	}

	public List<ExternalReferenceDTO> getExternalReferences() {
		return this.externalReferences;
	}

	public void setExternalReferences(final List<ExternalReferenceDTO> externalReferences) {
		this.externalReferences = externalReferences;
	}

	public List<String> getStorageTypes() {
		return this.storageTypes;
	}

	public void setStorageTypes(final List<String> storageTypes) {
		this.storageTypes = storageTypes;
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
