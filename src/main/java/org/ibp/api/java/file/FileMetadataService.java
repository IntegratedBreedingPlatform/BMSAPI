package org.ibp.api.java.file;

import org.generationcp.middleware.api.brapi.v1.image.Image;
import org.generationcp.middleware.api.brapi.v1.image.ImageNewRequest;
import org.generationcp.middleware.api.file.FileMetadataDTO;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface FileMetadataService {

	FileMetadataDTO getByFileUUID(String fileUUID);

	Image createImage(ImageNewRequest imageNewRequest);

	Image updateImage(String imageDbId, ImageNewRequest imageNewRequest);

	Image updateImageContent(String imageDbId, byte[] imageContent);

	FileMetadataDTO upload(MultipartFile file, String observationUnitUUID, String germplasmUUID, Integer instanceId, Integer lotId, Integer termId);

	void delete(String fileUUID);

	void removeFiles(List<Integer> variableIds, Integer observationUnitUUID, String germplasmUUID, Integer instanceId, Integer lotId);
}
