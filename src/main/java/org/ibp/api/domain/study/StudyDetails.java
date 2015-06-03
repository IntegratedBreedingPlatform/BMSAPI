
package org.ibp.api.domain.study;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class StudyDetails extends StudySummary {

	private final Set<Attribute> generalInfo = new HashSet<>();
	private final List<Environment> environments = new ArrayList<>();
	private final Set<Trait> traits = new HashSet<>();
	private final Set<DatasetSummary> datasets = new HashSet<>();

	public Set<Attribute> getGeneralInfo() {
		return generalInfo;
	}

	public void addGeneralInfo(Attribute attr) {
		if (attr != null) {
			this.generalInfo.add(attr);
		}
	}

	public List<Environment> getEnvironments() {
		return environments;
	}

	public void addEnvironment(Environment env) {
		if (env != null) {
			this.environments.add(env);
		}
	}

	public Set<Trait> getTraits() {
		return traits;
	}

	public void addTrait(Trait trait) {
		if (trait != null) {
			this.traits.add(trait);
		}
	}

	public Set<DatasetSummary> getDatasets() {
		return datasets;
	}

	public void addDataSet(DatasetSummary dataset) {
		if (dataset != null) {
			this.datasets.add(dataset);
		}
	}

}
