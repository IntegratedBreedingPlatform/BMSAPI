package org.ibp.api.rest.samplesubmission;

import org.ibp.api.rest.samplesubmission.service.GOBiiAuthenticationService;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

/**
 * Created by clarysabel on 9/12/18.
 */
@Ignore
public class GobiiAuthenticationResourceTest {

	private GOBiiAuthenticationService gobiiAuthenticationService;

	@Before
	public void before() {
		gobiiAuthenticationService = new GOBiiAuthenticationService();
	}

	@Test
	public void authenticateTest() {
		gobiiAuthenticationService.authenticate("USER_READER", "reader");
	}

}
