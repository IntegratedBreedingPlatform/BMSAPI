package org.ibp.api.java.impl.middleware.file.validator;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.ibp.api.exception.ApiRequestValidationException;
import org.ibp.api.java.file.FileStorageService;
import org.ibp.api.java.impl.middleware.common.validator.BaseValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.validation.BindingResult;
import org.springframework.validation.MapBindingResult;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;

@Component
public class FileValidator {
	@Autowired
	private FileStorageService fileStorageService;

	@Value("#{'${file.upload.supported.types}'.toLowerCase().split(',')}")
	private List<String> supportedFileTypes;

	protected BindingResult errors;

	public void validateFile(final MultipartFile file) {
		this.errors = new MapBindingResult(new HashMap<>(), String.class.getName());
		if (file == null) {
			this.errors.reject("file.upload.required", "");
			throw new ApiRequestValidationException(this.errors.getAllErrors());
		}

		this.validateExtension(file.getOriginalFilename());
	}

	public void validateExtension(final String fileName) {
		final String extension = FilenameUtils.getExtension(fileName);
		if (!this.supportedFileTypes.contains(extension.toLowerCase())) {
			this.errors.reject("file.upload.not-supported",  new String[] {StringUtils.join(this.supportedFileTypes, ", ")}, "");
			throw new ApiRequestValidationException(this.errors.getAllErrors());
		}
	}

	public void validateFileStorage() {
		BaseValidator.checkArgument(this.fileStorageService.isConfigured(), "file.storage.not.configured");
	}
}
