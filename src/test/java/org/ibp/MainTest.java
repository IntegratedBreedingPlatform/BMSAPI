package org.ibp;

import com.mangofactory.swagger.configuration.SpringSwaggerConfig;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class MainTest {

	@Mock
	private SpringSwaggerConfig springSwaggerConfig;

	@InjectMocks
	private Main main;

	@Test
	public void testCustomImplementationSwaggerEnabled() {
		this.main.setEnableSwagger(true);
		Assert.assertTrue(this.main.customImplementation().isEnabled());

		this.main.setEnableSwagger(false);
		Assert.assertFalse(this.main.customImplementation().isEnabled());

	}

}
