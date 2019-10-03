
package org.ibp.api.java.design;

import org.ibp.api.domain.design.BVDesignOutput;
import org.ibp.api.rest.design.BVDesignProperties;
import org.ibp.api.domain.design.MainDesign;

import java.io.IOException;

public interface DesignRunner {

	public BVDesignOutput runBVDesign(BVDesignProperties bvDesignProperties, MainDesign design)
			throws IOException;

}
