package org.ibp.api.java.file;

import org.generationcp.middleware.api.brapi.v1.image.Image;
import org.generationcp.middleware.api.brapi.v1.image.ImageNewRequest;
import org.generationcp.middleware.api.file.FileMetadataDTO;
import org.springframework.web.multipart.MultipartFile;

public interface FileMetadataService {

	Image createImage(ImageNewRequest imageNewRequest);

	Image updateImage(String imageDbId, ImageNewRequest imageNewRequest);

	Image updateImageContent(String imageDbId, byte[] imageContent);

	FileMetadataDTO save(MultipartFile file, String path, String observationUnitUUID, Integer termId);

	String getFilePath(String observationUnitUUID, Integer termId, String fileName);

	void delete(String fileUUID);
}
