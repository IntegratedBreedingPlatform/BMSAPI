package org.ibp.api.rest.sample;

import org.generationcp.commons.util.DateUtil;
import org.generationcp.middleware.domain.sample.SampleDTO;
import org.generationcp.middleware.domain.sample.SampleDetailsDTO;
import org.generationcp.middleware.domain.samplelist.SampleListDTO;
import org.generationcp.middleware.pojos.SampleList;
import org.generationcp.middleware.pojos.workbench.WorkbenchUser;
import org.generationcp.middleware.service.impl.study.SamplePlateInfo;
import org.ibp.api.java.impl.middleware.security.SecurityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.text.ParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional(propagation = Propagation.NEVER)
public class SampleListServiceImpl implements SampleListService {

	protected static final String PARENT_ID = "parentId";
	public static final String ID = "id";

	@Autowired
	private org.generationcp.middleware.service.api.SampleListService sampleListServiceMW;

	@Autowired
	private SecurityService securityService;

	@Autowired
	private SampleListValidator sampleListValidator;

	@Autowired
	private SampleValidator sampleValidator;

	@Override
	public Map<String, Object> createSampleList(final SampleListDto sampleListDto) {

		this.sampleListValidator.validateSampleList(sampleListDto);

		final HashMap<String, Object> mapResponse = new HashMap<>();
		final SampleListDTO sampleListDtoMW = this.translateToSampleListDto(sampleListDto);

		final Integer newSampleId = this.sampleListServiceMW.createSampleList(sampleListDtoMW).getId();
		mapResponse.put(ID, String.valueOf(newSampleId));

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

		this.sampleListValidator.validateFolderName(folderName);
		this.sampleListValidator.validateFolderId(parentId);
		this.sampleListValidator.validateProgramUUID(programUUID);

		final HashMap<String, Object> mapResponse = new HashMap<>();
		final WorkbenchUser createdBy = this.securityService.getCurrentlyLoggedInUser();
		final Integer result = this.sampleListServiceMW.createSampleListFolder(folderName, parentId, createdBy.getName(), programUUID);
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

		this.sampleListValidator.validateFolderName(newFolderName);
		this.sampleListValidator.validateFolderId(folderId);

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
	 * @param programUUID
	 * @throws Exception
	 */

	@Override
	public Map<String, Object> moveSampleListFolder(final Integer folderId, final Integer newParentId, final boolean isCropList,
			final String programUUID) {

		this.sampleListValidator.validateFolderId(folderId);
		this.sampleListValidator.validateFolderId(newParentId);

		final HashMap<String, Object> mapResponse = new HashMap<>();
		final SampleList result = this.sampleListServiceMW.moveSampleList(folderId, newParentId, isCropList, programUUID);
		mapResponse.put(PARENT_ID, String.valueOf(result.getHierarchy().getId()));
		return mapResponse;
	}

	/**
	 * Delete a folder Folder ID must exist and it can not contain any child
	 *
	 * @param folderId
	 */
	@Override
	public void deleteSampleListFolder(final Integer folderId) {
		this.sampleListValidator.validateFolderId(folderId);
		this.sampleListServiceMW.deleteSampleListFolder(folderId);
	}

	@Override
	public List<SampleList> search(final String searchString, final boolean exactMatch, final String programUUID, final Pageable pageable) {
		return this.sampleListServiceMW.searchSampleLists(searchString, exactMatch, programUUID, pageable);
	}

	@Override
	public List<SampleDetailsDTO> getSampleDetailsDTOs(final Integer listId) {
		return sampleListServiceMW.getSampleDetailsDTOs(listId);
	}

	@Override
	public void importSamplePlateInformation(final List<SampleDTO> sampleDTOs, final Integer listId){

		this.sampleValidator.validateSamplesForImportPlate(listId, sampleDTOs);

		final Map<String, SamplePlateInfo> samplePlateInfoMap = convertToSamplePlateInfoMap(sampleDTOs);
		this.sampleListServiceMW.updateSamplePlateInfo(listId, samplePlateInfoMap);

	}

	protected SampleListDTO translateToSampleListDto(final SampleListDto dto) {
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
		sampleListDTO.setDatasetId(dto.getDatasetId());
		sampleListDTO.setTakenBy(dto.getTakenBy());
		sampleListDTO.setParentId(dto.getParentId());
		sampleListDTO.setListName(dto.getListName());
		return sampleListDTO;
	}

	protected Map<String, SamplePlateInfo> convertToSamplePlateInfoMap(final List<SampleDTO> sampleDTOs) {

		final Map<String, SamplePlateInfo> map = new HashMap<>();

		// Convert the rows to SamplePlateInfo map.
		for (final SampleDTO sampleDTO : sampleDTOs) {
			final SamplePlateInfo samplePlateInfo = new SamplePlateInfo();
			final String sampleId = sampleDTO.getSampleBusinessKey();
			final String plateId = sampleDTO.getPlateId();
			final String well = sampleDTO.getWell();
			samplePlateInfo.setPlateId(plateId);
			samplePlateInfo.setWell(well);
			map.put(sampleId, samplePlateInfo);
		}
		return map;
	}

}
