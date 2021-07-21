package org.ibp.api.java.impl.middleware.file.validator;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.ibp.api.Util;
import org.ibp.api.exception.ApiRequestValidationException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.validation.BindingResult;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Component
public class FileValidator {

	@Value("#{'${file.upload.supported.types}'.toLowerCase().split(',')}")
	private List<String> supportedFileTypes;

	public void validateFile(final BindingResult errors, final MultipartFile file) {
		if (file == null) {
			errors.reject("file.upload.required", "");
			throw new ApiRequestValidationException(errors.getAllErrors());
		}

		this.validateExtension(errors, file.getOriginalFilename());
	}

	private void validateExtension(final BindingResult errors, final String fileName) {
		final String extension = FilenameUtils.getExtension(fileName);
		if (!this.supportedFileTypes.contains(extension.toLowerCase())) {
			errors.reject("file.upload.not-supported",  new String[] {StringUtils.join(this.supportedFileTypes, ", ")}, "");
			throw new ApiRequestValidationException(errors.getAllErrors());
		}
	}

}
