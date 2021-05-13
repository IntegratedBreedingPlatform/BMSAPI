package org.ibp.api.java.impl.middleware.file;

import org.ibp.api.exception.ApiRuntimeException;
import org.ibp.api.java.file.FileStorageService;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

public class NoneFileStorageServiceImpl implements FileStorageService {

	private static final String MESSAGE = "Storage configuration not available";

	@Override
	public Map<String, String> upload(final MultipartFile file, final String key) {
		throw new ApiRuntimeException(MESSAGE);
	}

	@Override
	public Resource getFile(final String key) {
		throw new ApiRuntimeException(MESSAGE);
	}

	@Override
	public byte[] getImage(final String key) {
		throw new ApiRuntimeException(MESSAGE);
	}
}
