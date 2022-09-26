package org.ibp.api.java.impl.middleware.file.validator;

import org.ibp.api.exception.ApiRequestValidationException;
import org.ibp.api.java.file.FileStorageService;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;

import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.MatcherAssert.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class FileValidatorTest {

	@Mock
	private FileStorageService fileStorageService;

	@InjectMocks
	private FileValidator fileValidator;

	@Before
	public void setUp() {
		ReflectionTestUtils.setField(this.fileValidator, "supportedFileTypes", Arrays.asList("jpg","png"));
	}

	@Test
	public void validateFile() {
		this.fileValidator.validateFile(this.mockMultipartFile("name.png"));

		try {
			this.fileValidator.validateFile(this.mockMultipartFile("name.pdf"));
		} catch (final ApiRequestValidationException e) {
			assertThat(Arrays.asList(e.getErrors().get(0).getCodes()), hasItem("file.upload.not-supported"));
		}
	}

	@Test
	public void testValidateFileStorage_Success() {
		try {
			Mockito.when(this.fileStorageService.isConfigured()).thenReturn(true);
			this.fileValidator.validateFileStorage();
		} catch (final ApiRequestValidationException e) {
			Assert.fail("No error should be thrown");
		}
	}

	@Test
	public void testValidateFileStorage_UnconfiguredFileStorage() {
		try {
			this.fileValidator.validateFileStorage();
		} catch (final ApiRequestValidationException e) {
			assertThat(Arrays.asList(e.getErrors().get(0).getCodes()), hasItem("file.storage.not.configured"));
		}
	}

	private MultipartFile mockMultipartFile(final String name) {
		final MultipartFile mock = Mockito.mock(MultipartFile.class);
		Mockito.when(mock.getOriginalFilename()).thenReturn(name);
		return mock;
	}

}
