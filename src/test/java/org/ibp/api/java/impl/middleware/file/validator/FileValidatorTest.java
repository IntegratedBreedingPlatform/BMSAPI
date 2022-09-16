package org.ibp.api.java.impl.middleware.file.validator;

import org.apache.commons.lang3.RandomStringUtils;
import org.generationcp.middleware.api.brapi.v1.image.ImageNewRequest;
import org.generationcp.middleware.api.brapi.v2.germplasm.ExternalReferenceDTO;
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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
		} catch (ApiRequestValidationException e) {
			assertThat(Arrays.asList(e.getErrors().get(0).getCodes()), hasItem("file.upload.not-supported"));
		}
	}

	@Test
	public void testValidateFileStorage_Success() {
		try {
			Mockito.when(this.fileStorageService.isConfigured()).thenReturn(true);
			this.fileValidator.validateFileStorage();
		} catch (ApiRequestValidationException e) {
			Assert.fail("No error should be thrown");
		}
	}

	@Test
	public void testValidateFileStorage_UnconfiguredFileStorage() {
		try {
			this.fileValidator.validateFileStorage();
		} catch (ApiRequestValidationException e) {
			assertThat(Arrays.asList(e.getErrors().get(0).getCodes()), hasItem("file.storage.not.configured"));
		}
	}

	@Test
	public void testValidateImage_Success() {
		try {
			this.fileValidator.validateImage(this.createImageNewRequest());
		} catch (ApiRequestValidationException e) {
			Assert.fail("No error should be thrown");
		}
	}

	@Test
	public void testValidateImage_NullObservationUnitDbId() {
		try {
			final ImageNewRequest imageNewRequest= this.createImageNewRequest();
			imageNewRequest.setObservationUnitDbId(null);
			this.fileValidator.validateImage(imageNewRequest);
		} catch (ApiRequestValidationException e) {
			assertThat(Arrays.asList(e.getErrors().get(0).getCodes()), hasItem("file.upload.brapi.images.observationunitdbid.required"));
		}
	}

	@Test
	public void testValidateImage_NullFileName() {
		try {
			final ImageNewRequest imageNewRequest= this.createImageNewRequest();
			imageNewRequest.setImageFileName(null);
			this.fileValidator.validateImage(imageNewRequest);
		} catch (ApiRequestValidationException e) {
			assertThat(Arrays.asList(e.getErrors().get(0).getCodes()), hasItem("file.upload.brapi.images.filename.required"));
		}
	}

	@Test
	public void testValidateImage_InvalidFileExtension() {
		try {
			final ImageNewRequest imageNewRequest= this.createImageNewRequest();
			imageNewRequest.setImageFileName("name.pdf");
			this.fileValidator.validateImage(imageNewRequest);
		} catch (ApiRequestValidationException e) {
			assertThat(Arrays.asList(e.getErrors().get(0).getCodes()), hasItem("file.upload.not-supported"));
		}
	}

	@Test
	public void testValidateImage_InvalidExternalReference() {
		try {
			final ImageNewRequest imageNewRequest= this.createImageNewRequest();
			imageNewRequest.getExternalReferences().get(0).setReferenceID(null);
			this.fileValidator.validateImage(imageNewRequest);
		} catch (ApiRequestValidationException e) {
			assertThat(Arrays.asList(e.getErrors().get(0).getCodes()), hasItem("file.upload.reference.null"));
		}
	}

	@Test
	public void testValidateImage_ReferenceIdExceedsMaxLength() {
		try {
			final ImageNewRequest imageNewRequest= this.createImageNewRequest();
			imageNewRequest.getExternalReferences().get(0).setReferenceID(RandomStringUtils.randomAlphanumeric(2001));
			this.fileValidator.validateImage(imageNewRequest);
		} catch (ApiRequestValidationException e) {
			assertThat(Arrays.asList(e.getErrors().get(0).getCodes()), hasItem("file.upload.reference.id.exceeded.length"));
		}
	}

	@Test
	public void testValidateImage_ReferenceSourceExceedsMaxLength() {
		try {
			final ImageNewRequest imageNewRequest= this.createImageNewRequest();
			imageNewRequest.getExternalReferences().get(0).setReferenceSource(RandomStringUtils.randomAlphanumeric(256));
			this.fileValidator.validateImage(imageNewRequest);
		} catch (ApiRequestValidationException e) {
			assertThat(Arrays.asList(e.getErrors().get(0).getCodes()), hasItem("file.upload.reference.source.exceeded.length"));
		}
	}

	private ImageNewRequest createImageNewRequest() {
		final ImageNewRequest imageNewRequest = new ImageNewRequest();
		imageNewRequest.setImageFileName("image.jpg");
		imageNewRequest.setObservationUnitDbId(RandomStringUtils.randomAlphanumeric(5));
		final List<ExternalReferenceDTO> externalReferenceDTOs = new ArrayList<>();
		final ExternalReferenceDTO externalReferenceDTO = new ExternalReferenceDTO(RandomStringUtils.randomNumeric(1),
			RandomStringUtils.randomAlphanumeric(10), RandomStringUtils.randomAlphanumeric(10));
		externalReferenceDTOs.add(externalReferenceDTO);
		imageNewRequest.setExternalReferences(externalReferenceDTOs);
		return imageNewRequest;
	}

	private MultipartFile mockMultipartFile(final String name) {
		final MultipartFile mock = Mockito.mock(MultipartFile.class);
		Mockito.when(mock.getOriginalFilename()).thenReturn(name);
		return mock;
	}

}
