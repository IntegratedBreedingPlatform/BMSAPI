
package org.ibp.api.java.impl.middleware.ontology.validator;

import org.apache.commons.collections.CollectionUtils;
import org.generationcp.middleware.domain.oms.Term;
import org.ibp.api.exception.ApiRequestValidationException;
import org.ibp.api.java.impl.middleware.ontology.TermRequest;
import org.springframework.stereotype.Component;
import org.springframework.validation.BindingResult;
import org.springframework.validation.Errors;
import org.springframework.validation.MapBindingResult;

import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class TermValidator extends OntologyValidator implements org.springframework.validation.Validator {

	@Override
	public boolean supports(final Class<?> aClass) {
		return TermRequest.class.equals(aClass);
	}

	@Override
	public void validate(final Object target, final Errors errors) {
		final TermRequest request = (TermRequest) target;
		this.checkTermExist(request.getTermName(), request.getId(), request.getCvId(), errors);
	}

	public void validate(final Integer termId) {

		final BindingResult errors = new MapBindingResult(new HashMap<>(), Integer.class.getName());

		if (this.termDataManager.getTermById(termId) == null) {
			errors.reject("variable.does.not.exist", new Object[] {termId}, "");
			throw new ApiRequestValidationException(errors.getAllErrors());
		}
	}

	public void validateTermIds(final List<Integer> termIds, final Errors errors) {
		final Set<Integer> existingVariableIds =
			this.termDataManager.getTermByIds(termIds).stream().map(Term::getId).collect(Collectors.toSet());
		final Set<Integer> termIdsNotExist = termIds.stream().filter(i -> !existingVariableIds.contains(i)).collect(Collectors.toSet());
		if (CollectionUtils.isNotEmpty(termIdsNotExist)) {
			termIdsNotExist.stream().forEach(o -> errors.reject("variable.does.not.exist", new Object[] {o}, ""));
		}
	}

}
