package org.ibp.api.java.impl.middleware.common.validator;

import org.generationcp.middleware.api.breedingmethod.BreedingMethodNewRequest;
import org.generationcp.middleware.pojos.MethodClass;
import org.generationcp.middleware.pojos.MethodGroup;
import org.generationcp.middleware.pojos.MethodType;
import org.ibp.api.exception.ApiRequestValidationException;
import org.springframework.stereotype.Component;
import org.springframework.validation.MapBindingResult;

import java.util.HashMap;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.joining;
import static org.ibp.api.java.impl.middleware.common.validator.BaseValidator.checkArgument;
import static org.ibp.api.java.impl.middleware.common.validator.BaseValidator.checkNotNull;

@Component
public class BreedingMethodValidator {

	public void validate(final BreedingMethodNewRequest breedingMethod) {
		checkNotNull(breedingMethod, "request.null");
		checkNotNull(breedingMethod.getName(), "field.is.required", new String[] {"name"});
		checkArgument(breedingMethod.getName().length() <= 50, "text.field.max.length", new String[] {"name", "50"});
		checkNotNull(breedingMethod.getCode(), "field.is.required", new String[] {"code"});
		checkArgument(breedingMethod.getCode().length() <= 8, "text.field.max.length", new String[] {"code", "8"});

		final String description = breedingMethod.getDescription();
		checkArgument(description == null || description.length() <= 255, "text.field.max.length", new String[] {"description", "255"});

		final String type = breedingMethod.getType();
		final MethodType methodType = MethodType.getMethodType(type);
		if (methodType == null) {
			final String validTypes = MethodType.getAll().stream().map(MethodType::getCode).collect(joining(", "));
			throw new ApiRequestValidationException("breeding.methods.invalid.type", new String[] {type, validTypes});
		}

		final Integer numberOfProgenitors = breedingMethod.getNumberOfProgenitors();
		checkNotNull(numberOfProgenitors, "field.is.required", new String[] {"numberOfProgenitors"});

		if (methodType.equals(MethodType.GENERATIVE)) {
			checkArgument(numberOfProgenitors >= 0 && numberOfProgenitors <= 2, "breeding.methods.invalid.numberOfProgenitors.generative");
		} else {
			checkArgument(numberOfProgenitors == -1, "breeding.methods.invalid.numberOfProgenitors.derivative");
		}

		final String group = breedingMethod.getGroup();
		final MethodGroup methodGroup = MethodGroup.getMethodGroup(group);
		if (methodGroup == null) {
			final String validGroups = MethodGroup.getAll().stream().map(MethodGroup::getCode).collect(joining(", "));
			throw new ApiRequestValidationException("breeding.methods.invalid.group", new String[] {group, validGroups});
		}

		final Integer methodClass = breedingMethod.getMethodClass();
		final MethodClass methodClass1 = MethodClass.getMethodClass(methodClass);
		if (methodClass1 == null) {
			final String validClasses = MethodClass.getAll().stream().map(MethodClass::getId).map(Object::toString).collect(joining(", "));
			throw new ApiRequestValidationException("breeding.methods.invalid.class", new Object[] {methodClass, validClasses});
		}

		checkArgument(breedingMethod.getSeparator() == null || breedingMethod.getSeparator().length() <= 255,
			"text.field.max.length", new String[] {"separator", "255"});
		checkArgument(breedingMethod.getPrefix() == null || breedingMethod.getPrefix().length() <= 255,
			"text.field.max.length", new String[] {"prefix", "255"});
		checkArgument(breedingMethod.getCount() == null || breedingMethod.getCount().length() <= 255,
			"text.field.max.length", new String[] {"count", "255"});
		checkArgument(breedingMethod.getSuffix() == null || breedingMethod.getSuffix().length() <= 255,
			"text.field.max.length", new String[] {"suffix", "255"});
	}
}
