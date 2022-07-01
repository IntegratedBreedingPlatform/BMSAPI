
package org.ibp.api.java.impl.middleware.common.validator;

import org.generationcp.middleware.api.crop.CropService;
import org.generationcp.middleware.pojos.workbench.CropType;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;

public class CropNameValidationInterceptorTest {

	@Mock
	private CropService cropServiceMW;

	@Mock
	private RequestInformationProvider requestInformationProvider;

	@Mock
	private HttpServletRequest request;

	@Mock
	private HttpServletResponse response;

	@Mock
	private ServletOutputStream responseOutputStream;

	@Mock
	private Object handler;

	private CropNameValidationInterceptor validationInterceptor;

	@Before
	public void beforeEachTest() throws IOException {
		MockitoAnnotations.initMocks(this);

		this.validationInterceptor = new CropNameValidationInterceptor();
		this.validationInterceptor.setCropServiceMW(this.cropServiceMW);
		this.validationInterceptor.setRequestInformationProvider(this.requestInformationProvider);

		Mockito.when(this.response.getOutputStream()).thenReturn(this.responseOutputStream);
	}

	@Test
	public void testValidPathParameterPresent() throws Exception {

		HashMap<String, String> testPathParameters = new HashMap<String, String>();
		testPathParameters.put("cropname", "wheat");

		Mockito.when(this.requestInformationProvider.getUrlTemplateAttributes()).thenReturn(testPathParameters);
		Mockito.when(this.cropServiceMW.getCropTypeByName("wheat")).thenReturn(new CropType("wheat"));

		Assert.assertTrue("Expecting handler to continue processing by returning true.",
				this.validationInterceptor.preHandle(this.request, this.response, this.handler));
	}

	@Test
	public void testInValidPathParameter() throws Exception {

		HashMap<String, String> testPathParameters = new HashMap<String, String>();
		testPathParameters.put("cropname", "nonExistantCrop");

		Mockito.when(this.requestInformationProvider.getUrlTemplateAttributes()).thenReturn(testPathParameters);
		Mockito.when(this.cropServiceMW.getCropTypeByName("nonExistantCrop")).thenReturn(null);

		Assert.assertFalse("Expecting handler to abort processing by returning false.",
				this.validationInterceptor.preHandle(this.request, this.response, this.handler));
	}

	@Test
	public void testNoCropNameParameterPresent() throws Exception {

		HashMap<String, String> testPathParameters = new HashMap<String, String>();

		Mockito.when(this.requestInformationProvider.getUrlTemplateAttributes()).thenReturn(testPathParameters);
		Mockito.when(this.cropServiceMW.getCropTypeByName("wheat")).thenReturn(new CropType("wheat"));

		Assert.assertTrue("Expecting handler to continue processing by returning true when there is no cropname parameter present.",
				this.validationInterceptor.preHandle(this.request, this.response, this.handler));
	}

}
