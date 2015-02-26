package org.generationcp.bms.ontology.services;


import org.generationcp.bms.ontology.dto.outgoing.MethodDTO;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;

import java.util.List;

public interface IOntologyModelService {

    public List<MethodDTO> getAllMethods() throws MiddlewareQueryException;
}
