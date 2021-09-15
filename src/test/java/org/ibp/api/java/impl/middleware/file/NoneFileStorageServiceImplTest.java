package org.ibp.api.java.impl.middleware.file;

import org.ibp.api.exception.ApiRuntime2Exception;
import org.junit.Test;

import static org.apache.commons.lang3.RandomStringUtils.randomAlphanumeric;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public class NoneFileStorageServiceImplTest {

	private NoneFileStorageServiceImpl noneFileStorageService = new NoneFileStorageServiceImpl();

	@Test
	public void testUpload() {
		try {
			this.noneFileStorageService.upload(null, randomAlphanumeric(255));
		} catch (final ApiRuntime2Exception ex) {
			assertThat(ex.getErrorCode(), is(NoneFileStorageServiceImpl.FILE_STORAGE_NOT_CONFIGURED_ERROR_CODE));
		}
	}

	@Test
	public void testGetFile() {
		try {
			this.noneFileStorageService.getFile(randomAlphanumeric(255));
		} catch (final ApiRuntime2Exception ex) {
			assertThat(ex.getErrorCode(), is(NoneFileStorageServiceImpl.FILE_STORAGE_NOT_CONFIGURED_ERROR_CODE));
		}
	}

	@Test
	public void testIsConfigured() {
		assertThat("NoneFileStorageServiceImpl.isConfigured should be always false", this.noneFileStorageService.isConfigured(), is(false));
	}

	@Test
	public void testDeleteFile() {
		try {
			this.noneFileStorageService.deleteFile(randomAlphanumeric(255));
		} catch (final ApiRuntime2Exception ex) {
			assertThat(ex.getErrorCode(), is(NoneFileStorageServiceImpl.FILE_STORAGE_NOT_CONFIGURED_ERROR_CODE));
		}
	}

	@Test
	public void testDeleteFiles() {
		try {
			this.noneFileStorageService.deleteFile(randomAlphanumeric(255));
		} catch (final ApiRuntime2Exception ex) {
			assertThat(ex.getErrorCode(), is(NoneFileStorageServiceImpl.FILE_STORAGE_NOT_CONFIGURED_ERROR_CODE));
		}
	}
}
