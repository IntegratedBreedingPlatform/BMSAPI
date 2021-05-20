package org.ibp.api.java.file;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

public interface FileStorageService {

	Map<String, String> upload(MultipartFile file, String key);

	byte[] getFile(String key);

	/**
	 * @return true if a file storage service is configured correctly. {@link org.ibp.api.java.impl.middleware.common.FileServiceFactory}
	 * will return {@link org.ibp.api.java.impl.middleware.file.NoneFileStorageServiceImpl} if properties are not set.
	 * Use this method instead of instanceof because of spring proxies.
	 */
	boolean isConfigured();
}
