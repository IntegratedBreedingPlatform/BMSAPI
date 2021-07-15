package org.ibp.api.java.impl.middleware.file;

import org.ibp.api.exception.ApiRuntime2Exception;
import org.ibp.api.java.file.FileStorageService;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

public class NoneFileStorageServiceImpl implements FileStorageService {

	@Override
	public void upload(final MultipartFile file, final String key) {
		throw new ApiRuntime2Exception("", "file.storage.not.configured");
	}

	@Override
	public byte[] getFile(final String key) {
		throw new ApiRuntime2Exception("", "file.storage.not.configured");
	}

	@Override
	public boolean isConfigured() {
		return false;
	}
}
