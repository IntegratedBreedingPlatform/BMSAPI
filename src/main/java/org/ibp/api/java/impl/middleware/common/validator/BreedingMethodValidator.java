package org.ibp.api.java.impl.middleware.common.validator;

import org.generationcp.middleware.api.breedingmethod.BreedingMethodDTO;
import org.generationcp.middleware.api.breedingmethod.BreedingMethodNewRequest;
import org.generationcp.middleware.api.breedingmethod.BreedingMethodService;
import org.generationcp.middleware.api.germplasm.GermplasmService;
import org.generationcp.middleware.domain.oms.TermId;
import org.generationcp.middleware.pojos.Germplasm;
import org.generationcp.middleware.pojos.MethodClass;
import org.generationcp.middleware.pojos.MethodGroup;
import org.generationcp.middleware.pojos.MethodType;
import org.generationcp.middleware.service.api.dataset.DatasetService;
import org.ibp.api.exception.ApiRequestValidationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

import static java.util.stream.Collectors.joining;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.ibp.api.java.impl.middleware.common.validator.BaseValidator.checkArgument;
import static org.ibp.api.java.impl.middleware.common.validator.BaseValidator.checkNotNull;

@Component
public class BreedingMethodValidator {
	
	@Autowired
	private BreedingMethodService breedingMethodService;

	@Autowired
	private GermplasmService germplasmService;

	@Autowired
	private DatasetService datasetService;

	public void validateCreation(final BreedingMethodNewRequest breedingMethod) {
		checkNotNull(breedingMethod, "request.null");
		checkArgument(!isBlank(breedingMethod.getName()), "field.is.required", new String[] {"name"});
		checkArgument(!isBlank(breedingMethod.getCode()), "field.is.required", new String[] {"code"});
		checkArgument(!isBlank(breedingMethod.getDescription()), "field.is.required", new String[] {"description"});

		final String type = breedingMethod.getType();
		final MethodType methodType = MethodType.getMethodType(type);

		this.validateMethodType(type, methodType);

		final Integer numberOfProgenitors = breedingMethod.getNumberOfProgenitors();
		checkNotNull(numberOfProgenitors, "field.is.required", new String[] {"numberOfProgenitors"});
		this.validateProgenitorTypeDuo(numberOfProgenitors, methodType);

		final String group = breedingMethod.getGroup();
		final MethodGroup methodGroup = MethodGroup.getMethodGroup(group);
		validateMethodGroup(group, methodGroup);

		final Integer methodClassId = breedingMethod.getMethodClass();
		final MethodClass methodClass = MethodClass.getMethodClass(methodClassId);
		validateMethodClass(methodClassId, methodClass);

		this.validateClassTypeDuo(methodClass, methodType);

		this.validateFieldsLength(breedingMethod);
	}

	public void validateEdition(final Integer breedingMethodDbId, final BreedingMethodNewRequest breedingMethodRequest) {
		final Optional<BreedingMethodDTO> methodOptional = this.breedingMethodService.getBreedingMethod(breedingMethodDbId);
		if (!methodOptional.isPresent()) {
			throw new ApiRequestValidationException("breeding.methods.not.exists", new Integer[] {breedingMethodDbId});
		}
		final BreedingMethodDTO breedingMethodDTO = methodOptional.get();

		/*
		 * Validate combinations of numberOfProgenitors, methodType, methodClasses
		 * coming either from the request or the db
		 */

		final Integer numberOfProgenitors = breedingMethodRequest.getNumberOfProgenitors() != null
			? breedingMethodRequest.getNumberOfProgenitors()
			: breedingMethodDTO.getNumberOfProgenitors();

		final String breedingMethodRequestType = breedingMethodRequest.getType();
		final String type = !isBlank(breedingMethodRequestType)
			? breedingMethodRequestType
			: breedingMethodDTO.getType();
		final MethodType methodType = MethodType.getMethodType(type);

		this.validateMethodType(type, methodType);
		this.validateProgenitorTypeDuo(numberOfProgenitors, methodType);

		final Integer methodClassId = breedingMethodRequest.getMethodClass() != null
			? breedingMethodRequest.getMethodClass()
			: breedingMethodDTO.getMethodClass();
		final MethodClass methodClass = MethodClass.getMethodClass(methodClassId);

		this.validateMethodClass(methodClassId, methodClass);
		this.validateClassTypeDuo(methodClass, methodType);

		final String group = breedingMethodRequest.getGroup();
		if (!isBlank(group)) {
			final MethodGroup methodGroup = MethodGroup.getMethodGroup(group);
			this.validateMethodGroup(group, methodGroup);
		}

		this.validateFieldsLength(breedingMethodRequest);
	}

	public void validateDeletion(final Integer breedingMethodDbId) {
		final Optional<BreedingMethodDTO> methodOptional = this.breedingMethodService.getBreedingMethod(breedingMethodDbId);
		if (!methodOptional.isPresent()) {
			throw new ApiRequestValidationException("breeding.methods.not.exists", new Integer[] {breedingMethodDbId});
		}

		// We avoid count for now as it can be slow
		Optional<Germplasm> germplasmOptional = this.germplasmService.findOneByMethodId(breedingMethodDbId);
		if (germplasmOptional.isPresent()) {
			throw new ApiRequestValidationException("breeding.methods.delete.has.germplasm",
				new String[] {germplasmOptional.get().getGid().toString()}); }

		final BreedingMethodDTO breedingMethodDTO = methodOptional.get();
		final long projectCount = this.datasetService.countByVariableIdAndValue(TermId.BREEDING_METHOD_CODE.getId(),
			breedingMethodDTO.getCode());
		if (projectCount > 0) {
			throw new ApiRequestValidationException("breeding.methods.delete.has.projects", new Object[] {projectCount});
		}

		final long observationsCount = this.datasetService.countObservationsByVariableIdAndValue(TermId.BREEDING_METHOD_VARIATE_CODE.getId(),
			breedingMethodDTO.getCode());
		if (observationsCount > 0) {
			throw new ApiRequestValidationException("breeding.methods.delete.has.observations", new Object[] {observationsCount});
		}
	}

	private void validateFieldsLength(final BreedingMethodNewRequest breedingMethod) {
		final String name = breedingMethod.getName();
		checkArgument(name == null || name.length() <= 50, "text.field.max.length", new String[] {"name", "50"});

		final String code = breedingMethod.getCode();
		checkArgument(code == null || code.length() <= 8, "text.field.max.length", new String[] {"code", "8"});

		final String description = breedingMethod.getDescription();
		checkArgument(description == null || description.length() <= 255, "text.field.max.length", new String[] {"description", "255"});

		checkArgument(breedingMethod.getSeparator() == null || breedingMethod.getSeparator().length() <= 255,
			"text.field.max.length", new String[] {"separator", "255"});
		checkArgument(breedingMethod.getPrefix() == null || breedingMethod.getPrefix().length() <= 255,
			"text.field.max.length", new String[] {"prefix", "255"});
		checkArgument(breedingMethod.getCount() == null || breedingMethod.getCount().length() <= 255,
			"text.field.max.length", new String[] {"count", "255"});
		checkArgument(breedingMethod.getSuffix() == null || breedingMethod.getSuffix().length() <= 255,
			"text.field.max.length", new String[] {"suffix", "255"});
	}

	private void validateMethodType(final String type, final MethodType methodType) {
		if (methodType == null) {
			final String validTypes = MethodType.getAll().stream().map(MethodType::getCode).collect(joining(", "));
			throw new ApiRequestValidationException("breeding.methods.invalid.type", new String[] {type, validTypes});
		}
	}

	private void validateProgenitorTypeDuo(final Integer numberOfProgenitors, final MethodType methodType) {
		if (methodType.equals(MethodType.GENERATIVE)) {
			checkArgument(numberOfProgenitors >= 0 && numberOfProgenitors <= 2, "breeding.methods.invalid.numberOfProgenitors.generative");
		} else {
			checkArgument(numberOfProgenitors == -1, "breeding.methods.invalid.numberOfProgenitors.derivative");
		}
	}

	private void validateClassTypeDuo(final MethodClass methodClass, final MethodType methodType) {
		final List<MethodClass> classes = MethodClass.getByMethodType().get(methodType);
		if (!classes.contains(methodClass)) {
			throw new ApiRequestValidationException("breeding.methods.invalid.typeclassduo",
				new Object[] {methodClass, methodType, classes});
		}
	}

	private void validateMethodClass(final Integer methodClassId, final MethodClass methodClass) {
		if (methodClass == null) {
			final String validClasses = MethodClass.getAll().stream().map(MethodClass::getId).map(Object::toString).collect(joining(", "));
			throw new ApiRequestValidationException("breeding.methods.invalid.class", new Object[] {methodClassId, validClasses});
		}
	}

	private void validateMethodGroup(final String group, final MethodGroup methodGroup) {
		if (methodGroup == null) {
			final String validGroups = MethodGroup.getAll().stream().map(MethodGroup::getCode).collect(joining(", "));
			throw new ApiRequestValidationException("breeding.methods.invalid.group", new String[] {group, validGroups});
		}
	}
}
