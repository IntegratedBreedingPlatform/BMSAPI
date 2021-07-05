package org.ibp.api.java.impl.middleware.file.validator;

import org.ibp.api.exception.ApiRequestValidationException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.validation.BindingResult;
import org.springframework.validation.MapBindingResult;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.HashMap;

import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.MatcherAssert.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class FileValidatorTest {

	@InjectMocks
	private FileValidator fileValidator;

	@Test
	public void validateFile() {
		ReflectionTestUtils.setField(this.fileValidator, "supportedFileTypes", Arrays.asList("jpg","png"));
		this.fileValidator.validateFile(Mockito.mock(BindingResult.class), this.mockMultipartFile("name.jpg"));
		this.fileValidator.validateFile(Mockito.mock(BindingResult.class), this.mockMultipartFile("name.png"));

		final BindingResult errors = new MapBindingResult(new HashMap<String, String>(), String.class.getName());
		try {
			this.fileValidator.validateFile(errors, this.mockMultipartFile("name.pdf"));
		} catch (ApiRequestValidationException e) {
			assertThat(Arrays.asList(e.getErrors().get(0).getCodes()), hasItem("file.upload.not-supported"));
		}
	}

	private MultipartFile mockMultipartFile(final String name) {
		final MultipartFile mock = Mockito.mock(MultipartFile.class);
		Mockito.when(mock.getOriginalFilename()).thenReturn(name);
		return mock;
	}

}
