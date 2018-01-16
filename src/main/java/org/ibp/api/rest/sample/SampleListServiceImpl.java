
package org.ibp.api.rest.sample;

import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.generationcp.commons.spring.util.ContextUtil;
import org.generationcp.commons.util.DateUtil;
import org.generationcp.middleware.domain.samplelist.SampleListDTO;
import org.generationcp.middleware.pojos.SampleList;
import org.generationcp.middleware.pojos.User;
import org.ibp.api.java.impl.middleware.security.SecurityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.base.Preconditions;

@Service
@Transactional(propagation = Propagation.NEVER)
public class SampleListServiceImpl implements SampleListService {

	@Autowired
	private org.generationcp.middleware.service.api.SampleListService sampleListServiceMW;

	@Autowired
	private SecurityService securityService;

	@Autowired
	private ContextUtil contextUtil;

	@Override
	public Map<String, Object> createSampleList(final SampleListDto sampleListDto) {
		Preconditions.checkArgument(sampleListDto.getInstanceIds() != null, "The Instance List must not be null");
		Preconditions.checkArgument(!sampleListDto.getInstanceIds().isEmpty(), "The Instance List must not be empty");
		Preconditions.checkNotNull(sampleListDto.getSelectionVariableId(), "The Selection Variable Id must not be empty");
		Preconditions.checkNotNull(sampleListDto.getStudyId(), "The Study Id must not be empty");
		Preconditions.checkNotNull(sampleListDto.getListName(), "The List Name must not be empty");
		Preconditions.checkArgument(StringUtils.isNotBlank(sampleListDto.getListName()), "The List Name must not be empty");
		Preconditions.checkArgument(sampleListDto.getListName().length() <= 100, "List Name must not exceed 100 characters");
		Preconditions.checkNotNull(sampleListDto.getCreatedDate(), "The Created Date must not be empty");
		Preconditions.checkArgument(
				StringUtils.isBlank(sampleListDto.getDescription()) || sampleListDto.getDescription().length() <= 255,
				"List Description must not exceed 255 characters");
		Preconditions.checkArgument(StringUtils.isBlank(sampleListDto.getNotes()) || sampleListDto.getNotes().length() <= 65535,
				"Notes must not exceed 65535 characters");

		final HashMap<String, Object> mapResponse = new HashMap<>();
		final SampleListDTO sampleListDtoMW = this.translateToSampleListDto(sampleListDto);

		final Integer newSampleId = this.sampleListServiceMW.createSampleList(sampleListDtoMW).getId();
		mapResponse.put("id", String.valueOf(newSampleId));

		return mapResponse;
	}

	/**
	 * Create a sample list folder Sample List folder name must be unique across the elements in the parent folder
	 *
	 * @param folderName
	 * @param parentId
	 * @param programUUID
	 * @return Folder ID
	 * @throws Exception
	 */
	@Override
	public Map<String, Object> createSampleListFolder(final String folderName, final Integer parentId, final String programUUID) {
		Preconditions.checkArgument(folderName != null, "The folder name must not be null");
		Preconditions.checkArgument(parentId != null, "The parent Id must not be null");
		Preconditions.checkArgument(programUUID != null, "The programUUID must not be null");

		final HashMap<String, Object> mapResponse = new HashMap<>();
		final User createdBy = this.securityService.getCurrentlyLoggedInUser();
		final Integer result = this.sampleListServiceMW.createSampleListFolder(folderName, parentId, createdBy, programUUID);
		mapResponse.put("id", String.valueOf(result));
		return mapResponse;
	}

	/**
	 * Update sample list folder name New folder name should be unique across the elements in the parent folder
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
		final SampleList result = this.sampleListServiceMW.updateSampleListFolderName(folderId, newFolderName);
		mapResponse.put("id", String.valueOf(result.getId()));
		return mapResponse;
	}

	/**
	 * Move a folder to another folder FolderID must exist, newParentID must exist newParentID folder must not contain another sample list
	 * with the name that the one that needs to be moved
	 *
	 * @param folderId
	 * @param newParentId
	 * @param isCropList
	 * @throws Exception
	 */

	@Override
	public Map<String, Object> moveSampleListFolder(final Integer folderId, final Integer newParentId, final boolean isCropList) {
		Preconditions.checkArgument(folderId != null, "The folder id must not be null");
		Preconditions.checkArgument(newParentId != null, "The new parent id must not be null");

		final HashMap<String, Object> mapResponse = new HashMap<>();
		final SampleList result = this.sampleListServiceMW.moveSampleList(folderId, newParentId, isCropList);
		mapResponse.put("parentId", String.valueOf(result.getHierarchy().getId()));
		return mapResponse;
	}

	/**
	 * Delete a folder Folder ID must exist and it can not contain any child
	 *
	 * @param folderId
	 * @throws Exception
	 */
	@Override
	public void deleteSampleListFolder(final Integer folderId) {
		Preconditions.checkArgument(folderId != null, "The folder id must not be null");
		this.sampleListServiceMW.deleteSampleListFolder(folderId);
	}

	private SampleListDTO translateToSampleListDto(final SampleListDto dto) {
		final SampleListDTO sampleListDTO = new SampleListDTO();

		sampleListDTO.setCreatedBy(this.securityService.getCurrentlyLoggedInUser().getName());
		sampleListDTO.setCropName(dto.getCropName());
		sampleListDTO.setProgramUUID(dto.getProgramUUID());
		sampleListDTO.setDescription(dto.getDescription());
		sampleListDTO.setInstanceIds(dto.getInstanceIds());
		sampleListDTO.setNotes(dto.getNotes());

		try {

			if (!dto.getSamplingDate().isEmpty()) {
				sampleListDTO.setSamplingDate(DateUtil.getSimpleDateFormat(DateUtil.FRONTEND_DATE_FORMAT).parse(dto.getSamplingDate()));
			}

			if (!dto.getCreatedDate().isEmpty()) {
				sampleListDTO.setCreatedDate(DateUtil.getSimpleDateFormat(DateUtil.FRONTEND_DATE_FORMAT).parse(dto.getCreatedDate()));
			}

		} catch (final ParseException e) {
			throw new IllegalArgumentException("The List Date should be in yyyy-MM-dd format");
		}

		sampleListDTO.setSelectionVariableId(dto.getSelectionVariableId());
		sampleListDTO.setStudyId(dto.getStudyId());
		sampleListDTO.setTakenBy(dto.getTakenBy());
		sampleListDTO.setParentId(dto.getParentId());
		sampleListDTO.setListName(dto.getListName());
		return sampleListDTO;
	}

	public void setSampleListServiceMW(final org.generationcp.middleware.service.api.SampleListService service) {
		this.sampleListServiceMW = service;
	}

	public void setSecurityService(final SecurityService securityService) {
		this.securityService = securityService;
	}
}
