package org.ibp.api.java.impl.middleware.study;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.List;

import org.generationcp.middleware.domain.fieldbook.FieldMapInfo;


public class FieldMapTestUtility {
	static List<FieldMapInfo> getFieldMapInfoFromSeralizedFile(final String pathToFile) {
		FileInputStream fileIn = null;
		ObjectInputStream in = null;
		try {
			fileIn = new FileInputStream(FieldMapTestUtility.class.getResource(pathToFile).getFile());
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
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

}
