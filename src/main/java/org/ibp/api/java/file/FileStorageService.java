package org.ibp.api.java.file;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

public interface FileStorageService {

	Map<String, String> upload(MultipartFile file, String key);

	Resource getFile(String key);

	byte[] getImage(String key);
}
