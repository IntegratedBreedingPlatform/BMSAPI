package org.ibp.api.java.impl.middleware.design.runner;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;
import org.generationcp.commons.util.DateUtil;
import org.ibp.api.domain.design.DesignLicenseInfo;
import org.ibp.api.exception.BVLicenseParseException;
import org.ibp.api.java.design.DesignLicenseService;
import org.ibp.api.java.design.runner.ProcessRunner;
import org.ibp.api.rest.design.BVDesignProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.File;
import java.io.IOException;
import java.text.Format;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

@Component
@ConditionalOnProperty(
	value = "design.runner.license.service",
	havingValue = "org.ibp.api.java.impl.middleware.design.runner.BVDesignLicenseService")
public class BVDesignLicenseService implements DesignLicenseService {

	static final String LICENSE_DATE_FORMAT = "dd-MMM-yyyy";
	static final String LICENSE_SUCCESS_CODE = "0";
	private static final Logger LOG = LoggerFactory.getLogger(BVDesignLicenseService.class);

	private static final String BVDESIGN_STATUS_OUTPUT_FILENAME = "son";

	@Resource
	private BVDesignProperties bvDesignProperties;

	private ObjectMapper objectMapper = new ObjectMapper();

	private ProcessRunner bvDesignLicenseProcessRunner = new BVDesignLicenseProcessRunner();

	@Override
	public boolean isExpired() {

		try {
			final DesignLicenseInfo designLicenseInfo = this.retrieveLicenseInfo();
			final Format formatter = new SimpleDateFormat(LICENSE_DATE_FORMAT);
			final String expiry = designLicenseInfo.getStatus().getLicense().getExpiry();
			if (!StringUtils.isEmpty(expiry)) {
				final Date expiryDate = (Date) formatter.parseObject(expiry);
				final Date currentDate = DateUtil.getCurrentDateWithZeroTime();
				if (currentDate.compareTo(expiryDate) > 0) {
					return true;
				}
			}
		} catch (final ParseException e) {
			BVDesignLicenseService.LOG.error(e.getMessage(), e);
		}
		return false;
	}

	DesignLicenseInfo retrieveLicenseInfo() throws BVLicenseParseException {

		final String bvDesignLocation = this.bvDesignProperties.getBvDesignPath();

		this.generateBVDesignLicenseJsonFile(bvDesignLocation);

		final String jsonPathFile = new File(bvDesignLocation).getParent() + File.separator + BVDESIGN_STATUS_OUTPUT_FILENAME;

		return this.readLicenseInfoFromJsonFile(new File(jsonPathFile));
	}

	DesignLicenseInfo readLicenseInfoFromJsonFile(final File file) {

		DesignLicenseInfo designLicenseInfo;

		try {

			designLicenseInfo = objectMapper.readValue(file, DesignLicenseInfo.class);

		} catch (final IOException e) {
			BVDesignLicenseService.LOG.error(e.getMessage(), e);
			throw new BVLicenseParseException("bv.design.error.cannot.read.license.file");
		}

		if (designLicenseInfo.getStatus() != null && !LICENSE_SUCCESS_CODE.equals(designLicenseInfo.getStatus().getReturnCode())) {
			throw new BVLicenseParseException("bv.design.error.generic", designLicenseInfo.getStatus().getAppStatus());
		}

		return designLicenseInfo;

	}

	void generateBVDesignLicenseJsonFile(final String bvDesignLocation) throws BVLicenseParseException {

		try {

			final String bvDesignDirectory = new File(bvDesignLocation).getParent();
			bvDesignLicenseProcessRunner.setDirectory(bvDesignDirectory);
			bvDesignLicenseProcessRunner.run(bvDesignLocation, "-status", "-json");

		} catch (final Exception e) {
			BVDesignLicenseService.LOG.error(e.getMessage(), e);
			throw new BVLicenseParseException("bv.design.error.failed.license.generation");
		}

	}

	void setBvDesignLicenseProcessRunner(final BVDesignLicenseProcessRunner bvDesignLicenseProcessRunner) {
		this.bvDesignLicenseProcessRunner = bvDesignLicenseProcessRunner;
	}

	void setBvDesignProperties(final BVDesignProperties bvDesignProperties) {
		this.bvDesignProperties = bvDesignProperties;
	}

	void setObjectMapper(final ObjectMapper objectMapper) {
		this.objectMapper = objectMapper;
	}

	@Override
	public Integer getExpiryDays() {
		final DesignLicenseInfo designLicenseInfo = this.retrieveLicenseInfo();
		return Integer.parseInt(designLicenseInfo.getStatus().getLicense().getExpiryDays());
	}

	class BVDesignLicenseProcessRunner implements ProcessRunner {

		private String bvDesignDirectory = "";

		@Override
		public Integer run(final String... command) throws IOException {

			final Integer statusCode = -1;

			final ProcessBuilder processBuilder = new ProcessBuilder(command);
			processBuilder.directory(new File(bvDesignDirectory));
			final Process p = processBuilder.start();
			try {
				return p.waitFor();
			} catch (final InterruptedException e) {
				BVDesignLicenseService.LOG.error(e.getMessage(), e);
			}

			return statusCode;
		}

		@Override
		public void setDirectory(final String directory) {
			this.bvDesignDirectory = directory;
		}

	}

}
