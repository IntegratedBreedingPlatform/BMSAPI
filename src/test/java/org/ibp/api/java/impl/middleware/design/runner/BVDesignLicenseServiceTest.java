package org.ibp.api.java.impl.middleware.design.runner;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.generationcp.commons.util.DateUtil;
import org.ibp.api.domain.design.DesignLicenseInfo;
import org.ibp.api.domain.design.License;
import org.ibp.api.domain.design.Status;
import org.ibp.api.exception.BVLicenseParseException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class BVDesignLicenseServiceTest {

	@Mock
	private BVDesignLicenseService.BVDesignLicenseProcessRunner processRunner;

	@Mock
	private ObjectMapper objectMapper;

	@Spy
	private BVDesignLicenseService bvDesignLicenseService;

	private DesignLicenseInfo bvDesignLicenseInfo;

	@Before
	public void init() {
		MockitoAnnotations.initMocks(this);
		this.bvDesignLicenseService.setBvDesignLicenseProcessRunner(this.processRunner);
		this.bvDesignLicenseService.setObjectMapper(this.objectMapper);

		this.bvDesignLicenseInfo = this.createBVDesignLicenseInfo();
		this.bvDesignLicenseInfo.getStatus().setReturnCode("0");
		Mockito.doReturn(this.bvDesignLicenseInfo).when(this.bvDesignLicenseService).retrieveLicenseInfo();
	}

	@Test
	public void testIsExpiredWithNoDate(){
		Assert.assertFalse(this.bvDesignLicenseService.isExpired());
	}

	@Test
	public void testIsExpiredWithExpiredDate() {

		final Calendar calendar = DateUtil.getCalendarInstance();
		calendar.setTime(DateUtil.getCurrentDateWithZeroTime());
		calendar.add(Calendar.DAY_OF_WEEK, -1);

		final SimpleDateFormat df = new SimpleDateFormat(BVDesignLicenseService.LICENSE_DATE_FORMAT);
		this.bvDesignLicenseInfo.getStatus().getLicense().setExpiry(df.format(calendar.getTime()));

		Assert.assertTrue(bvDesignLicenseService.isExpired());
	}

	@Test
	public void testIsExpiredWithCurrentDate() {

		final SimpleDateFormat df = new SimpleDateFormat(BVDesignLicenseService.LICENSE_DATE_FORMAT);
		this.bvDesignLicenseInfo.getStatus().getLicense().setExpiry(df.format(DateUtil.getCurrentDateWithZeroTime()));

		Assert.assertFalse(bvDesignLicenseService.isExpired());
	}

	@Test
	public void testIsExpiredWithDateASecondAfterCurrentDate() {
		final Calendar calendar = DateUtil.getCalendarInstance();
		calendar.setTime(DateUtil.getCurrentDateWithZeroTime());
		calendar.add(Calendar.SECOND, 1);

		final SimpleDateFormat df = new SimpleDateFormat(BVDesignLicenseService.LICENSE_DATE_FORMAT);
		this.bvDesignLicenseInfo.getStatus().getLicense().setExpiry(df.format(calendar.getTime()));

		Assert.assertFalse(bvDesignLicenseService.isExpired());
	}

	@Test
	public void testGetlicenseInfo() {
		Assert.assertEquals(this.bvDesignLicenseInfo.getStatus().getLicense(), this.bvDesignLicenseService.getLicenseInfo());
	}

	@Test
	public void testReadLicenseInfoFromJsonFileSuccess() throws IOException {

		final File file = Mockito.mock(File.class);
		this.bvDesignLicenseInfo.getStatus().setReturnCode(BVDesignLicenseService.LICENSE_SUCCESS_CODE);
		Mockito.when(objectMapper.readValue(file, DesignLicenseInfo.class)).thenReturn(this.bvDesignLicenseInfo);

		try {
			this.bvDesignLicenseService.readLicenseInfoFromJsonFile(file);
		} catch (final BVLicenseParseException e) {
			Assert.fail("The method should not throw an exception");
		}
	}

	@Test
	public void testReadLicenseInfoFromJsonFile_BVReturnedErrorCode() throws IOException {

		final File file = Mockito.mock(File.class);
		// If BVDesign failed in generating the license file the return code value will be a non-zero (-1).
		final String errorStatusCode = "-1";
		final String errorMessage = "There is an error.";
		this.bvDesignLicenseInfo.getStatus().setReturnCode(errorStatusCode);
		this.bvDesignLicenseInfo.getStatus().setAppStatus(errorMessage);
		Mockito.when(objectMapper.readValue(file, DesignLicenseInfo.class)).thenReturn(this.bvDesignLicenseInfo);

		try {
			this.bvDesignLicenseService.readLicenseInfoFromJsonFile(file);
			Assert.fail("The method should throw an exception");
		} catch (final BVLicenseParseException e) {
			Assert.assertEquals("bv.design.error.generic", e.getBvErrorCode());
			Assert.assertEquals(errorMessage, e.getMessageFromApp());
		}

	}


	@Test
	public void testReadLicenseInfoFromJsonFile_IOException() throws IOException {

		final File file = Mockito.mock(File.class);
		Mockito.doThrow(new IOException()).when(objectMapper).readValue(file, DesignLicenseInfo.class);

		try {
			this.bvDesignLicenseService.readLicenseInfoFromJsonFile(file);
			Assert.fail("The method should throw an exception");
		} catch (final BVLicenseParseException e) {
			Assert.assertEquals("bv.design.error.cannot.read.license.file", e.getBvErrorCode());
			Assert.assertNull(e.getMessageFromApp());
		}

	}

	@Test
	public void testGenerateBVDesignLicenseJsonFileSuccess() throws IOException {

		final String bvDesignLocation = "parentDirectory/bvDesign";
		bvDesignLicenseService.setBvDesignPath(bvDesignLocation);
		try {
			bvDesignLicenseService.generateBVDesignLicenseJsonFile();
			Mockito.verify(this.processRunner).setDirectory("parentDirectory");
			Mockito.verify(this.processRunner).run(bvDesignLocation, "-status", "-json");
		} catch (final BVLicenseParseException e) {
			Assert.fail("generateBVDesignLicenseJsonFile should not throw a BVLicenseParseException");
		}

	}

	@Test
	public void testGenerateBVDesignLicenseJsonFileFailed() throws IOException {
		final String bvDesignLocation = "parentDirectory/bvDesign";
		bvDesignLicenseService.setBvDesignPath(bvDesignLocation);
		Mockito.when(this.processRunner.run(bvDesignLocation, "-status", "-json")).thenThrow(new IOException());

		try {
			this.bvDesignLicenseService.generateBVDesignLicenseJsonFile();
			Assert.fail("generateBVDesignLicenseJsonFile should throw a BVLicenseParseException");
		} catch (final BVLicenseParseException e) {
			Assert.assertEquals("bv.design.error.failed.license.generation", e.getBvErrorCode());
			Assert.assertNull(e.getMessageFromApp());
		}

	}

	@Test
	public void testRetrieveLicenseInfo() throws IOException {
		final BVDesignLicenseService service = new BVDesignLicenseService();
		service.setObjectMapper(this.objectMapper);
		service.setBvDesignLicenseProcessRunner(this.processRunner);
		final String bvDesignLocation = "parentDirectory/bvdesign";
		service.setBvDesignPath(bvDesignLocation);

		this.bvDesignLicenseInfo.getStatus().setReturnCode(BVDesignLicenseService.LICENSE_SUCCESS_CODE);

		Mockito.when(objectMapper.readValue(ArgumentMatchers.any(File.class), ArgumentMatchers.eq(DesignLicenseInfo.class))).thenReturn(this.bvDesignLicenseInfo);

		try {

			final DesignLicenseInfo licenseInfo = service.retrieveLicenseInfo();
			Mockito.verify(this.processRunner).setDirectory("parentDirectory");
			Mockito.verify(this.processRunner).run(bvDesignLocation, "-status", "-json");
			Mockito.verify(this.objectMapper).readValue(ArgumentMatchers.any(File.class), ArgumentMatchers.eq(DesignLicenseInfo.class));
			Assert.assertEquals(licenseInfo, bvDesignLicenseInfo);

		} catch (final BVLicenseParseException e) {
			Assert.fail("retrieveLicenseInfo should not throw a BVLicenseParseException");
		}

	}

	private DesignLicenseInfo createBVDesignLicenseInfo() {
		final DesignLicenseInfo bvDesignLicenseInfo = new DesignLicenseInfo();
		final Status status = new Status();
		final License license = new License("Succesful license checkout", "73", "31-DEC-2019");
		status.setLicense(license);
		bvDesignLicenseInfo.setStatus(status);
		return bvDesignLicenseInfo;
	}

}
