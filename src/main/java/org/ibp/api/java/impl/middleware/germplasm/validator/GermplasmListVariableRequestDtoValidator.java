package org.ibp.api.java.impl.middleware.germplasm.validator;

import org.generationcp.middleware.api.germplasmlist.GermplasmListVariableRequestDto;
import org.generationcp.middleware.domain.ontology.VariableType;
import org.springframework.stereotype.Component;
import org.springframework.validation.BindingResult;
import org.springframework.validation.MapBindingResult;

import java.util.HashMap;

@Component
public class GermplasmListVariableRequestDtoValidator {

	private BindingResult errors;

	private static final Integer[] VALID_TYPES = {VariableType.ENTRY_DETAIL.getId()};

	public void validate(final Integer listId, final GermplasmListVariableRequestDto germplasmListVariableRequestDto) {
		this.errors = new MapBindingResult(new HashMap<>(), GermplasmListVariableRequestDto.class.getName());

	}

}
