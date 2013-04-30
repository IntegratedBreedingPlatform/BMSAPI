package org.generationcp.ibpworkbench.rest;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

import org.generationcp.ibpworkbench.model.DataResponse;

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

@Path("/breeding_view")
public class BreedingView {

    @GET
    @Path("/ssa/save_result")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.TEXT_XML)
    public DataResponse saveSsaResult(@Context HttpServletRequest request) {
        DataResponse response = new DataResponse(true, "Successfully invoked service.");
        return response;
    }

    @GET
    @Path("/test")
    @Produces("text/plain")
    public String test() {
        return "WebService for BreedingView has been setup properly.";
    }
}
