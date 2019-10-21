package org.ibp.api.java.impl.middleware.design.runner;

import au.com.bytecode.opencsv.CSVReader;
import org.generationcp.commons.pojo.ProcessTimeoutThread;
import org.ibp.api.domain.design.BVDesignOutput;
import org.ibp.api.domain.design.MainDesign;
import org.ibp.api.java.design.runner.DesignRunner;
import org.ibp.api.java.design.runner.ProcessRunner;
import org.ibp.api.java.impl.middleware.design.generator.ExperimentDesignGenerator;
import org.ibp.api.java.impl.middleware.design.util.ExperimentalDesignUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import javax.xml.bind.JAXBException;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.util.List;

@Component
@ConditionalOnProperty(
	value = "design.runner",
	havingValue = "org.ibp.api.java.impl.middleware.design.runner.BVDesignRunner")
public class BVDesignRunner implements DesignRunner {

	public static final String BV_PREFIX = "-bv";
	public static final String CSV_EXTENSION = ".csv";

	private static final Logger LOG = LoggerFactory.getLogger(BVDesignRunner.class);
	private static final String XML_EXTENSION = ".xml";

	private ProcessRunner processRunner = new BVDesignProcessRunner();
	private BVDesignOutputReader outputReader = new BVDesignOutputReader();
	private BVDesignXmlInputWriter inputWriter = new BVDesignXmlInputWriter();

	@Value("upload.directory")
	private String uploadDirectory;

	@Value("bv.design.runner.timeout")
	private Integer bvDesignRunnerTimeout;

	@Value("bv.design.path")
	private String bvDesignPath;

	@Override
	public BVDesignOutput runBVDesign(final MainDesign design) throws IOException {

		int returnCode = -1;

		if (bvDesignPath != null && design != null && design.getDesign() != null) {

			final String xml = this.getXMLStringForDesign(design);

			final String filepath = this.inputWriter.write(xml);

			returnCode = this.processRunner.run(bvDesignPath, "-i" + filepath);
		}

		final BVDesignOutput output = new BVDesignOutput(returnCode);

		if (returnCode == 0) {
			output.setResults(this.outputReader.read(design.getDesign().getParameterValue(ExperimentDesignGenerator.OUTPUTFILE_PARAM)));
		}

		return output;
	}

	public String getXMLStringForDesign(final MainDesign design) {
		String xml = "";
		final Long currentTimeMillis = System.currentTimeMillis();
		final String outputFilePath = currentTimeMillis + BVDesignRunner.BV_PREFIX + BVDesignRunner.CSV_EXTENSION;

		design.getDesign().setParameterValue(ExperimentDesignGenerator.OUTPUTFILE_PARAM, outputFilePath);
		design.getDesign().setParameterValue(ExperimentDesignGenerator.SEED_PARAM, this.getSeedValue(currentTimeMillis));

		try {
			xml = ExperimentalDesignUtil.getXmlStringForSetting(design);
		} catch (final JAXBException e) {
			BVDesignRunner.LOG.error(e.getMessage(), e);
		}
		return xml;
	}

	private String getSeedValue(final Long currentTimeMillis) {
		String seedValue = Long.toString(currentTimeMillis);
		if (currentTimeMillis > Integer.MAX_VALUE) {
			seedValue = seedValue.substring(seedValue.length() - 9);
		}
		return seedValue;
	}

	private static String generateBVFileName(final String extensionFileName) {
		return System.currentTimeMillis() + BVDesignRunner.BV_PREFIX + extensionFileName;
	}

	public void setProcessRunner(final BVDesignProcessRunner processRunner) {
		this.processRunner = processRunner;
	}

	public void setOutputReader(final BVDesignOutputReader outputReader) {
		this.outputReader = outputReader;
	}

	public void setInputWriter(final BVDesignXmlInputWriter inputWriter) {
		this.inputWriter = inputWriter;
	}

	public class BVDesignProcessRunner implements ProcessRunner {

		@Override
		public Integer run(final String... command) throws IOException {

			final Integer returnCode = -1;

			final ProcessBuilder pb = new ProcessBuilder(command);
			final Process p = pb.start();
			// add a timeout for the design runner
			final long bvDesignRunnerTimeout = 60 * 1000 * Long.valueOf(BVDesignRunner.this.bvDesignRunnerTimeout);
			final ProcessTimeoutThread processTimeoutThread = new ProcessTimeoutThread(p, bvDesignRunnerTimeout);
			processTimeoutThread.start();
			try {
				final InputStreamReader isr = new InputStreamReader(p.getInputStream());
				final BufferedReader br = new BufferedReader(isr);

				String lineRead;
				while ((lineRead = br.readLine()) != null) {
					BVDesignRunner.LOG.debug(lineRead);
				}

				return p.waitFor();
			} catch (final InterruptedException e) {
				BVDesignRunner.LOG.error(e.getMessage(), e);
			} finally {
				if (processTimeoutThread != null) {
					// Stop the thread if it's still running
					processTimeoutThread.interrupt();
				}
				if (p != null) {
					// missing these was causing the mass amounts of open 'files'
					p.getInputStream().close();
					p.getOutputStream().close();
					p.getErrorStream().close();
				}
			}

			return returnCode;

		}

		@Override
		public void setDirectory(final String directory) {
			// do nothing
		}
	}


	public static class BVDesignOutputReader {

		public List<String[]> read(final String filePath) throws IOException {

			final File outputFile = new File(filePath);
			final FileReader fileReader = new FileReader(outputFile);
			final List<String[]> myEntries;
			try (final CSVReader reader = new CSVReader(fileReader)) {
				myEntries = reader.readAll();
				fileReader.close();
			}
			Files.delete(outputFile.toPath());
			return myEntries;

		}

	}


	public class BVDesignXmlInputWriter {

		public String write(final String xml) {

			final String filename = BVDesignRunner.generateBVFileName(BVDesignRunner.XML_EXTENSION);
			final String path = BVDesignRunner.this.uploadDirectory + File.separator + filename;
			final File f = new File(path);
			String filenamePath = f.getAbsolutePath();
			try {

				final File file = new File(filenamePath);
				try (final BufferedWriter output = new BufferedWriter(new FileWriter(file))) {
					output.write(xml);
				}
				filenamePath = file.getAbsolutePath();
			} catch (final IOException e) {
				BVDesignRunner.LOG.error(e.getMessage(), e);
			}

			return filenamePath;
		}

	}

	public void setUploadDirectory(final String uploadDirectory) {
		this.uploadDirectory = uploadDirectory;
	}

	public void setBvDesignRunnerTimeout(final Integer bvDesignRunnerTimeout) {
		this.bvDesignRunnerTimeout = bvDesignRunnerTimeout;
	}

	public void setBvDesignPath(final String bvDesignPath) {
		this.bvDesignPath = bvDesignPath;
	}
}
