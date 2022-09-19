package org.ibp.api.java.impl.middleware.common.validator;

import org.apache.commons.lang3.RandomStringUtils;
import org.generationcp.middleware.api.brapi.v1.image.ImageNewRequest;
import org.generationcp.middleware.api.brapi.v2.germplasm.ExternalReferenceDTO;
import org.ibp.api.exception.ApiRequestValidationException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.MatcherAssert.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class ImageValidatorTest {

	@InjectMocks
	private ImageValidator imageValidator;

	@Before
	public void setUp() {
		ReflectionTestUtils.setField(this.imageValidator, "supportedFileTypes", Arrays.asList("jpg","png"));
	}

	@Test
	public void testValidateImage_Success() {
		try {
			this.imageValidator.validateImage(this.createImageNewRequest());
		} catch (final ApiRequestValidationException e) {
			Assert.fail("No error should be thrown");
		}
	}

	@Test
	public void testValidateImage_NullObservationUnitDbId() {
		try {
			final ImageNewRequest imageNewRequest= this.createImageNewRequest();
			imageNewRequest.setObservationUnitDbId(null);
			this.imageValidator.validateImage(imageNewRequest);
		} catch (final ApiRequestValidationException e) {
			assertThat(Arrays.asList(e.getErrors().get(0).getCodes()), hasItem("file.upload.brapi.images.observationunitdbid.required"));
		}
	}

	@Test
	public void testValidateImage_NullFileName() {
		try {
			final ImageNewRequest imageNewRequest= this.createImageNewRequest();
			imageNewRequest.setImageFileName(null);
			this.imageValidator.validateImage(imageNewRequest);
		} catch (final ApiRequestValidationException e) {
			assertThat(Arrays.asList(e.getErrors().get(0).getCodes()), hasItem("file.upload.brapi.images.filename.required"));
		}
	}

	@Test
	public void testValidateImage_InvalidFileExtension() {
		try {
			final ImageNewRequest imageNewRequest= this.createImageNewRequest();
			imageNewRequest.setImageFileName("name.pdf");
			this.imageValidator.validateImage(imageNewRequest);
		} catch (final ApiRequestValidationException e) {
			assertThat(Arrays.asList(e.getErrors().get(0).getCodes()), hasItem("file.upload.not-supported"));
		}
	}

	@Test
	public void testValidateImage_InvalidExternalReference() {
		try {
			final ImageNewRequest imageNewRequest= this.createImageNewRequest();
			imageNewRequest.getExternalReferences().get(0).setReferenceID(null);
			this.imageValidator.validateImage(imageNewRequest);
		} catch (final ApiRequestValidationException e) {
			assertThat(Arrays.asList(e.getErrors().get(0).getCodes()), hasItem("file.upload.reference.null"));
		}
	}

	@Test
	public void testValidateImage_ReferenceIdExceedsMaxLength() {
		try {
			final ImageNewRequest imageNewRequest= this.createImageNewRequest();
			imageNewRequest.getExternalReferences().get(0).setReferenceID(RandomStringUtils.randomAlphanumeric(2001));
			this.imageValidator.validateImage(imageNewRequest);
		} catch (final ApiRequestValidationException e) {
			assertThat(Arrays.asList(e.getErrors().get(0).getCodes()), hasItem("file.upload.reference.id.exceeded.length"));
		}
	}

	@Test
	public void testValidateImage_ReferenceSourceExceedsMaxLength() {
		try {
			final ImageNewRequest imageNewRequest= this.createImageNewRequest();
			imageNewRequest.getExternalReferences().get(0).setReferenceSource(RandomStringUtils.randomAlphanumeric(256));
			this.imageValidator.validateImage(imageNewRequest);
		} catch (final ApiRequestValidationException e) {
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
}
