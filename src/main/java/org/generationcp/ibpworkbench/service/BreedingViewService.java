package org.generationcp.ibpworkbench.service;

import java.util.List;
import java.util.Map;

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

public interface BreedingViewService {
    public void execute(Map<String, String> params, List<String> errors) throws Exception;
}
