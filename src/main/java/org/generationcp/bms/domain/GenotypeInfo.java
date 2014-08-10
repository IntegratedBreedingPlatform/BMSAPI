package org.generationcp.bms.domain;

import java.util.List;

public class GenotypeInfo {

	private final Integer germplasmId;
	private List<GenotypeData> genotypes;

	public GenotypeInfo(Integer germplasmId) {
		this.germplasmId = germplasmId;
	}

	public List<GenotypeData> getGenotypes() {
		return genotypes;
	}

	public void setGenotypes(List<GenotypeData> genotypes) {
		this.genotypes = genotypes;
	}

	public Integer getGermplasmId() {
		return germplasmId;
	}

}
