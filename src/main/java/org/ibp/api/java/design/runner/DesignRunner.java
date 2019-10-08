
package org.ibp.api.java.design.runner;

import org.ibp.api.domain.design.BVDesignOutput;
import org.ibp.api.rest.design.BVDesignProperties;
import org.ibp.api.domain.design.MainDesign;

import java.io.IOException;

public interface DesignRunner {

	public BVDesignOutput runBVDesign(MainDesign design)
			throws IOException;

}
