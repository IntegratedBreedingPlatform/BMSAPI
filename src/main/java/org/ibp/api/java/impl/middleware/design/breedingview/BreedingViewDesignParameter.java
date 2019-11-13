package org.ibp.api.java.impl.middleware.design.breedingview;

public enum BreedingViewDesignParameter {
	NCLATIN("nclatin"),
	NRLATIN("nrlatin"),
	REPLATINGROUPS("replatingroups"),
	NCOLUMNS("ncolumns"),
	NROWS("nrows"),
	NBLATIN("nblatin"),

	INITIAL_TREATMENT_NUMBER("initialtreatnum"),
	NREPLICATES("nreplicates"),
	NTREATMENTS("ntreatments"),
	BLOCKSIZE("blocksize"),
	TIMELIMIT("timelimit"),
	LEVELS("levels"),
	TREATMENTFACTORS("treatmentfactors"),
	INITIAL_PLOT_NUMBER("initialplotnum"),

	NBLOCKS("nblocks"),
	OUTPUTFILE("outputfile"),
	SEED("seed"),
	NCONTROLS("ncontrols"),
	NUMBER_TRIALS("numbertrials"),
	NREPEATS("nrepeats");

	private String parameterName;

	BreedingViewDesignParameter(final String parameterName) {
		this.parameterName = parameterName;
	}

	public String getParameterName() {
		return parameterName;
	}
}
