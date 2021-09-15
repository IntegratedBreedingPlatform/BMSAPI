package org.ibp.api.java.impl.middleware.file;

import org.ibp.api.exception.ApiRuntime2Exception;
import org.ibp.api.java.file.FileStorageService;
import org.springframework.web.multipart.MultipartFile;

public class NoneFileStorageServiceImpl implements FileStorageService {

	public static final String FILE_STORAGE_NOT_CONFIGURED_ERROR_CODE = "file.storage.not.configured";

	@Override
	public void upload(final MultipartFile file, final String path) {
		throw new ApiRuntime2Exception("", FILE_STORAGE_NOT_CONFIGURED_ERROR_CODE);
	}

	@Override
	public byte[] getFile(final String path) {
		throw new ApiRuntime2Exception("", FILE_STORAGE_NOT_CONFIGURED_ERROR_CODE);
	}

	@Override
	public boolean isConfigured() {
		return false;
	}

	@Override
	public void deleteFile(final String path) {
		throw new ApiRuntime2Exception("", FILE_STORAGE_NOT_CONFIGURED_ERROR_CODE);
	}
}
