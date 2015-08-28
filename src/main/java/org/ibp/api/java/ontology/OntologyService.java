package org.ibp.api.java.ontology;

import java.util.List;

import org.ibp.api.domain.study.Trait;

public interface OntologyService {

	public List<Trait> getTraitGroups();

	public List<Trait> getTraitsByGroup(int groupId);
}
