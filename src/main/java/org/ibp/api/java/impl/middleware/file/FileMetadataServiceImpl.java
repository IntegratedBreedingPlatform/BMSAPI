package org.ibp.api.java.impl.middleware.file;

import org.generationcp.commons.util.FileUtils;
import org.generationcp.middleware.api.brapi.v1.image.Image;
import org.generationcp.middleware.api.brapi.v1.image.ImageNewRequest;
import org.generationcp.middleware.api.file.FileMetadataDTO;
import org.generationcp.middleware.api.file.FileMetadataMapper;
import org.ibp.api.java.file.FileMetadataService;
import org.ibp.api.java.file.FileStorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang3.StringUtils.isBlank;

@Service
@Transactional
public class FileMetadataServiceImpl implements FileMetadataService {

	@Autowired
	private org.generationcp.middleware.api.file.FileMetadataService fileMetadataService;

	@Autowired
	private FileStorageService fileStorageService;

	@Override
	public FileMetadataDTO getByFileUUID(final String fileUUID) {
		return this.fileMetadataService.getByFileUUID(fileUUID);
	}

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
		final FileMetadataDTO fileMetadataDTO = this.fileMetadataService.getByFileUUID(imageDbId);
		this.fileStorageService.upload(FileUtils.wrapAsMultipart(imageContent), fileMetadataDTO.getPath());

		final FileMetadataMapper fileMetadataMapper = new FileMetadataMapper();
		return fileMetadataMapper.map(fileMetadataDTO);
	}

	@Override
	public FileMetadataDTO upload(
		final MultipartFile file,
		final String observationUnitUUID,
		final String germplasmUUID,
		final Integer termId
	) {

		final String path = !isBlank(observationUnitUUID) //
			? this.fileMetadataService.getFilePathForObservationUnit(observationUnitUUID, file.getOriginalFilename()) //
			: this.fileMetadataService.getFilePathForGermplasm(germplasmUUID, file.getOriginalFilename());

		FileMetadataDTO fileMetadataDTO = new FileMetadataDTO();
		fileMetadataDTO.setName(file.getOriginalFilename());
		fileMetadataDTO.setMimeType(file.getContentType());
		fileMetadataDTO.setSize((int) file.getSize());
		fileMetadataDTO.setPath(path);
		fileMetadataDTO = this.fileMetadataService.save(fileMetadataDTO, observationUnitUUID, germplasmUUID, termId);

		// save file storage last as it is outside the transaction
		this.fileStorageService.upload(file, path);

		return fileMetadataDTO;
	}

	@Override
	public void delete(final String fileUUID) {
		final FileMetadataDTO fileMetadataDTO = this.fileMetadataService.getByFileUUID(fileUUID);
		this.fileMetadataService.delete(fileUUID);
		// delete file storage last as it is outside the transaction
		this.fileStorageService.deleteFile(fileMetadataDTO.getPath());
	}

	@Override
	public void removeFiles(final List<Integer> variableIds, final Integer observationUnitUUID, final String germplasmUUID) {
		final List<FileMetadataDTO> fileMetadataDTOList = this.fileMetadataService.getAll(variableIds, observationUnitUUID, germplasmUUID);

		this.fileMetadataService.removeFiles(variableIds, observationUnitUUID, germplasmUUID);

		final List<String> paths = fileMetadataDTOList.stream().map(FileMetadataDTO::getPath).collect(toList());
		// delete file storage last as it is outside the transaction
		this.fileStorageService.deleteFiles(paths);
	}

}
