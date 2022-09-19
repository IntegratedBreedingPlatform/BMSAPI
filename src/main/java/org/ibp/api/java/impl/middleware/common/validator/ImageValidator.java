package org.ibp.api.java.impl.middleware.common.validator;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.generationcp.middleware.api.brapi.v1.image.ImageNewRequest;
import org.generationcp.middleware.api.brapi.v2.germplasm.ExternalReferenceDTO;
import org.ibp.api.exception.ApiRequestValidationException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.validation.BindingResult;
import org.springframework.validation.MapBindingResult;

import java.util.HashMap;
import java.util.List;

import static org.ibp.api.java.impl.middleware.common.validator.BaseValidator.checkNotNull;

@Component
public class ImageValidator {

	private static final int MAX_REFERENCE_ID_LENGTH = 2000;
	private static final int MAX_REFERENCE_SOURCE_LENGTH = 255;

	@Value("#{'${file.upload.supported.types}'.toLowerCase().split(',')}")
	private List<String> supportedFileTypes;

	protected BindingResult errors;

	public void validateExtension(final String fileName) {
		final String extension = FilenameUtils.getExtension(fileName);
		if (!this.supportedFileTypes.contains(extension.toLowerCase())) {
			this.errors.reject("file.upload.not-supported",  new String[] {StringUtils.join(this.supportedFileTypes, ", ")}, "");
			throw new ApiRequestValidationException(this.errors.getAllErrors());
		}
	}

	public void validateImage(final ImageNewRequest body) {
		this.errors = new MapBindingResult(new HashMap<>(), String.class.getName());
		checkNotNull(body.getObservationUnitDbId(), "file.upload.brapi.images.observationunitdbid.required");
		final String imageFileName = body.getImageFileName();
		checkNotNull(imageFileName, "file.upload.brapi.images.filename.required");
		this.validateExtension(imageFileName);
		this.validateExternalReferences(body.getExternalReferences());
	}

	private void validateExternalReferences(final List<ExternalReferenceDTO> externalReferenceDTOS) {
		if (!CollectionUtils.isEmpty(externalReferenceDTOS)) {
			for(final ExternalReferenceDTO externalReferenceDTO: externalReferenceDTOS) {
				if (externalReferenceDTO == null || StringUtils.isEmpty(externalReferenceDTO.getReferenceID()) || StringUtils.isEmpty(externalReferenceDTO.getReferenceSource())) {
					this.errors.reject("file.upload.reference.null","");
				}
				if (StringUtils.isNotEmpty(externalReferenceDTO.getReferenceID()) && externalReferenceDTO.getReferenceID().length() > MAX_REFERENCE_ID_LENGTH) {
					this.errors.reject("file.upload.reference.id.exceeded.length", "");
				}
				if (StringUtils.isNotEmpty(externalReferenceDTO.getReferenceSource()) && externalReferenceDTO.getReferenceSource().length() > MAX_REFERENCE_SOURCE_LENGTH) {
					this.errors.reject("file.upload.reference.source.exceeded.length", "");
				}
			}
			if (this.errors.hasErrors()) {
				throw new ApiRequestValidationException(this.errors.getAllErrors());
			}
		}
	}

}
