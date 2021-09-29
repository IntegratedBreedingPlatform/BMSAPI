package org.ibp.api.java.impl.middleware.germplasm.validator;

import com.google.common.collect.Lists;
import org.generationcp.middleware.api.germplasmlist.GermplasmListService;
import org.generationcp.middleware.api.germplasmlist.GermplasmListVariableRequestDto;
import org.generationcp.middleware.domain.ontology.Variable;
import org.generationcp.middleware.domain.ontology.VariableType;
import org.generationcp.middleware.manager.ontology.api.OntologyVariableDataManager;
import org.generationcp.middleware.manager.ontology.daoElements.VariableFilter;
import org.ibp.api.exception.ApiRequestValidationException;
import org.ibp.api.java.impl.middleware.common.validator.BaseValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.validation.BindingResult;
import org.springframework.validation.MapBindingResult;

import java.util.HashMap;
import java.util.List;

@Component
public class GermplasmListVariableValidator {

	@Autowired
	private OntologyVariableDataManager ontologyVariableDataManager;

	@Autowired
	private GermplasmListService germplasmListService;

	private BindingResult errors;

	private static final List<Integer> VALID_TYPES = Lists.newArrayList(VariableType.ENTRY_DETAIL.getId());

	public void validateAddVariableToList(final Integer listId, final GermplasmListVariableRequestDto germplasmListVariableRequestDto) {
		this.errors = new MapBindingResult(new HashMap<>(), GermplasmListVariableRequestDto.class.getName());
		BaseValidator.checkNotNull(germplasmListVariableRequestDto, "param.null", new String[] {"request body"});
		BaseValidator.checkNotNull(germplasmListVariableRequestDto.getVariableId(), "param.null", new String[] {"variableId"});
		BaseValidator.checkNotNull(germplasmListVariableRequestDto.getVariableTypeId(), "param.null", new String[] {"variableTypeId"});

		if (!VALID_TYPES.contains(germplasmListVariableRequestDto.getVariableTypeId())) {
			this.errors.reject("germplasm.list.variable.type.invalid", "");
			throw new ApiRequestValidationException(this.errors.getAllErrors());
		}

		final VariableFilter variableFilter = new VariableFilter();
		variableFilter.addVariableId(germplasmListVariableRequestDto.getVariableId());
		final List<Variable> variableDetails = ontologyVariableDataManager.getWithFilter(variableFilter);
		if (CollectionUtils.isEmpty(variableDetails)) {
			this.errors.reject("germplasm.list.variable.does.not.exist", "");
			throw new ApiRequestValidationException(this.errors.getAllErrors());
		}

		final Variable variable = variableDetails.get(0);
		if (!variable.getVariableTypes().contains(VariableType.ENTRY_DETAIL)) {
			this.errors.reject("germplasm.list.variable.type.incompatible", "");
			throw new ApiRequestValidationException(this.errors.getAllErrors());
		}

		this.validateVariableIsNotAssociatedToList(listId, germplasmListVariableRequestDto.getVariableId());
	}

	public void validateVariableIsNotAssociatedToList(final Integer listId, final Integer variableId) {
		this.errors = new MapBindingResult(new HashMap<>(), Integer.class.getName());
		final List<Integer> variableIds = germplasmListService.getListOntologyVariables(listId, null);
		if (variableIds.contains(variableId)) {
			this.errors.reject("germplasm.list.variable.already.associated.to.list", "");
			throw new ApiRequestValidationException(this.errors.getAllErrors());
		}
	}

	public void validateVariableIsAssociatedToList(final Integer listId, final Integer variableId) {
		this.errors = new MapBindingResult(new HashMap<>(), Integer.class.getName());
		final List<Integer> variableIds = germplasmListService.getListOntologyVariables(listId, VALID_TYPES);
		if (!variableIds.contains(variableId)) {
			this.errors.reject("germplasm.list.variable.not.associated", "");
			throw new ApiRequestValidationException(this.errors.getAllErrors());
		}
	}
}
