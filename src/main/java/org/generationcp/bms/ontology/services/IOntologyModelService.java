package org.generationcp.bms.ontology.services;


import org.generationcp.bms.ontology.dto.outgoing.MethodResponse;
import org.generationcp.bms.ontology.dto.outgoing.MethodSummary;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;

import java.util.List;

public interface IOntologyModelService {

    public List<MethodSummary> getAllMethods() throws MiddlewareQueryException;
    public MethodResponse getMethod(Integer id) throws MiddlewareQueryException;
}
