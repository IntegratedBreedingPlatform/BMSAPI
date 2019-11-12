package org.ibp.api.java.impl.middleware.design.breedingview;

public enum BreedingViewVariableParameter {

	BLOCK("blockfactor"),
	REP("replicatefactor"),
	ENTRY("treatmentfactor"),
	PLOT("plotfactor"),
	COLUMN("columnfactor"),
	ROW("rowfactor");

	private String parameterName;

	BreedingViewVariableParameter(final String parameterName) {
		this.parameterName = parameterName;
	}

	public String getParameterName() {
		return parameterName;
	}
}