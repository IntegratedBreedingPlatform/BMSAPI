package org.ibp.api.java.file;

import org.springframework.web.multipart.MultipartFile;

public interface FileStorageService {

	void upload(MultipartFile file, String path);

	byte[] getFile(String path);

	/**
	 * @return true if a file storage service is configured correctly. {@link org.ibp.api.java.impl.middleware.common.FileStorageServiceFactory}
	 * will return {@link org.ibp.api.java.impl.middleware.file.NoneFileStorageServiceImpl} if properties are not set.
	 * Use this method instead of instanceof because of spring proxies.
	 */
	boolean isConfigured();

	void deleteFile(String path);
}
