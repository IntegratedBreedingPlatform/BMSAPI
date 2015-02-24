package org.generationcp.ibpworkbench.service;

import org.junit.Before;
import org.mockito.MockitoAnnotations;

import static org.mockito.Mockito.*;

public class BreedingViewServiceTest {
	

	BreedingViewServiceImpl service;
	
	
	@Before
	public void setUp() throws Exception {
		
		MockitoAnnotations.initMocks(this);
		service = spy(new BreedingViewServiceImpl());

	}


}
