
package org.ibp.api.domain.study;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.ibp.api.domain.ontology.TermSummary;

public class StudyDetails extends StudySummary {

	private final Set<StudyAttribute> generalInfo = new HashSet<>();
	private final List<Environment> environments = new ArrayList<>();
	private final Set<TermSummary> traits = new HashSet<>();
	private final Set<DatasetSummary> datasets = new HashSet<>();

	public Set<StudyAttribute> getGeneralInfo() {
		return generalInfo;
	}

	public void addGeneralInfo(StudyAttribute attr) {
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

	public Set<TermSummary> getTraits() {
		return traits;
	}

	public void addTrait(TermSummary trait) {
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
