package org.ibp.api.java.file;

import org.generationcp.middleware.api.brapi.v1.image.Image;
import org.generationcp.middleware.api.brapi.v1.image.ImageNewRequest;
import org.generationcp.middleware.api.file.FileMetadataDTO;
import org.springframework.web.multipart.MultipartFile;

public interface FileMetadataService {

	Image createImage(ImageNewRequest imageNewRequest);

	Image updateImage(String imageDbId, ImageNewRequest imageNewRequest);

	Image updateImageContent(String imageDbId, byte[] imageContent);

	FileMetadataDTO upload(MultipartFile file, String observationUnitUUID, Integer termId);

	void delete(String fileUUID);
}
