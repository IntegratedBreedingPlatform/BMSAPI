package org.ibp.api.java.impl.middleware.file;

import org.junit.Test;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public class AWSS3FileStorageServiceImplTest {

	private AWSS3FileStorageServiceImpl awss3FileStorageService = new AWSS3FileStorageServiceImpl();

	@Test
	public void testIsConfigured() {
		assertThat(this.awss3FileStorageService.isConfigured(), is(true));
	}
}
