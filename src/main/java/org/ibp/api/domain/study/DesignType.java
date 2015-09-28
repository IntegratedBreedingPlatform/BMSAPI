
package org.ibp.api.domain.study;

public enum DesignType {

	UNREP(10055, "UNREP", "Unreplicated demonstration"), //
	CRD(10100, "CRD", "Completely randomized design"), //
	RCBD(10110, "RCBD", "Randomized complete block design"), //
	ALPHA(10120, "Alpha", "Alpha lattice"), //
	RIBD(10130, "RIBD", "Resolvable incomplete block design"), //
	NRIBD(10140, "NRIBD", "Non resolvable incomplete block design"), //
	RRCD(10145, "RRCD", "Resolvable row-column design"), //
	NRRCD(10150, "NRRCD", "Non resolvable row-column design"), //
	Augmented(10160, "Augmented", "Augmented design"), //
	SP(10165, "SP", "Split-plot design"), //
	RIBDL(10166, "RIBDL", "Resolvable Incomplete Block Design (Latinized)"), //
	RRCDL(10167, "RRCDL", "Resolvable Row-and-Column Design (Latinized)");

	private final Integer id;
	private final String name;
	private final String description;

	DesignType(final Integer id, final String name, final String description) {
		this.id = id;
		this.name = name;
		this.description = description;
	}

	public Integer getId() {
		return this.id;
	}

	public String getName() {
		return this.name;
	}

	public String getDescription() {
		return this.description;
	}

}
