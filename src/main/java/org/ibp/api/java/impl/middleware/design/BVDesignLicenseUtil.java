package org.ibp.api.java.impl.middleware.design;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.generationcp.commons.util.DateUtil;
import org.ibp.api.domain.design.BVDesignLicenseInfo;
import org.ibp.api.exception.BVLicenseParseException;
import org.ibp.api.java.design.DesignLicenseUtil;
import org.ibp.api.java.design.runner.ProcessRunner;
import org.ibp.api.rest.design.BVDesignProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;

import javax.annotation.Resource;
import java.io.File;
import java.io.IOException;
import java.text.Format;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

@Configurable
public class BVDesignLicenseUtil implements DesignLicenseUtil {

	public static final String LICENSE_DATE_FORMAT = "dd-MMM-yyyy";
	public static final String LICENSE_SUCCESS_CODE = "0";
	private static final Logger LOG = LoggerFactory.getLogger(BVDesignLicenseUtil.class);

	public static final String BVDESIGN_STATUS_OUTPUT_FILENAME = "son";

	@Resource
	private BVDesignProperties bvDesignProperties;

	@Resource
	private MessageSource messageSource;

	private ObjectMapper objectMapper = new ObjectMapper();

	private ProcessRunner bvDesignLicenseProcessRunner = new BVDesignLicenseProcessRunner();

	@Override
	public boolean isExpired(final BVDesignLicenseInfo bvDesignLicenseInfo) {

		try {

			final Format formatter = new SimpleDateFormat(LICENSE_DATE_FORMAT);
			final Date expiryDate = (Date) formatter.parseObject(bvDesignLicenseInfo.getStatus().getLicense().getExpiry());
			final Date currentDate = DateUtil.getCurrentDateWithZeroTime();
			if (currentDate.compareTo(expiryDate) > 0) {
				return true;
			}
		} catch (final ParseException e) {
			BVDesignLicenseUtil.LOG.error(e.getMessage(), e);
		}
		return false;
	}

	@Override
	public boolean isExpiringWithinThirtyDays(final BVDesignLicenseInfo bvDesignLicenseInfo) {

		int expiryDays = 0;

		try {
			expiryDays = Integer.parseInt(bvDesignLicenseInfo.getStatus().getLicense().getExpiryDays());
		} catch (final NumberFormatException e) {

			final String errorMessage =
					this.messageSource.getMessage("bv.design.error.expiry.days.not.numeric", null, LocaleContextHolder.getLocale());
			BVDesignLicenseUtil.LOG.error(errorMessage, e);
			return true;
		}

		return expiryDays <= 30;

	}

	@Override
	public BVDesignLicenseInfo retrieveLicenseInfo() throws BVLicenseParseException {

		final String bvDesignLocation = this.bvDesignProperties.getBvDesignPath();

		this.generateBVDesignLicenseJsonFile(bvDesignLocation);

		final String jsonPathFile = new File(bvDesignLocation).getParent() + File.separator + BVDESIGN_STATUS_OUTPUT_FILENAME;

		return this.readLicenseInfoFromJsonFile(new File(jsonPathFile));
	}

	protected BVDesignLicenseInfo readLicenseInfoFromJsonFile(final File file) throws BVLicenseParseException {

		BVDesignLicenseInfo bvDesignLicenseInfo = new BVDesignLicenseInfo();

		try {

			bvDesignLicenseInfo = objectMapper.readValue(file, BVDesignLicenseInfo.class);

		} catch (final IOException e) {

			final String errorMessage =
					this.messageSource.getMessage("bv.design.error.cannot.read.license.file", null, LocaleContextHolder.getLocale());
			BVDesignLicenseUtil.LOG.error(errorMessage + ":" + e.getMessage(), e);
			throw new BVLicenseParseException(errorMessage);
		}

		if (!LICENSE_SUCCESS_CODE.equals(bvDesignLicenseInfo.getStatus().getReturnCode())) {
			final String errorMessage = this.messageSource.getMessage("bv.design.error.generic", null, LocaleContextHolder.getLocale());
			throw new BVLicenseParseException(errorMessage + bvDesignLicenseInfo.getStatus().getAppStatus());
		}

		return bvDesignLicenseInfo;

	}

	protected void generateBVDesignLicenseJsonFile(final String bvDesignLocation) throws BVLicenseParseException {

		try {

			final String bvDesignDirectory = new File(bvDesignLocation).getParent();
			bvDesignLicenseProcessRunner.setDirectory(bvDesignDirectory);
			bvDesignLicenseProcessRunner.run(bvDesignLocation, "-status", "-json");

		} catch (final Exception e) {
			final String errorMessage =
					this.messageSource.getMessage("bv.design.error.failed.license.generation", null, LocaleContextHolder.getLocale());
			BVDesignLicenseUtil.LOG.error(errorMessage + ":" + e.getMessage(), e);
			throw new BVLicenseParseException(errorMessage);
		}

	}

	public void setBvDesignLicenseProcessRunner(final BVDesignLicenseProcessRunner bvDesignLicenseProcessRunner) {
		this.bvDesignLicenseProcessRunner = bvDesignLicenseProcessRunner;
	}

	class BVDesignLicenseProcessRunner implements ProcessRunner {

		private String bvDesignDirectory = "";

		@Override
		public Integer run(final String... command) throws IOException {

			final Integer statusCode = -1;

			Process p = null;
			final ProcessBuilder processBuilder = new ProcessBuilder(command);
			processBuilder.directory(new File(bvDesignDirectory));
			p = processBuilder.start();
			try {
				return p.waitFor();
			} catch (final InterruptedException e) {
				BVDesignLicenseUtil.LOG.error(e.getMessage(), e);
			}

			return statusCode;
		}

		@Override
		public void setDirectory(final String directory) {
			this.bvDesignDirectory = directory;
		}

	}

}
