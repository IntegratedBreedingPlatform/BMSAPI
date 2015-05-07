package org.ibp.api.java.impl.middleware.ontology;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import org.generationcp.middleware.domain.oms.CvId;
import org.generationcp.middleware.domain.ontology.Method;
import org.generationcp.middleware.exceptions.MiddlewareException;
import org.generationcp.middleware.manager.ontology.api.OntologyBasicDataManager;
import org.generationcp.middleware.manager.ontology.api.OntologyMethodDataManager;
import org.ibp.api.domain.common.GenericResponse;
import org.ibp.api.domain.ontology.MethodResponse;
import org.ibp.api.domain.ontology.MethodSummary;
import org.ibp.api.domain.ontology.TermRequest;
import org.ibp.api.exception.ApiRequestValidationException;
import org.ibp.api.exception.ApiRuntimeException;
import org.ibp.api.java.impl.middleware.common.CommonUtil;
import org.ibp.api.java.impl.middleware.ontology.validator.MethodValidator;
import org.ibp.api.java.impl.middleware.ontology.validator.TermDeletableValidator;
import org.ibp.api.java.ontology.OntologyMethodService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.BindingResult;
import org.springframework.validation.MapBindingResult;

@Service
public class OntologyMethodServiceImpl implements OntologyMethodService{

	@Autowired
	private OntologyMethodDataManager ontologyMethodDataManager;
	
	@Autowired
	private OntologyBasicDataManager ontologyBasicDataManager;
	
	@Autowired
	private MethodValidator methodValidator;
	
	@Autowired
	protected TermDeletableValidator termDeletableValidator;

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
	public MethodResponse getMethod(Integer id) {
		try {
			Method method = this.ontologyMethodDataManager.getMethod(id);
			if (method == null) {
			  	return null;
			}
			boolean deletable = true;
			if (this.ontologyBasicDataManager.isTermReferred(id)) {
			  	deletable = false;
			}
			ModelMapper mapper = OntologyMapper.getInstance();
			MethodResponse response = mapper.map(method, MethodResponse.class);
			String FIELD_TO_BE_EDITABLE_IF_TERM_REFERRED = "description";
			if (!deletable) {
			  response.setEditableFields(new ArrayList<>(Collections.singletonList(FIELD_TO_BE_EDITABLE_IF_TERM_REFERRED)));
			} else {
			  	response.setEditableFields(new ArrayList<>(Arrays.asList("name", FIELD_TO_BE_EDITABLE_IF_TERM_REFERRED)));
			}
			response.setDeletable(deletable);
			return response;
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
	public void updateMethod(Integer id, MethodSummary method) {
		BindingResult errors = new MapBindingResult(new HashMap<String, String>(), "Method");
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
	public void deleteMethod(Integer id) {
		BindingResult errors = new MapBindingResult(new HashMap<String, String>(), "Method");
		this.termDeletableValidator.validate(new TermRequest(String.valueOf(id), "Method", CvId.METHODS.getId()), errors);
		if (errors.hasErrors()) {
			throw new ApiRequestValidationException(errors.getAllErrors());
		}

		try {
			this.ontologyMethodDataManager.deleteMethod(id);
		} catch (MiddlewareException e) {
			throw new ApiRuntimeException("Error!", e);
		}
	}
}
