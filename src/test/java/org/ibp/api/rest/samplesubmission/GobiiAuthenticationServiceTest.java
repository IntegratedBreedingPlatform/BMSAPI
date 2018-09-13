package org.ibp.api.rest.samplesubmission;

import org.ibp.api.rest.samplesubmission.service.impl.GOBiiAuthenticationServiceImpl;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Created by clarysabel on 9/12/18.
 */
@Ignore
public class GobiiAuthenticationServiceTest {

	@Autowired
	private GOBiiAuthenticationServiceImpl gobiiAuthenticationService;

	@Before
	public void before() {
		gobiiAuthenticationService = new GOBiiAuthenticationServiceImpl();
		gobiiAuthenticationService.resolveGobiiURL();
	}

	@Test
	public void authenticateTest() {
		gobiiAuthenticationService.authenticate("USER_READER", "reader");
	}

}
