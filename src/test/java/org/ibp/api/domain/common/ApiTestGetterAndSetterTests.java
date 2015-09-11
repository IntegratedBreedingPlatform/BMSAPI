
package org.ibp.api.domain.common;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.ibp.test.utilities.TestGetterAndSetter;

/**
 * A helper test to test getter and setter solely for reducing noise in the test coverage.
 *
 */
public class ApiTestGetterAndSetterTests extends TestSuite  {

	public static Test suite() {
		final TestGetterAndSetter TestGetterAndSetter = new TestGetterAndSetter();
		return TestGetterAndSetter.getTestSuite("ApiGetterAndSetterTest", "org.ibp.api");
	}

}
