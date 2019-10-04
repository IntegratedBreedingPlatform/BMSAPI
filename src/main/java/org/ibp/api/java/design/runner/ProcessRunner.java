package org.ibp.api.java.design.runner;

import java.io.IOException;

public interface ProcessRunner {

	Integer run(String... command) throws IOException;

	void setDirectory(String directory);
}
