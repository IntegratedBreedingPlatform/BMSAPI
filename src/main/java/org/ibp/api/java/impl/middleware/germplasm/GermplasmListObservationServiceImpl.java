package org.ibp.api.java.impl.middleware.germplasm;

import org.generationcp.middleware.api.germplasmlist.GermplasmListDataDto;
import org.generationcp.middleware.api.germplasmlist.GermplasmListObservationDto;
import org.generationcp.middleware.api.germplasmlist.GermplasmListObservationRequestDto;
import org.generationcp.middleware.api.germplasmlist.GermplasmListService;
import org.generationcp.middleware.domain.ontology.Variable;
import org.generationcp.middleware.manager.ontology.api.OntologyVariableDataManager;
import org.generationcp.middleware.pojos.GermplasmList;
import org.generationcp.middleware.util.VariableValueUtil;
import org.ibp.api.exception.ApiRequestValidationException;
import org.ibp.api.java.germplasm.GermplasmListObservationService;
import org.ibp.api.java.impl.middleware.common.validator.BaseValidator;
import org.ibp.api.java.impl.middleware.germplasm.validator.GermplasmListValidator;
import org.ibp.api.java.impl.middleware.germplasm.validator.GermplasmListVariableValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindingResult;
import org.springframework.validation.MapBindingResult;

import java.util.HashMap;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class GermplasmListObservationServiceImpl implements GermplasmListObservationService {

	private BindingResult errors;

	private static final Integer VALUE_MAX_LENGTH = 255;

	@Autowired
	private GermplasmListValidator germplasmListValidator;

	@Autowired
	private GermplasmListService germplasmListService;

	@Autowired
	private GermplasmListVariableValidator germplasmListVariableValidator;

	@Autowired
	private OntologyVariableDataManager ontologyVariableDataManager;

	@Override
	public Integer create(final Integer listId, final GermplasmListObservationRequestDto germplasmListObservationRequestDto) {
		this.errors = new MapBindingResult(new HashMap<>(), GermplasmListObservationRequestDto.class.getName());
		BaseValidator.checkNotNull(listId, "param.null", new String[] {"listId"});
		BaseValidator.checkNotNull(germplasmListObservationRequestDto, "param.null", new String[] {"request body"});
		BaseValidator.checkNotNull(germplasmListObservationRequestDto.getVariableId(), "param.null", new String[] {"variableId"});
		BaseValidator.checkNotNull(germplasmListObservationRequestDto.getListDataId(), "param.null", new String[] {"listDataId"});
		BaseValidator.checkNotNull(germplasmListObservationRequestDto.getValue(), "param.null", new String[] {"value"});

		final GermplasmList germplasmList = this.germplasmListValidator.validateGermplasmListExists(listId);
		this.germplasmListValidator.validateListIsNotAFolder(germplasmList);
		this.germplasmListValidator.validateListIsUnlocked(germplasmList);

		final GermplasmListDataDto germplasmListDataDto = this.validateListDataExists(germplasmListObservationRequestDto.getListDataId());
		this.validateListDataBelongsToTheList(listId, germplasmListDataDto);

		final Variable variable = this.validateVariableIdIsAVariable(germplasmListObservationRequestDto.getVariableId());

		this.germplasmListVariableValidator.validateVariableIsAssociatedToList(listId, germplasmListObservationRequestDto.getVariableId());

		this.validateValue(germplasmListObservationRequestDto.getValue());
		this.validateVariableDataTypeValue(variable, germplasmListObservationRequestDto.getValue());
		germplasmListObservationRequestDto
			.setcValueId(VariableValueUtil.resolveCategoricalValueId(variable, germplasmListObservationRequestDto.getValue()));

		return this.germplasmListService.saveListDataObservation(listId, germplasmListObservationRequestDto);
	}

	@Override
	public void update(final Integer listId, final Integer observationId,
		final String value) {
		this.errors = new MapBindingResult(new HashMap<>(), GermplasmListObservationRequestDto.class.getName());
		BaseValidator.checkNotNull(listId, "param.null", new String[] {"listId"});
		BaseValidator.checkNotNull(listId, "param.null", new String[] {"observationId"});
		BaseValidator.checkNotNull(value, "param.null", new String[] {"request body"});

		final GermplasmList germplasmList = this.germplasmListValidator.validateGermplasmListExists(listId);
		this.germplasmListValidator.validateListIsNotAFolder(germplasmList);
		this.germplasmListValidator.validateListIsUnlocked(germplasmList);

		final GermplasmListObservationDto germplasmListObservationDto = this.validateObservationExists(observationId);
		this.validateObservationBelongsToList(listId, germplasmListObservationDto);

		this.validateValue(value);
		final Variable variable = this.ontologyVariableDataManager.getVariable(null, germplasmListObservationDto.getVariableId(), false);
		this.validateVariableDataTypeValue(variable, value);

		final Integer cValueId = VariableValueUtil.resolveCategoricalValueId(variable, value);
		this.germplasmListService.updateListDataObservation(observationId, value, cValueId);

	}

	@Override
	public void delete(final Integer listId, final Integer observationId) {
		this.errors = new MapBindingResult(new HashMap<>(), Integer.class.getName());

		final GermplasmList germplasmList = this.germplasmListValidator.validateGermplasmListExists(listId);
		this.germplasmListValidator.validateListIsNotAFolder(germplasmList);
		this.germplasmListValidator.validateListIsUnlocked(germplasmList);

		final GermplasmListObservationDto germplasmListObservationDto = this.validateObservationExists(observationId);
		this.validateObservationBelongsToList(listId, germplasmListObservationDto);

		this.germplasmListService.deleteListDataObservation(observationId);
	}

	@Override
	public long countObservationsByVariables(final Integer listId, final List<Integer> variableIds) {
		this.errors = new MapBindingResult(new HashMap<>(), Integer.class.getName());
		BaseValidator.checkNotNull(listId, "param.null", new String[] {"listId"});
		BaseValidator.checkNotNull(listId, "param.null", new String[] {"variableIds"});

		final GermplasmList germplasmList = this.germplasmListValidator.validateGermplasmListExists(listId);
		this.germplasmListValidator.validateListIsNotAFolder(germplasmList);

		//validate variableIds

		this.germplasmListVariableValidator.validateAllVariableIdsAreAssociatedToList(listId, variableIds);
		return this.germplasmListService.countObservationsByVariables(listId, variableIds);
	}

	private void validateObservationBelongsToList(final Integer listId, final GermplasmListObservationDto germplasmListObservationDto) {
		final GermplasmListDataDto germplasmListDataDto =
			this.germplasmListService.getGermplasmListData(germplasmListObservationDto.getListDataId()).get();
		if (!listId.equals(germplasmListDataDto.getListId())) {
			this.errors.reject("germplasm.list.data.observation.id.does.not.match.list", "");
			throw new ApiRequestValidationException(this.errors.getAllErrors());
		}

	}

	private Variable validateVariableIdIsAVariable(final Integer variableId) {
		final Variable variable = this.ontologyVariableDataManager.getVariable(null, variableId, false);
		if (variable == null) {
			this.errors.reject("germplasm.list.variable.does.not.exist", "");
			throw new ApiRequestValidationException(this.errors.getAllErrors());
		}
		return variable;
	}

	private GermplasmListDataDto validateListDataExists(final Integer listDataId) {
		final Optional<GermplasmListDataDto> germplasmListDataDtoOptional = this.germplasmListService.getGermplasmListData(listDataId);
		if (!germplasmListDataDtoOptional.isPresent()) {
			this.errors.reject("germplasm.list.data.id.not.exists", "");
			throw new ApiRequestValidationException(this.errors.getAllErrors());
		}
		return germplasmListDataDtoOptional.get();
	}

	private void validateListDataBelongsToTheList(final Integer listId, final GermplasmListDataDto germplasmListDataDto) {
		if (!listId.equals(germplasmListDataDto.getListId())) {
			this.errors.reject("germplasm.list.data.id.does.not.belong.to.list", "");
			throw new ApiRequestValidationException(this.errors.getAllErrors());
		}
	}

	private void validateVariableDataTypeValue(final Variable variable, final String value) {
		if (!VariableValueUtil.isValidAttributeValue(variable, value)) {
			this.errors.reject("invalid.variable.value", "");
			throw new ApiRequestValidationException(errors.getAllErrors());
		}
	}

	private GermplasmListObservationDto validateObservationExists(final Integer observationId) {
		final Optional<GermplasmListObservationDto> germplasmListObservationDto =
			this.germplasmListService.getGermplasmListObservation(observationId);
		if (!germplasmListObservationDto.isPresent()) {
			this.errors.reject("germplasm.list.data.observation.id.invalid", "");
			throw new ApiRequestValidationException(this.errors.getAllErrors());
		}
		return germplasmListObservationDto.get();
	}

	private void validateValue(final String value) {
		if (value.length() > VALUE_MAX_LENGTH) {
			this.errors.reject("germplasm.list.data.invalid.length", "");
			throw new ApiRequestValidationException(errors.getAllErrors());
		}
	}

}
