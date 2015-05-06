package org.ibp.api.java.impl.middleware.ontology.validator;

import org.generationcp.middleware.domain.oms.CvId;
import org.generationcp.middleware.exceptions.MiddlewareException;
import org.ibp.api.domain.ontology.TermRequest;
import org.ibp.api.java.impl.middleware.common.CommonUtil;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;

import java.util.Objects;

@Component
public class TermDeletableValidator extends OntologyValidator implements
org.springframework.validation.Validator {

	@Override
	public boolean supports(Class<?> aClass) {
		return TermRequest.class.equals(aClass);
	}

	@Override
	public void validate(Object target, Errors errors) {
		TermRequest request = (TermRequest) target;

		try {
			this.checkTermExist(request.getTermName(), request.getId(), request.getCvId(), errors);

			if (errors.hasErrors()) {
				return;
			}

			if(Objects.equals(request.getCvId(), CvId.VARIABLES.getId())){
				int observations = this.ontologyBasicDataManager.getVariableObservations(Integer.valueOf(request.getId()));
				if(observations == 0){
					return;
				}
			} else {
				boolean isReferred = this.ontologyBasicDataManager.isTermReferred(CommonUtil.tryParseSafe(request.getId()));
				if (!isReferred) {
					return;
				}
			}

			this.addCustomError(errors, RECORD_IS_NOT_DELETABLE, new Object[] { request.getTermName(), request.getId() });

		} catch (MiddlewareException e) {
			this.log.error("Error while validating object", e);
		}
	}
}
