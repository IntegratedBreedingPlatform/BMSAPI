package org.ibp.api.java.impl.middleware.common;

import org.ibp.api.java.impl.middleware.file.AWSS3FileStorageServiceImpl;
import org.ibp.api.java.impl.middleware.file.NoneFileStorageServiceImpl;
import org.ibp.api.java.impl.middleware.file.SFTPFileStorageServiceImpl;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.test.util.ReflectionTestUtils;

import static org.hamcrest.Matchers.instanceOf;

@RunWith(MockitoJUnitRunner.class)
public class FileStorageServiceFactoryTest {

	private FileStorageServiceFactory fileStorageServiceFactory = new FileStorageServiceFactory();

	@Test
	public void testGetFileStorageService_None() {
		Assert.assertThat(this.fileStorageServiceFactory.getFileStorageService(), instanceOf(NoneFileStorageServiceImpl.class));
	}

	@Test
	public void testGetFileStorageService_InvalidConf() {
		ReflectionTestUtils.setField(this.fileStorageServiceFactory, "host", "host");
		ReflectionTestUtils.setField(this.fileStorageServiceFactory, "privateKeyPath", "privateKeyPath");
		Assert.assertThat(this.fileStorageServiceFactory.getFileStorageService(), instanceOf(NoneFileStorageServiceImpl.class));
	}

	@Test
	public void testGetFileStorageService_InvalidConf2() {
		ReflectionTestUtils.setField(this.fileStorageServiceFactory, "bucketName", "testbucket");
		ReflectionTestUtils.setField(this.fileStorageServiceFactory, "accessKey", "myaccessKey");
		ReflectionTestUtils.setField(this.fileStorageServiceFactory, "region", "myregion");
		Assert.assertThat(this.fileStorageServiceFactory.getFileStorageService(), instanceOf(NoneFileStorageServiceImpl.class));
	}

	@Test
	public void testGetFileStorageService_AWS() {
		ReflectionTestUtils.setField(this.fileStorageServiceFactory, "bucketName", "testbucket");
		ReflectionTestUtils.setField(this.fileStorageServiceFactory, "accessKey", "myaccessKey");
		ReflectionTestUtils.setField(this.fileStorageServiceFactory, "secretKey", "mysecretKey");
		ReflectionTestUtils.setField(this.fileStorageServiceFactory, "region", "myregion");
		Assert.assertThat(this.fileStorageServiceFactory.getFileStorageService(), instanceOf(AWSS3FileStorageServiceImpl.class));
	}

	@Test
	public void testGetFileStorageService_SFTP() {
		ReflectionTestUtils.setField(this.fileStorageServiceFactory, "host", "host");
		ReflectionTestUtils.setField(this.fileStorageServiceFactory, "username", "username");
		ReflectionTestUtils.setField(this.fileStorageServiceFactory, "password", "password");
		Assert.assertThat(this.fileStorageServiceFactory.getFileStorageService(), instanceOf(SFTPFileStorageServiceImpl.class));
	}

	@Test
	public void testGetFileStorageService_SFTP_PrivateKey() {
		ReflectionTestUtils.setField(this.fileStorageServiceFactory, "host", "host");
		ReflectionTestUtils.setField(this.fileStorageServiceFactory, "username", "username");
		ReflectionTestUtils.setField(this.fileStorageServiceFactory, "privateKeyPath", "privateKeyPath");
		Assert.assertThat(this.fileStorageServiceFactory.getFileStorageService(), instanceOf(SFTPFileStorageServiceImpl.class));
	}
}
