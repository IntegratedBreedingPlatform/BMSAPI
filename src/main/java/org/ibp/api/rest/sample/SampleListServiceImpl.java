
package org.ibp.api.rest.sample;

import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;

import org.generationcp.commons.util.DateUtil;
import org.generationcp.middleware.domain.samplelist.SampleListDTO;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.pojos.SampleList;
import org.ibp.api.java.impl.middleware.security.SecurityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.base.Preconditions;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional (propagation = Propagation.NEVER)
public class SampleListServiceImpl implements SampleListService {

	@Autowired
	private org.generationcp.middleware.service.api.SampleListService sampleListServiceMW;

	@Autowired
	private SecurityService securityService;

	@Override
	public Map<String, Object> createSampleList(final SampleListDto sampleListDto) {
		Preconditions.checkArgument(sampleListDto.getInstanceIds() != null, "The Instance List must not be null");
		Preconditions.checkArgument(!sampleListDto.getInstanceIds().isEmpty(), "The Instance List must not be empty");
		Preconditions.checkNotNull(sampleListDto.getSelectionVariableId(), "The Selection Variable Id must not be empty");
		Preconditions.checkNotNull(sampleListDto.getStudyId(), "The Study Id must not be empty");

		final HashMap<String, Object> mapResponse = new HashMap<>();
		mapResponse.put("id", String.valueOf(0));
		try {
			final SampleListDTO sampleListDtoMW = this.translateToSampleListDto(sampleListDto);

			final Integer newSampleId = this.sampleListServiceMW.createSampleList(sampleListDtoMW).getId();
			mapResponse.put("id", String.valueOf(newSampleId));

		} catch (MiddlewareQueryException | ParseException e) {
			mapResponse.put("ERROR", "Error on SampleListService.createSampleList " + e.getMessage());
		}

		return mapResponse;
	}

	/**
	 * Create a sample list folder
	 * Sample List folder name must be unique across the elements in the parent folder
	 *
	 * @param folderName
	 * @param parentId
	 * @return Folder ID
	 * @throws Exception
	 */
	@Override
	public Map<String, Object> createSampleListFolder(final String folderName, final Integer parentId) {
		Preconditions.checkArgument(folderName != null, "The folder name must not be null");
		Preconditions.checkArgument(parentId != null, "The parent Id must not be null");

		final HashMap<String, Object> mapResponse = new HashMap<>();
		mapResponse.put("id", String.valueOf(0));
		try {

			final String createdBy = this.securityService.getCurrentlyLoggedInUser().getName();
			Integer result = this.sampleListServiceMW.createSampleListFolder(folderName, parentId, createdBy);
			mapResponse.put("id", String.valueOf(result));
		} catch (Exception e) {
			mapResponse.put("ERROR", "Error on SampleListService.createSampleListFolder " + e.getMessage());
		}
		return mapResponse;
	}

	/**
	 * Update sample list folder name
	 * New folder name should be unique across the elements in the parent folder
	 *
	 * @param folderId
	 * @param newFolderName
	 * @throws Exception
	 */
	@Override
	public Map<String, Object> updateSampleListFolderName(final Integer folderId, final String newFolderName) {
		Preconditions.checkArgument(folderId != null, "The folder id must not be null");
		Preconditions.checkArgument(newFolderName != null, "The new folder name must not be null");

		final HashMap<String, Object> mapResponse = new HashMap<>();
		mapResponse.put("id", String.valueOf(0));
		try {
			SampleList result = this.sampleListServiceMW.updateSampleListFolderName(folderId, newFolderName);
			mapResponse.put("id", String.valueOf(result.getId()));
		} catch (Exception e) {
			mapResponse.put("ERROR", "Error on SampleListService.updateSampleListFolderName " + e.getMessage());
		}
		return mapResponse;
	}

	/**
	 * Move a folder to another folder
	 * FolderID must exist, newParentID must exist
	 * newParentID folder must not contain another sample list with the name that the one that needs to be moved
	 *
	 * @param folderId
	 * @param newParentId
	 * @throws Exception
	 */

	@Override
	public Map<String, Object> moveSampleListFolder(final Integer folderId, final Integer newParentId) {
		Preconditions.checkArgument(folderId != null, "The folder id must not be null");
		Preconditions.checkArgument(newParentId != null, "The new parent id must not be null");

		final HashMap<String, Object> mapResponse = new HashMap<>();
		mapResponse.put("id", String.valueOf(0));
		try {
			SampleList result = this.sampleListServiceMW.moveSampleList(folderId, newParentId);
			mapResponse.put("parentId", String.valueOf(result.getHierarchy().getId()));
		} catch (Exception e) {
			mapResponse.put("ERROR", "Error on SampleListService.moveSampleListFolder " + e.getMessage());
		}
		return mapResponse;
	}

	/**
	 * Delete a folder
	 * Folder ID must exist and it can not contain any child
	 *
	 * @param folderId
	 * @throws Exception
	 */
	@Override
	public Map<String, Object> deleteSampleListFolder(final Integer folderId) {
		Preconditions.checkArgument(folderId != null, "The folder id must not be null");

		final HashMap<String, Object> mapResponse = new HashMap<>();
		mapResponse.put("id", String.valueOf(0));
		try {
			this.sampleListServiceMW.deleteSampleListFolder(folderId);
		} catch (Exception e) {
			mapResponse.put("ERROR", "Error on SampleListService.deleteSampleListFolder " + e.getMessage());
		}
		return mapResponse;
	}

	private SampleListDTO translateToSampleListDto(final SampleListDto dto) throws ParseException {
		final SampleListDTO sampleListDTO = new SampleListDTO();

		sampleListDTO.setCreatedBy(this.securityService.getCurrentlyLoggedInUser().getName());
		sampleListDTO.setCropName(dto.getCropName());
		sampleListDTO.setDescription(dto.getDescription());
		sampleListDTO.setInstanceIds(dto.getInstanceIds());
		sampleListDTO.setNotes(dto.getNotes());

		if (!dto.getSamplingDate().isEmpty()) {
			sampleListDTO.setSamplingDate(DateUtil.getSimpleDateFormat(DateUtil.FRONTEND_DATE_FORMAT).parse(dto.getSamplingDate()));
		}

		sampleListDTO.setSelectionVariableId(dto.getSelectionVariableId());
		sampleListDTO.setStudyId(dto.getStudyId());
		sampleListDTO.setTakenBy(dto.getTakenBy());

		return sampleListDTO;
	}

	public void setSampleListServiceMW(final org.generationcp.middleware.service.api.SampleListService service) {
		this.sampleListServiceMW = service;
	}

	public void setSecurityService(final SecurityService securityService) {
		this.securityService = securityService;
	}
}
