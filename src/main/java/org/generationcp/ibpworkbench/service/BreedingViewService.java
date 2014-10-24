/*******************************************************************************
 * Copyright (c) 2012, All Rights Reserved.
 *
 * Generation Challenge Programme (GCP)
 *
 *
 * This software is licensed for use under the terms of the GNU General Public
 * License (http://bit.ly/8Ztv8M) and the provisions of Part F of the Generation
 * Challenge Programme Amended Consortium Agreement (http://bit.ly/KQX1nL)
 *
 *******************************************************************************/
package org.generationcp.ibpworkbench.service;

import java.util.List;
import java.util.Map;

import org.generationcp.ibpworkbench.exceptions.IBPWebServiceException;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;

public interface BreedingViewService {

    void execute(Map<String, String> params, List<String> errors) throws IBPWebServiceException;

    void deleteDataSet(Integer dataSetId) throws MiddlewareQueryException;
}
