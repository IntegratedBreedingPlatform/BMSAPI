package org.ibp.api.java.impl.middleware.ontology;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import org.generationcp.middleware.domain.oms.CvId;
import org.generationcp.middleware.domain.ontology.Method;
import org.generationcp.middleware.exceptions.MiddlewareException;
import org.generationcp.middleware.manager.ontology.api.OntologyMethodDataManager;
import org.generationcp.middleware.manager.ontology.api.TermDataManager;
import org.ibp.api.domain.common.GenericResponse;
import org.ibp.api.domain.ontology.MethodDetails;
import org.ibp.api.domain.ontology.MethodSummary;
import org.ibp.api.domain.ontology.TermRequest;
import org.ibp.api.exception.ApiRequestValidationException;
import org.ibp.api.exception.ApiRuntimeException;
import org.ibp.api.java.impl.middleware.common.CommonUtil;
import org.ibp.api.java.impl.middleware.ontology.validator.MethodValidator;
import org.ibp.api.java.impl.middleware.ontology.validator.MiddlewareIdFormatValidator;
import org.ibp.api.java.impl.middleware.ontology.validator.TermDeletableValidator;
import org.ibp.api.java.impl.middleware.ontology.validator.TermValidator;
import org.ibp.api.java.ontology.OntologyMethodService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.BindingResult;
import org.springframework.validation.MapBindingResult;

@Service
public class OntologyMethodServiceImpl implements OntologyMethodService {

	@Autowired
	private TermDataManager termDataManager;

	@Autowired
	private OntologyMethodDataManager ontologyMethodDataManager;
	
	@Autowired
	private MethodValidator methodValidator;
	
	@Autowired
	protected TermDeletableValidator termDeletableValidator;
	
	@Autowired
	protected MiddlewareIdFormatValidator idFormatValidator;
	
	@Autowired
	protected TermValidator termValidator;

  	@Override
	public List<MethodSummary> getAllMethods() {
		try {
			List<Method> methodList = this.ontologyMethodDataManager.getAllMethods();
			List<MethodSummary> methods = new ArrayList<>();

			ModelMapper mapper = OntologyMapper.getInstance();

			for (Method method : methodList) {
			  	MethodSummary methodSummary = mapper.map(method, MethodSummary.class);
			  	methods.add(methodSummary);
			}
			return methods;
		} catch (MiddlewareException e) {
			throw new ApiRuntimeException("Error!", e);
		}
	}

	@Override
	public MethodDetails getMethod(String id) {
		validateId(id);
		BindingResult errors = new MapBindingResult(new HashMap<String, String>(), "Method");
		TermRequest term = new TermRequest(id, "method", CvId.METHODS.getId());
		this.termValidator.validate(term, errors);
		if (errors.hasErrors()) {
			throw new ApiRequestValidationException(errors.getAllErrors());
		}
		
		try {
			Method method = this.ontologyMethodDataManager.getMethod(Integer.valueOf(id));
			if (method == null) {
			  	return null;
			}
			boolean deletable = true;
			if (this.termDataManager.isTermReferred(Integer.valueOf(id))) {
			  	deletable = false;
			}
			ModelMapper mapper = OntologyMapper.getInstance();
			MethodDetails methodDetails = mapper.map(method, MethodDetails.class);
			String FIELD_TO_BE_EDITABLE_IF_TERM_REFERRED = "description";
			if (!deletable) {
			  methodDetails.getMetadata().setEditableFields(new ArrayList<>(Collections.singletonList(FIELD_TO_BE_EDITABLE_IF_TERM_REFERRED)));
			} else {
			  	methodDetails.getMetadata().setEditableFields(new ArrayList<>(Arrays.asList("name", FIELD_TO_BE_EDITABLE_IF_TERM_REFERRED)));
			}
			methodDetails.getMetadata().setDeletable(deletable);
			return methodDetails;
		} catch (MiddlewareException e) {
			throw new ApiRuntimeException("Error!", e);
		}
	}

	@Override
	public GenericResponse addMethod(MethodSummary method) {
		method.setId(null);
		BindingResult errors = new MapBindingResult(new HashMap<String, String>(), "Method");
		this.methodValidator.validate(method, errors);
		if (errors.hasErrors()) {
			throw new ApiRequestValidationException(errors.getAllErrors());
		}
		
		try {
			Method middlewareMethod = new Method();
			middlewareMethod.setName(method.getName());
			middlewareMethod.setDefinition(method.getDescription());
			this.ontologyMethodDataManager.addMethod(middlewareMethod);
			return new GenericResponse(middlewareMethod.getId());
		} catch (MiddlewareException e) {
			throw new ApiRuntimeException("Error!", e);
		}
	}

	@Override
	public void updateMethod(String id, MethodSummary method) {
		validateId(id);
		BindingResult errors = new MapBindingResult(new HashMap<String, String>(), "Method");
		TermRequest term = new TermRequest(id, "method", CvId.METHODS.getId());
		this.termValidator.validate(term, errors);
		if (errors.hasErrors()) {
			throw new ApiRequestValidationException(errors.getAllErrors());
		}
		method.setId(id);
		this.methodValidator.validate(method, errors);
		if (errors.hasErrors()) {
			throw new ApiRequestValidationException(errors.getAllErrors());
		}
		try {
			Method middlewareMethod = new Method();
			middlewareMethod.setId(CommonUtil.tryParseSafe(method.getId()));
			middlewareMethod.setName(method.getName());
			middlewareMethod.setDefinition(method.getDescription());
			this.ontologyMethodDataManager.updateMethod(middlewareMethod);
		} catch (MiddlewareException e) {
			throw new ApiRuntimeException("Error!", e);
		}
	}

	@Override
	public void deleteMethod(String id) {
		validateId(id);
		BindingResult errors = new MapBindingResult(new HashMap<String, String>(), "Method");
		this.termDeletableValidator.validate(new TermRequest(String.valueOf(id), "Method", CvId.METHODS.getId()), errors);
		if (errors.hasErrors()) {
			throw new ApiRequestValidationException(errors.getAllErrors());
		}

		try {
			this.ontologyMethodDataManager.deleteMethod(Integer.valueOf(id));
		} catch (MiddlewareException e) {
			throw new ApiRuntimeException("Error!", e);
		}
	}
	
	private void validateId(String id) {
		BindingResult errors = new MapBindingResult(new HashMap<String, String>(), "Method");
		this.idFormatValidator.validate(id, errors);
		if (errors.hasErrors()) {
			throw new ApiRequestValidationException(errors.getAllErrors());
		}
	}
}
