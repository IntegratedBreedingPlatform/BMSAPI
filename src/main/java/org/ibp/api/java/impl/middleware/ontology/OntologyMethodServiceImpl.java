package org.ibp.api.java.impl.middleware.ontology;

import org.generationcp.middleware.domain.oms.OntologyMethod;
import org.generationcp.middleware.exceptions.MiddlewareException;
import org.generationcp.middleware.manager.ontology.api.OntologyBasicDataManager;
import org.generationcp.middleware.manager.ontology.api.OntologyMethodDataManager;
import org.ibp.api.domain.common.GenericResponse;
import org.ibp.api.domain.ontology.MethodRequest;
import org.ibp.api.domain.ontology.MethodResponse;
import org.ibp.api.domain.ontology.MethodSummary;
import org.ibp.api.exception.ApiRuntimeException;
import org.ibp.api.java.impl.middleware.common.CommonUtil;
import org.ibp.api.java.ontology.OntologyMethodService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Service
public class OntologyMethodServiceImpl implements OntologyMethodService{

	@Autowired
	private OntologyMethodDataManager ontologyMethodDataManager;
	@Autowired
	private OntologyBasicDataManager ontologyBasicDataManager;

  	@Override
	public List<MethodSummary> getAllMethods() {
		try {
			List<OntologyMethod> methodList = this.ontologyMethodDataManager.getAllMethods();
			List<MethodSummary> methods = new ArrayList<>();

			ModelMapper mapper = OntologyMapper.getInstance();

			for (OntologyMethod method : methodList) {
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
			OntologyMethod method = this.ontologyMethodDataManager.getMethod(id);
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
	public GenericResponse addMethod(MethodRequest request) {
		try {
			OntologyMethod method = new OntologyMethod();
			method.setName(request.getName());
			method.setDefinition(request.getDescription());
			this.ontologyMethodDataManager.addMethod(method);
			return new GenericResponse(method.getId());
		} catch (MiddlewareException e) {
			throw new ApiRuntimeException("Error!", e);
		}
	}

	@Override
	public void updateMethod(Integer id, MethodRequest request) {
		try {
			OntologyMethod method = new OntologyMethod();
			method.setId(CommonUtil.tryParseSafe(request.getId()));
			method.setName(request.getName());
			method.setDefinition(request.getDescription());
			this.ontologyMethodDataManager.updateMethod(method);
		} catch (MiddlewareException e) {
			throw new ApiRuntimeException("Error!", e);
		}
	}

	@Override
	public void deleteMethod(Integer id) {
	  	try {
			this.ontologyMethodDataManager.deleteMethod(id);
		} catch (MiddlewareException e) {
			throw new ApiRuntimeException("Error!", e);
		}
	}
}
