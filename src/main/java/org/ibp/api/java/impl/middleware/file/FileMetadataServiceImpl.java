package org.ibp.api.java.impl.middleware.file;

import org.generationcp.commons.util.FileUtils;
import org.generationcp.middleware.api.brapi.v1.image.Image;
import org.generationcp.middleware.api.brapi.v1.image.ImageNewRequest;
import org.generationcp.middleware.api.file.FileMetadataDTO;
import org.generationcp.middleware.api.file.FileMetadataMapper;
import org.ibp.api.java.file.FileMetadataService;
import org.ibp.api.java.file.FileStorageService;
import org.ibp.api.java.impl.middleware.common.validator.BaseValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@Transactional
public class FileMetadataServiceImpl implements FileMetadataService {

	@Autowired
	private org.generationcp.middleware.api.file.FileMetadataService fileMetadataService;

	@Autowired
	private FileStorageService fileStorageService;

	@Override
	public Image createImage(final ImageNewRequest imageNewRequest) {
		return this.fileMetadataService.save(imageNewRequest);
	}

	@Override
	public Image updateImage(final String imageDbId, final ImageNewRequest imageNewRequest) {
		return this.fileMetadataService.update(imageDbId, imageNewRequest);
	}

	@Override
	public Image updateImageContent(final String imageDbId, final byte[] imageContent) {
		final FileMetadataDTO fileMetadataDTO = this.fileMetadataService.getFileMetadataByUUID(imageDbId);
		this.fileStorageService.upload(FileUtils.wrapAsMultipart(imageContent), fileMetadataDTO.getPath());
		this.fileMetadataService.linkToObservation(fileMetadataDTO, null);

		final FileMetadataMapper fileMetadataMapper = new FileMetadataMapper();
		return fileMetadataMapper.map(fileMetadataDTO);
	}

	@Override
	public FileMetadataDTO save(final MultipartFile file, final String path, final String observationUnitUUID, final Integer termId) {
		FileMetadataDTO fileMetadataDTO = new FileMetadataDTO();
		fileMetadataDTO.setName(file.getOriginalFilename());
		fileMetadataDTO.setMimeType(file.getContentType());
		fileMetadataDTO.setSize((int) file.getSize());
		fileMetadataDTO.setPath(path);
		fileMetadataDTO = this.fileMetadataService.save(fileMetadataDTO, observationUnitUUID);
		this.fileMetadataService.linkToObservation(fileMetadataDTO, termId);
		return fileMetadataDTO;
	}

	@Override
	public String getFilePath(final String observationUnitUUID, final Integer termId, final String fileName) {
		BaseValidator.checkNotNull(fileName, "param.null", new String[] {"fileName"});
		return this.fileMetadataService.getFilePath(observationUnitUUID, termId, fileName);
	}
}
