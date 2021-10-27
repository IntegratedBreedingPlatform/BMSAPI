package org.ibp.api.java.germplasm;

import org.generationcp.middleware.api.germplasmlist.GermplasmListVariableRequestDto;
import org.generationcp.middleware.domain.ontology.Variable;

import java.util.List;
import java.util.Set;

public interface GermplasmListVariableService {

	void addVariableToList(Integer listId, GermplasmListVariableRequestDto germplasmListVariableRequestDto);

	void removeListVariables(Integer listId, Set<Integer> variableIds);

	List<Variable> getGermplasmListVariables(String cropName, String programUUID, Integer listId, Integer variableTypeId);

}
