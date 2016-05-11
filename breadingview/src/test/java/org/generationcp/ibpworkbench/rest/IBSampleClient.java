/*******************************************************************************
 * Copyright (c) 2012, All Rights Reserved.
 *
 * Generation Challenge Programme (GCP)
 *
 *
 * This software is licensed for use under the terms of the GNU General Public License (http://bit.ly/8Ztv8M) and the provisions of Part F
 * of the Generation Challenge Programme Amended Consortium Agreement (http://bit.ly/KQX1nL)
 *
 *******************************************************************************/

package org.generationcp.ibpworkbench.rest;

/*
 * import java.io.InputStream;
 * 
 * import javax.ws.rs.core.MediaType; import javax.ws.rs.core.MultivaluedMap;
 * 
 * import com.sun.jersey.api.client.ClientResponse; import com.sun.jersey.core.util.MultivaluedMapImpl; import org.junit.After; import
 * org.junit.Before; import org.junit.Test; import org.mortbay.jetty.Server; import org.mortbay.jetty.webapp.WebAppContext;
 * 
 * import com.sun.jersey.api.client.Client; import com.sun.jersey.api.client.WebResource; import com.sun.jersey.multipart.FormDataMultiPart;
 */

public class IBSampleClient {
	/*
	 * Server server;
	 * 
	 * @Before public void before() throws Exception { server = new Server(8080);
	 * 
	 * WebAppContext webapp = new WebAppContext(); webapp.setContextPath("/IBPWebService"); webapp.setWar("target/IBPWebService.war");
	 * 
	 * server.addHandler(webapp); server.start(); }
	 * 
	 * @After public void after() throws Exception { server.stop(); }
	 * 
	 * @Test public void upload() { uploadFile("http://localhost:8080/IBPWebService/rest/breeding_view/ssa/save_result", "data.csv"); }
	 * 
	 * @Test public void readOutputBreedingView() { String url = "http://localhost:8080/IBPWebService/rest/breeding_view/ssa/save_result?" +
	 * "Filename=C:/Efficio/3rdComing/trunk/IBPWebService/src/test/resources/Burkina_trait_means.csv&" +
	 * "StudyId=-1&WorkbenchProjectId=1&InputDataSetId=1&OutputDataSetId=1"; WebResource webResource = Client.create().resource(url);
	 * 
	 * ClientResponse response = webResource.accept(MediaType.TEXT_XML).get(ClientResponse.class); if (response.getStatus() != 200) { throw
	 * new RuntimeException("Failed : HTTP error code : " + response.getStatus()); } System.out.println(response); }
	 * 
	 * public void uploadFile(String url, String fileName) { InputStream stream = getClass().getClassLoader().getResourceAsStream(fileName);
	 * FormDataMultiPart part = new FormDataMultiPart().field("file", stream, MediaType.TEXT_PLAIN_TYPE);
	 * 
	 * WebResource resource = Client.create().resource(url); String response =
	 * resource.type(MediaType.MULTIPART_FORM_DATA_TYPE).post(String.class, part); System.out.println(response);
	 * //assertEquals("Hello, World", response); }
	 */
}
