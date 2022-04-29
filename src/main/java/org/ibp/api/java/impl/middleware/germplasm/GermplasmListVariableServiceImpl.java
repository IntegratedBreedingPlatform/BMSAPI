package org.ibp.api.java.impl.middleware.germplasm;

import com.google.common.collect.Lists;
import org.apache.commons.lang3.StringUtils;
import org.generationcp.middleware.api.germplasmlist.GermplasmListVariableRequestDto;
import org.generationcp.middleware.api.program.ProgramDTO;
import org.generationcp.middleware.domain.ontology.Variable;
import org.generationcp.middleware.domain.ontology.VariableType;
import org.generationcp.middleware.pojos.GermplasmList;
import org.ibp.api.exception.ApiRequestValidationException;
import org.ibp.api.exception.ResourceNotFoundException;
import org.ibp.api.java.germplasm.GermplasmListVariableService;
import org.ibp.api.java.impl.middleware.common.validator.BaseValidator;
import org.ibp.api.java.impl.middleware.common.validator.GermplasmListValidator;
import org.ibp.api.java.impl.middleware.common.validator.ProgramValidator;
import org.ibp.api.java.impl.middleware.germplasm.validator.GermplasmListVariableValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindingResult;
import org.springframework.validation.MapBindingResult;

import java.util.HashMap;
import java.util.List;
import java.util.Set;

@Service
@Transactional
public class GermplasmListVariableServiceImpl implements GermplasmListVariableService {

	private static final List<Integer> VALID_TYPES = Lists.newArrayList(VariableType.ENTRY_DETAIL.getId());

	@Autowired
	public ProgramValidator programValidator;

	@Autowired
	public org.generationcp.middleware.api.germplasmlist.GermplasmListService germplasmListService;

	@Autowired
	private GermplasmListVariableValidator germplasmListVariableValidator;

	@Autowired
	private GermplasmListValidator germplasmListValidator;

	private BindingResult errors;

	@Override
	public void addVariableToList(final Integer listId,
		final GermplasmListVariableRequestDto germplasmListVariableRequestDto) {
		this.errors = new MapBindingResult(new HashMap<>(), GermplasmListVariableRequestDto.class.getName());
		final GermplasmList germplasmList = germplasmListValidator.validateGermplasmList(listId);
		germplasmListValidator.validateMaxVariablesAllowed(listId);
		germplasmListValidator.validateListIsNotAFolder(germplasmList);
		germplasmListValidator.validateListIsUnlocked(germplasmList);
		germplasmListVariableValidator.validateAddVariableToList(listId, germplasmListVariableRequestDto);
		germplasmListService.addVariableToList(listId, germplasmListVariableRequestDto);

	}

	@Override
	public void removeListVariables(final Integer listId, final Set<Integer> variableIds) {
		this.errors = new MapBindingResult(new HashMap<>(), String.class.getName());
		final GermplasmList germplasmList = germplasmListValidator.validateGermplasmList(listId);
		germplasmListValidator.validateListIsNotAFolder(germplasmList);
		germplasmListValidator.validateListIsUnlocked(germplasmList);
		BaseValidator.checkNotEmpty(variableIds, "germplasm.list.variable.ids.can.not.be.empty");
		this.germplasmListVariableValidator.validateAllVariableIdsAreVariables(variableIds);
		this.germplasmListVariableValidator.validateAllVariableIdsAreAssociatedToList(listId, Lists.newArrayList(variableIds));
		this.germplasmListService.removeListVariables(listId, variableIds);
	}

	@Override
	public List<Variable> getGermplasmListVariables(final String cropName, final String programUUID, final Integer listId,
		final Integer variableTypeId) {
		this.errors = new MapBindingResult(new HashMap<>(), String.class.getName());
		this.validateProgram(cropName, programUUID);
		germplasmListValidator.validateGermplasmList(listId);
		if (variableTypeId != null) {
			if (!VariableType.ids().contains(variableTypeId)) {
				this.errors.reject("variable.type.does.not.exist", "");
				throw new ApiRequestValidationException(this.errors.getAllErrors());
			}
		}
		return germplasmListService.getGermplasmListVariables(programUUID, listId, variableTypeId);
	}

	private void validateProgram(final String cropName, final String programUUID) {
		if (!StringUtils.isEmpty(programUUID)) {
			this.programValidator.validate(new ProgramDTO(cropName, programUUID), this.errors);
			if (this.errors.hasErrors()) {
				throw new ResourceNotFoundException(this.errors.getAllErrors().get(0));
			}
		}
	}

}
