
package org.ibp.api.ibpworkbench.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class ObjectUtil<T> {

	private static final Logger LOG = LoggerFactory.getLogger(ObjectUtil.class);

	public void serializeObject(T o, String filename) {

		// save the object to file
		FileOutputStream fos = null;
		ObjectOutputStream out = null;
		try {
			fos = new FileOutputStream(filename);
			out = new ObjectOutputStream(fos);
			out.writeObject(o);

			out.close();
		} catch (IOException ex) {
			ObjectUtil.LOG.error("Error: ", ex);
		}

	}

	public T deserializeFromFile(String filename) {

		FileInputStream fis = null;
		ObjectInputStream in = null;
		T object = null;

		try {
			fis = new FileInputStream(filename);
			in = new ObjectInputStream(fis);
			object = (T) in.readObject();
			in.close();
		} catch (Exception ex) {
			ObjectUtil.LOG.error("Error: ", ex);
		}

		return object;
	}

}
