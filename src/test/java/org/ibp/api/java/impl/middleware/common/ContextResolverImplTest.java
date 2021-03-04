
package org.ibp.api.java.impl.middleware.common;

import liquibase.util.StringUtils;
import org.generationcp.middleware.ContextHolder;
import org.generationcp.middleware.api.program.ProgramDTO;
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

import java.util.Collections;

public class ContextResolverImplTest {

	public static final String MAIZE = "maize";
	public static final String PROGRAM_UUID = "abc-123";
	@Mock
	private CropService cropService;

	@Mock
	private ProgramService programService;

	private ContextResolverImpl contextResolverImpl;

	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);
		this.contextResolverImpl = new ContextResolverImpl();
		this.contextResolverImpl.setCropService(this.cropService);
		this.contextResolverImpl.setProgramService(this.programService);

		Mockito.doReturn(Collections.singletonList("Maize")).when(this.cropService).getInstalledCrops();
		Mockito.doReturn(new ProgramDTO()).when(this.programService).getByUUIDAndCrop(MAIZE, PROGRAM_UUID);
	}


	@Test(expected = ContextResolutionException.class)
	public void testResolveDatabaseFromUrl_InvalidURL() {

		// Non-null request but bad URL.Expect exception.
		final MockHttpServletRequest request = new MockHttpServletRequest();
		request.setRequestURI("/locations");
		RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

		this.contextResolverImpl.resolveDatabaseFromUrl();
	}

	@Test
	public void testResolveDatabaseFromUrl_ValidURLBMSAPI() {

		final MockHttpServletRequest request = new MockHttpServletRequest();
		request.setRequestURI("/crops/maize/locations");

		RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

		final String database = this.contextResolverImpl.resolveDatabaseFromUrl();
		Assert.assertEquals("Crop database was not resolved correctly for BMSAPI request URL.", "ibdbv2_maize_merged", database);
		Assert.assertEquals(MAIZE, ContextHolder.getCurrentCrop());
	}

	@Test
	public void testResolveDatabaseFromUrl_ValidURLBrAPI() {

		final MockHttpServletRequest request = new MockHttpServletRequest();
		request.setRequestURI("/maize/brapi/v1/locations");

		RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

		final String database = this.contextResolverImpl.resolveDatabaseFromUrl();
		Assert.assertEquals("Crop database was not resolved correctly for BrAPI request URL.", "ibdbv2_maize_merged", database);
		Assert.assertEquals(MAIZE, ContextHolder.getCurrentCrop());
	}


	@Test
	public void testResolveCropNameFromUrl_BrAPIValidCrop() {

		final MockHttpServletRequest request = new MockHttpServletRequest();
		request.setRequestURI("/maize/brapi/v1/locations");

		RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

		final String crop = this.contextResolverImpl.resolveCropNameFromUrl();
		Assert.assertEquals("Crop was not resolved correctly for BrAPI request URL.", MAIZE, crop);
		Assert.assertEquals(MAIZE, ContextHolder.getCurrentCrop());
	}

	@Test(expected = ContextResolutionException.class)
	public void testResolveCropNameFromUrl_BrAPIInvalidCrop() {

		final MockHttpServletRequest request = new MockHttpServletRequest();
		request.setRequestURI("/rice/brapi/v1/locations");

		RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

		this.contextResolverImpl.resolveCropNameFromUrl();
	}

	@Test
	public void testResolveCropNameFromUrl_BrAPIInstanceLevelResource() {

		final MockHttpServletRequest request = new MockHttpServletRequest();
		request.setRequestURI("/brapi/v1/crops");

		RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

		final String crop = this.contextResolverImpl.resolveCropNameFromUrl();
		Assert.assertTrue(StringUtils.isEmpty(crop));
	}


	@Test
	public void testResolveCropNameFromUrl_BMSAPIValidCropFromPathVariable() {

		final MockHttpServletRequest request = new MockHttpServletRequest();
		request.setRequestURI("/crops/maize/variables");

		RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

		final String crop = this.contextResolverImpl.resolveCropNameFromUrl();
		Assert.assertEquals("Crop was not resolved correctly for BMSAPI request URL.", MAIZE, crop);
		Assert.assertEquals(MAIZE, ContextHolder.getCurrentCrop());
	}

	@Test(expected = ContextResolutionException.class)
	public void testResolveCropNameFromUrl_BMSAPIInvalidCropFromPathVariable() {

		final MockHttpServletRequest request = new MockHttpServletRequest();
		request.setRequestURI("/crops/rice/variables");

		RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

		this.contextResolverImpl.resolveCropNameFromUrl();
	}

	@Test
	public void testResolveCropNameFromUrl_BMSAPIValidCropFromRequestParameter() {

		final MockHttpServletRequest request = new MockHttpServletRequest();
		request.setRequestURI("/users");
		request.setParameter("cropName", MAIZE);

		RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

		final String crop = this.contextResolverImpl.resolveCropNameFromUrl();
		Assert.assertEquals("Crop was not resolved correctly for BMSAPI request URL.", MAIZE, crop);
		Assert.assertEquals(MAIZE, ContextHolder.getCurrentCrop());
	}

	@Test(expected = ContextResolutionException.class)
	public void testResolveCropNameFromUrl_BMSAPIInvalidCropFromRequestParameter() {

		final MockHttpServletRequest request = new MockHttpServletRequest();
		request.setRequestURI("/users");
		request.setParameter("cropName", "rice");

		RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

		this.contextResolverImpl.resolveCropNameFromUrl();
	}

	@Test
	public void testResolveCropNameFromUrl_BMSAPIInstanceLevelResource() {

		final MockHttpServletRequest request = new MockHttpServletRequest();
		request.setRequestURI("/r-packages/1/calls");

		RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

		final String crop = this.contextResolverImpl.resolveCropNameFromUrl();
		Assert.assertTrue(StringUtils.isEmpty(crop));
	}

	@Test
	public void testResolveCropNameFromUrl_BMSAPINoCrop() {

		final MockHttpServletRequest request = new MockHttpServletRequest();
		request.setRequestURI("/breeding-view-licenses");

		RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

		final String crop = this.contextResolverImpl.resolveCropNameFromUrl();
		Assert.assertTrue(StringUtils.isEmpty(crop));
	}


	@Test
	public void testResolveProgramUuidFromRequest_ValidProgramFromPathVariable() {

		final MockHttpServletRequest request = new MockHttpServletRequest();
		request.setRequestURI("/crops/maize/programs/abc-123/studies/101");

		RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

		final String programUUID = this.contextResolverImpl.resolveProgramUuidFromRequest();
		Assert.assertEquals("Program UUID was not resolved correctly for request URL.", PROGRAM_UUID, programUUID);
		Assert.assertEquals(PROGRAM_UUID, ContextHolder.getCurrentProgram());
	}

	@Test(expected = ContextResolutionException.class)
	public void testResolveProgramUuidFromRequest_InvalidProgramFromPathVariable() {

		final MockHttpServletRequest request = new MockHttpServletRequest();
		request.setRequestURI("/crops/maize/programs/abc-123/studies/101");

		RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
		Mockito.doReturn(null).when(this.programService).getByUUIDAndCrop(MAIZE, PROGRAM_UUID);

		this.contextResolverImpl.resolveProgramUuidFromRequest();
	}

	@Test(expected = ContextResolutionException.class)
	public void testResolveProgramUuidFromRequest_InvalidCropFromPathVariable() {

		final MockHttpServletRequest request = new MockHttpServletRequest();
		request.setRequestURI("/crops/rice/programs/abc-123/studies/101");

		RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

		this.contextResolverImpl.resolveProgramUuidFromRequest();
	}

	@Test
	public void testResolveProgramUuidFromRequest_ValidProgramFromRequestParameter() {

		final MockHttpServletRequest request = new MockHttpServletRequest();
		request.setRequestURI("/users");
		request.setParameter("cropName", MAIZE);
		request.setParameter("programUUID", PROGRAM_UUID);

		RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

		final String programUUID = this.contextResolverImpl.resolveProgramUuidFromRequest();
		Assert.assertEquals("Program UUID was not resolved correctly for request URL.", PROGRAM_UUID, programUUID);
		Assert.assertEquals(PROGRAM_UUID, ContextHolder.getCurrentProgram());
	}

	@Test(expected = ContextResolutionException.class)
	public void testResolveProgramUuidFromRequest_InvalidCropFromRequestParameter() {

		final MockHttpServletRequest request = new MockHttpServletRequest();
		request.setRequestURI("/users");
		request.setParameter("cropName", "rice");
		request.setParameter("programUUID", PROGRAM_UUID);

		RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

		this.contextResolverImpl.resolveProgramUuidFromRequest();
	}

	@Test(expected = ContextResolutionException.class)
	public void testResolveProgramUuidFromRequest_InvalidProgramFromRequestParameter() {

		final MockHttpServletRequest request = new MockHttpServletRequest();
		request.setRequestURI("/users");
		request.setParameter("cropName", MAIZE);
		request.setParameter("programUUID", PROGRAM_UUID);

		RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
		Mockito.doReturn(null).when(this.programService).getByUUIDAndCrop(MAIZE, PROGRAM_UUID);

		this.contextResolverImpl.resolveProgramUuidFromRequest();
	}

	@Test(expected = ContextResolutionException.class)
	public void testResolveProgramUuidFromRequest_ValidProgramButNoCrop() {

		final MockHttpServletRequest request = new MockHttpServletRequest();
		request.setRequestURI("/users");
		request.setParameter("programUUID", PROGRAM_UUID);

		RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

		this.contextResolverImpl.resolveProgramUuidFromRequest();
	}


	@Test
	public void testResolveProgramUuidFromRequest_NoProgramUUID() {

		final MockHttpServletRequest request = new MockHttpServletRequest();
		request.setRequestURI("/breeding-view-licenses");

		RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

		final String programUUID = this.contextResolverImpl.resolveProgramUuidFromRequest();
		Assert.assertTrue(StringUtils.isEmpty(programUUID));
	}

}
