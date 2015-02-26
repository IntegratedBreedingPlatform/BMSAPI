package org.generationcp.bms.ontology.services.impl;

import org.generationcp.bms.ontology.dto.outgoing.MethodDTO;
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
    public List<MethodDTO> getAllMethods() throws MiddlewareQueryException {

        List<Method> methodList = ontologyService.getAllMethods();
        List<MethodDTO> methods = new ArrayList<>();

        ModelMapper mapper = OntologyMapper.methodMapper();

        for (Method method : methodList){
            MethodDTO methodDTO = mapper.map(method, MethodDTO.class);
            methods.add(methodDTO);
        }
        return methods;
    }
}
