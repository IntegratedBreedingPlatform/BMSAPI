package org.ibp.api.java.impl.middleware.design;

import au.com.bytecode.opencsv.CSVReader;
import org.ibp.api.domain.design.BVDesignOutput;
import org.ibp.api.rest.design.BVDesignProperties;
import org.ibp.api.domain.design.MainDesign;
import org.ibp.api.java.design.DesignRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

public class BVDesignRunner implements DesignRunner {

	public static final String BV_PREFIX = "-bv";
	public static final String CSV_EXTENSION = ".csv";

	private static final Logger LOG = LoggerFactory.getLogger(BVDesignRunner.class);
	private static final String XML_EXTENSION = ".xml";

	private BVDesignOutputReader outputReader = new BVDesignOutputReader();
	private BVDesignXmlInputWriter inputWriter = new BVDesignXmlInputWriter();
	private static long bvDesignRunnerTimeout;

	@Override
	public BVDesignOutput runBVDesign(final BVDesignProperties bvDesignProperties,
			final MainDesign design) throws IOException {
		/**
		 * TODO:
		 * 1. IBP-3123 Run BV Design and get the design output
		 */
		int returnCode = -1;
		final BVDesignOutput output = new BVDesignOutput(returnCode);

		return output;
	}

	private String getSeedValue(final Long currentTimeMillis) {
		String seedValue = Long.toString(currentTimeMillis);
		if (currentTimeMillis > Integer.MAX_VALUE) {
			seedValue = seedValue.substring(seedValue.length() - 9);
		}
		return seedValue;
	}

	private static String generateBVFilePath(final String extensionFilename, final BVDesignProperties bvDesignProperties) {
		final String filename = BVDesignRunner.generateBVFileName(extensionFilename);
		final String filenamePath = bvDesignProperties.getUploadDirectory() + File.separator + filename;
		final File f = new File(filenamePath);
		return f.getAbsolutePath();
	}

	private static String generateBVFileName(final String extensionFileName) {
		return System.currentTimeMillis() + BVDesignRunner.BV_PREFIX + extensionFileName;
	}

	public void setOutputReader(final BVDesignOutputReader outputReader) {
		this.outputReader = outputReader;
	}

	public void setInputWriter(final BVDesignXmlInputWriter inputWriter) {
		this.inputWriter = inputWriter;
	}


	public class BVDesignOutputReader {

		public List<String[]> read(final String filePath) throws IOException {

			final File outputFile = new File(filePath);
			final FileReader fileReader = new FileReader(outputFile);
			final CSVReader reader = new CSVReader(fileReader);
			final List<String[]> myEntries = reader.readAll();

			fileReader.close();
			reader.close();
			outputFile.delete();

			return myEntries;

		}

	}


	public class BVDesignXmlInputWriter {

		public String write(final String xml, final BVDesignProperties bvDesignProperties) {

			String filenamePath = BVDesignRunner.generateBVFilePath(BVDesignRunner.XML_EXTENSION, bvDesignProperties);
			try {

				final File file = new File(filenamePath);
				final BufferedWriter output = new BufferedWriter(new FileWriter(file));
				output.write(xml);
				output.close();
				filenamePath = file.getAbsolutePath();
			} catch (final IOException e) {
				BVDesignRunner.LOG.error(e.getMessage(), e);
			}

			return filenamePath;
		}

	}

}
