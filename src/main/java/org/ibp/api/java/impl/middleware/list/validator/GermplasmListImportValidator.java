package org.ibp.api.java.impl.middleware.list.validator;

import org.generationcp.middleware.api.brapi.v2.list.GermplasmListImportRequestDTO;
import org.springframework.stereotype.Component;
import org.springframework.validation.BindingResult;

import java.util.List;

@Component
public class GermplasmListImportValidator {
	protected BindingResult errors;

	public BindingResult pruneListsInvalidForImport(final List<GermplasmListImportRequestDTO> importRequestDTOS) {

	}
}
