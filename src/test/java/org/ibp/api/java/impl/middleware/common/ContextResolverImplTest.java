
package org.ibp.api.java.impl.middleware.common;

import liquibase.util.StringUtils;
import org.ibp.api.java.crop.CropService;
import org.ibp.api.java.program.ProgramService;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Arrays;
import java.util.Collections;

public class ContextResolverImplTest {

	@Mock
	private CropService cropService;

	@Mock
	private ProgramService programService;

	private ContextResolverImpl contextResolverImpl;

	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);
		this.contextResolverImpl = new ContextResolverImpl();
		this.contextResolverImpl.setCropService(cropService);
		this.contextResolverImpl.setProgramService(this.programService);

		Mockito.doReturn(Collections.singletonList("maize")).when(this.cropService).getInstalledCrops();
	}


	@Test(expected = ContextResolutionException.class)
	public void testResolveDatabaseFromUrl_InvalidURL() {

		// Non-null request but bad URL.Expect exception.
		MockHttpServletRequest request = new MockHttpServletRequest();
		request.setRequestURI("/locations");
		RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

		this.contextResolverImpl.resolveDatabaseFromUrl();
	}

	@Test
	public void testResolveDatabaseFromUrl_ValidURLBMSAPI() {

		MockHttpServletRequest request = new MockHttpServletRequest();
		request.setRequestURI("/crops/maize/locations");

		RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

		String database = this.contextResolverImpl.resolveDatabaseFromUrl();
		Assert.assertEquals("Crop database was not resolved correctly for BMSAPI request URL.", "ibdbv2_maize_merged", database);
	}

	@Test
	public void testResolveDatabaseFromUrl_ValidURLBrAPI() {

		MockHttpServletRequest request = new MockHttpServletRequest();
		request.setRequestURI("/maize/brapi/v1/locations");

		RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

		String database = this.contextResolverImpl.resolveDatabaseFromUrl();
		Assert.assertEquals("Crop database was not resolved correctly for BrAPI request URL.", "ibdbv2_maize_merged", database);
	}


	@Test
	public void testResolveCropNameFromUrl_BrAPIValidCrop() {

		MockHttpServletRequest request = new MockHttpServletRequest();
		request.setRequestURI("/maize/brapi/v1/locations");

		RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

		String crop = this.contextResolverImpl.resolveCropNameFromUrl();
		Assert.assertEquals("Crop was not resolved correctly for BrAPI request URL.", "maize", crop);
	}

	@Test(expected = ContextResolutionException.class)
	public void testResolveCropNameFromUrl_BrAPIInvalidCrop() {

		MockHttpServletRequest request = new MockHttpServletRequest();
		request.setRequestURI("/rice/brapi/v1/locations");

		RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

		this.contextResolverImpl.resolveCropNameFromUrl();
	}

	@Test
	public void testResolveCropNameFromUrl_BrAPIInstanceLevelResource() {

		MockHttpServletRequest request = new MockHttpServletRequest();
		request.setRequestURI("/brapi/v1/crops");

		RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

		String crop = this.contextResolverImpl.resolveCropNameFromUrl();
		Assert.assertTrue(StringUtils.isEmpty(crop));
	}


	@Test
	public void testResolveCropNameFromUrl_BMSAPIValidCropFromPathVariable() {

		MockHttpServletRequest request = new MockHttpServletRequest();
		request.setRequestURI("/crops/maize/variables");

		RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

		String crop = this.contextResolverImpl.resolveCropNameFromUrl();
		Assert.assertEquals("Crop was not resolved correctly for BMSAPI request URL.", "maize", crop);
	}

	@Test(expected = ContextResolutionException.class)
	public void testResolveCropNameFromUrl_BMSAPIInvalidCropFromPathVariable() {

		MockHttpServletRequest request = new MockHttpServletRequest();
		request.setRequestURI("/crops/rice/variables");

		RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

		this.contextResolverImpl.resolveCropNameFromUrl();
	}

	@Test
	public void testResolveCropNameFromUrl_BMSAPIValidCropFromRequestParameter() {

		MockHttpServletRequest request = new MockHttpServletRequest();
		request.setRequestURI("/users");
		request.setParameter("cropName", "maize");

		RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

		String crop = this.contextResolverImpl.resolveCropNameFromUrl();
		Assert.assertEquals("Crop was not resolved correctly for BMSAPI request URL.", "maize", crop);
	}

	@Test(expected = ContextResolutionException.class)
	public void testResolveCropNameFromUrl_BMSAPIInvalidCropFromRequestParameter() {

		MockHttpServletRequest request = new MockHttpServletRequest();
		request.setRequestURI("/users");
		request.setParameter("cropName", "rice");

		RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

		this.contextResolverImpl.resolveCropNameFromUrl();
	}

	@Test
	public void testResolveCropNameFromUrl_BMSAPIInstanceLevelResource() {

		MockHttpServletRequest request = new MockHttpServletRequest();
		request.setRequestURI("/r-packages/1/calls");

		RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

		String crop = this.contextResolverImpl.resolveCropNameFromUrl();
		Assert.assertTrue(StringUtils.isEmpty(crop));
	}

	@Test
	public void testResolveCropNameFromUrl_BMSAPINoCrop() {

		MockHttpServletRequest request = new MockHttpServletRequest();
		request.setRequestURI("/breeding-view-licenses");

		RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

		String crop = this.contextResolverImpl.resolveCropNameFromUrl();
		Assert.assertTrue(StringUtils.isEmpty(crop));
	}

}
