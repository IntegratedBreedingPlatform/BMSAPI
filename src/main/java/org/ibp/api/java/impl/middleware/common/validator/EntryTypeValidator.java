package org.ibp.api.java.impl.middleware.common.validator;

import org.generationcp.middleware.ContextHolder;
import org.generationcp.middleware.domain.oms.TermId;
import org.generationcp.middleware.manager.api.OntologyDataManager;
import org.ibp.api.exception.ApiRequestValidationException;
import org.springframework.stereotype.Component;
import org.springframework.validation.BindingResult;
import org.springframework.validation.MapBindingResult;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class EntryTypeValidator {

	@Resource
	private OntologyDataManager ontologyDataManager;

	public void validateEntryType(final Integer entryTypeId) {
		final BindingResult errors = new MapBindingResult(new HashMap<String, String>(), Integer.class.getName());

		final List<Integer> entryTypeIds = this.ontologyDataManager.getStandardVariable(TermId.ENTRY_TYPE.getId(), ContextHolder.getCurrentProgram())
			.getEnumerations().stream().map(entryTypes -> entryTypes.getId()).collect(Collectors.toList());
		if (!entryTypeIds.contains(entryTypeId)) {
			errors.reject("entry.type.does.not.exist", "");
		}

		if (errors.hasErrors()) {
			throw new ApiRequestValidationException(errors.getAllErrors());
		}
	}

	void setOntologyDataManager(final OntologyDataManager ontologyDataManager){
		this.ontologyDataManager = ontologyDataManager;
	}
}
