package org.ibp.api.java.impl.middleware.study;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.URISyntaxException;
import java.util.List;

import org.generationcp.middleware.domain.fieldbook.FieldMapInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FieldMapTestUtility {

	private static final Logger LOGGER = LoggerFactory.getLogger(FieldMapTestUtility.class);

	static List<FieldMapInfo> getFieldMapInfoFromSerializedFile(final String pathToFile) throws URISyntaxException {
		FileInputStream fileIn = null;
		ObjectInputStream in = null;
		try {
			fileIn = new FileInputStream(new File(FieldMapTestUtility.class.getClass().getResource(pathToFile).toURI()));
			in = new ObjectInputStream(fileIn);
			@SuppressWarnings("unchecked")
			List<FieldMapInfo> readObject = (List<FieldMapInfo>) in.readObject();
			in.close();
			fileIn.close();
			return readObject;

		} catch (IOException | ClassNotFoundException e) {
			throw new RuntimeException();
		} finally {
			try {
				in.close();
				fileIn.close();
			} catch (IOException e) {
				LOGGER.error("Error while executing getFieldMapInfoFromSerializedFile", e);
			}
		}
	}

}
