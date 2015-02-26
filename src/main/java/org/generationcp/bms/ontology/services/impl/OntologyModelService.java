package org.generationcp.bms.ontology.services.impl;

import org.generationcp.bms.ontology.dto.outgoing.MethodSummary;
import org.generationcp.bms.ontology.dto.outgoing.MethodResponse;
import org.generationcp.bms.ontology.services.IOntologyModelService;
import org.generationcp.bms.ontology.services.OntologyMapper;
import org.generationcp.middleware.domain.oms.Method;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.service.api.OntologyService;
import org.modelmapper.ModelMapper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class OntologyModelService implements IOntologyModelService {

    @Autowired
    private OntologyService ontologyService;

    @Override
    public List<MethodSummary> getAllMethods() throws MiddlewareQueryException {

        List<Method> methodList = ontologyService.getAllMethods();
        List<MethodSummary> methods = new ArrayList<>();

        ModelMapper mapper = OntologyMapper.methodMapper();

        for (Method method : methodList){
            MethodSummary methodSummary = mapper.map(method, MethodSummary.class);
            methods.add(methodSummary);
        }
        return methods;
    }

    @Override
    public MethodResponse getMethod(Integer id) throws MiddlewareQueryException {
        Method method = ontologyService.getMethod(id);
        if(method == null) return null;
        ModelMapper mapper = OntologyMapper.methodMapper();
        return mapper.map(method, MethodResponse.class);
    }

}
