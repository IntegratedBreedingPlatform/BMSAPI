package org.ibp.api.rest.file;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.generationcp.middleware.api.file.FileMetadataDTO;
import org.generationcp.middleware.pojos.workbench.PermissionsEnum;
import org.ibp.api.java.file.FileMetadataService;
import org.ibp.api.java.file.FileStorageService;
import org.ibp.api.java.impl.middleware.common.validator.BaseValidator;
import org.ibp.api.java.impl.middleware.file.validator.FileValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.net.URLDecoder;
import java.util.Collections;
import java.util.Map;

import static org.apache.commons.lang3.StringUtils.isBlank;

@Api("File services")
@RestController
@RequestMapping("/crops/{cropName}")
public class FileResource {

	@Autowired
	private FileStorageService fileStorageService;

	@Autowired
	private FileMetadataService fileMetadataService;

	@Autowired
	private FileValidator fileValidator;

	@Autowired
	private HttpServletRequest request;

	@RequestMapping(value = "/files", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<FileMetadataDTO> upload(
		@PathVariable final String cropName,
		@RequestParam(required = false) final String programUUID,
		@RequestPart("file") final MultipartFile file,
		@RequestParam(required = false) final String observationUnitUUID,
		@RequestParam(required = false) final String germplasmUUID,
		@RequestParam(required = false) final Integer instanceId,
		@RequestParam(required = false) final Integer lotId,
		@RequestParam(required = false) final Integer termId
	) {
		this.validateFileStorage();
		if (!isBlank(observationUnitUUID)) {
			FileResource.verifyHasAuthorityStudyObservation(this.request);
		}
		else if (instanceId != null) {
			FileResource.verifyHasAuthorityStudyEnvironment(this.request);
		} else if(lotId != null) {
			verifyHasAuthorityLots(this.request);
		}  else {
			FileResource.verifyHasAuthorityGermplasm(this.request);
		}
		this.fileValidator.validateFile(file);
		//Check if only one of the parameters has value
		final boolean valid = ((isBlank(observationUnitUUID)? 0 : 1) + (isBlank(germplasmUUID)? 0 : 1) + ((instanceId == null)? 0 : 1) + ((lotId == null)? 0 : 1)) == 1;
		BaseValidator.checkArgument(valid, "file.upload.entity.invalid");

		final FileMetadataDTO fileMetadataDTO = this.fileMetadataService
			.upload(file, observationUnitUUID, germplasmUUID, instanceId, lotId, termId);
		return new ResponseEntity<>(fileMetadataDTO, HttpStatus.CREATED);
	}

	/*
	 * NOTE: with GET /files/{fileUUID} we get some url sanitization issues in the frontend:
	 * WARNING: sanitizing unsafe URL value
	 */
	@RequestMapping(value = "/files/**", method = RequestMethod.GET)
	@ResponseBody
	public byte[] getFile(
		@PathVariable final String cropName,
		@RequestParam(required = false) final String programUUID,
		final HttpServletRequest request
	) {
		this.validateFileStorage();
		final String path = getPath(request);
		return this.fileStorageService.getFile(path);
	}

	private static String getPath(final HttpServletRequest request) {
		final String path = request.getRequestURI().split(".*\\/files\\/")[1];
		return URLDecoder.decode(path);
	}

	@RequestMapping(value = "/files/{fileUUID}", method = RequestMethod.DELETE)
	@ResponseBody
	public ResponseEntity<Void> deleteFile(
		@PathVariable final String cropName,
		@RequestParam(required = false) final String programUUID,
		@PathVariable final String fileUUID
	) {
		this.validateFileStorage();
		final FileMetadataDTO fileMetadataDTO = this.fileMetadataService.getByFileUUID(fileUUID);
		if (fileMetadataDTO.getInstanceId() != null) {
			verifyHasAuthorityStudyEnvironment(this.request);
		} else if (!isBlank(fileMetadataDTO.getObservationUnitUUID())) {
			verifyHasAuthorityStudyObservation(this.request);
		} else if(fileMetadataDTO.getLotId() != null) {
			verifyHasAuthorityLots(this.request);
		} else {
			verifyHasAuthorityGermplasm(this.request);
		}

		this.fileMetadataService.delete(fileUUID);
		return new ResponseEntity<>(HttpStatus.OK);
	}

	/**
	 * @return Map<String, String> to overcome angularjs limitation
	 */
	@ApiOperation("Get file storage status: true => active")
	@RequestMapping(value = "/filestorage/status", method = RequestMethod.GET)
	public ResponseEntity<Map<String, Boolean>> getFileStorageStatus(
		@PathVariable final String cropName,
		@RequestParam(required = false) final String programUUID
	) {
		return new ResponseEntity<>(Collections.singletonMap("status", this.fileStorageService.isConfigured()), HttpStatus.OK);
	}

	private void validateFileStorage() {
		BaseValidator.checkArgument(this.fileStorageService.isConfigured(), "file.storage.not.configured");
	}

	public static void verifyHasAuthorityGermplasm(final HttpServletRequest request) {
		if (!(request.isUserInRole(PermissionsEnum.ADMIN.name())
			|| request.isUserInRole(PermissionsEnum.GERMPLASM.name())
			|| request.isUserInRole(PermissionsEnum.MANAGE_GERMPLASM.name())
			|| request.isUserInRole(PermissionsEnum.EDIT_GERMPLASM.name())
			|| request.isUserInRole(PermissionsEnum.MG_MANAGE_FILES.name()))) {
			throw new AccessDeniedException("");
		}
	}

	public static void verifyHasAuthorityStudyObservation(final HttpServletRequest request) {
		if (!(request.isUserInRole(PermissionsEnum.ADMIN.name())
			|| request.isUserInRole(PermissionsEnum.STUDIES.name())
			|| request.isUserInRole(PermissionsEnum.MANAGE_STUDIES.name())
			|| request.isUserInRole(PermissionsEnum.MS_MANAGE_OBSERVATION_UNITS.name())
			|| request.isUserInRole(PermissionsEnum.MS_MANAGE_FILES.name()))) {
			throw new AccessDeniedException("");
		}
	}

	public static void verifyHasAuthorityStudyEnvironment(final HttpServletRequest request) {
		if (!(request.isUserInRole(PermissionsEnum.ADMIN.name())
			|| request.isUserInRole(PermissionsEnum.STUDIES.name())
			|| request.isUserInRole(PermissionsEnum.MANAGE_STUDIES.name())
			|| request.isUserInRole(PermissionsEnum.ENVIRONMENT.name())
			|| request.isUserInRole(PermissionsEnum.MANAGE_FILES_ENVIRONMENT.name()))) {
			throw new AccessDeniedException("");
		}
	}

	public static void verifyHasAuthorityLots(final HttpServletRequest request) {
		if (!(request.isUserInRole(PermissionsEnum.ADMIN.name())
			|| request.isUserInRole(PermissionsEnum.CROP_MANAGEMENT.name())
			|| request.isUserInRole(PermissionsEnum.MANAGE_INVENTORY.name())
			|| request.isUserInRole(PermissionsEnum.MANAGE_LOTS.name())
			|| request.isUserInRole(PermissionsEnum.UPDATE_LOTS.name())
			|| request.isUserInRole(PermissionsEnum.MI_MANAGE_FILES.name()))) {
			throw new AccessDeniedException("");
		}
	}
}
