package org.ibp.api.java.ontology;

import org.generationcp.middleware.domain.ontology.FormulaDto;

public interface FormulaService {

	FormulaDto save(final FormulaDto formulaDto);

	void delete(final Integer formulaId);

	FormulaDto update(final FormulaDto formulaDto);

}
