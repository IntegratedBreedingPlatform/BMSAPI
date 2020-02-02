package org.ibp.api.rest.sample;

import org.generationcp.commons.util.DateUtil;
import org.generationcp.middleware.domain.sample.SampleDTO;
import org.generationcp.middleware.domain.sample.SampleDetailsDTO;
import org.generationcp.middleware.domain.samplelist.SampleListDTO;
import org.generationcp.middleware.pojos.SampleList;
import org.generationcp.middleware.pojos.workbench.WorkbenchUser;
import org.generationcp.middleware.service.impl.study.SamplePlateInfo;
import org.ibp.api.exception.ApiRuntimeException;
import org.ibp.api.java.impl.middleware.security.SecurityService;
import org.ibp.api.rest.samplesubmission.domain.GOBiiGermplasm;
import org.ibp.api.rest.samplesubmission.domain.GOBiiProject;
import org.ibp.api.rest.samplesubmission.domain.GOBiiSample;
import org.ibp.api.rest.samplesubmission.domain.GOBiiSampleList;
import org.ibp.api.rest.samplesubmission.domain.common.GOBiiToken;
import org.ibp.api.rest.samplesubmission.service.GOBiiAuthenticationService;
import org.ibp.api.rest.samplesubmission.service.GOBiiProjectService;
import org.ibp.api.rest.samplesubmission.service.GOBiiSampleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.ParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional
public class SampleListServiceImpl implements SampleListService {

	protected static final String PARENT_ID = "parentId";
	public static final String ID = "id";

	@Autowired
	private org.generationcp.middleware.service.api.SampleListService sampleListServiceMW;

	@Autowired
	private SecurityService securityService;

	@Autowired
	private GOBiiAuthenticationService goBiiAuthenticationService;

	@Autowired
	private GOBiiProjectService goBiiProjectService;

	@Autowired
	private GOBiiSampleService goBiiSampleService;

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
		this.sampleListValidator.validateFolderIdAndProgram(parentId);


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
		this.sampleListValidator.validateFolderIdAndProgram(folderId);

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

		this.sampleListValidator.validateFolderIdAndProgram(folderId);
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
		this.sampleListValidator.validateFolderIdAndProgram(folderId);
		this.sampleListServiceMW.deleteSampleListFolder(folderId);
	}

	@Override
	public List<SampleList> search(final String searchString, final boolean exactMatch, final String programUUID, final Pageable pageable) {
		return this.sampleListServiceMW.searchSampleLists(searchString, exactMatch, programUUID, pageable);
	}

	@Override
	public List<SampleDetailsDTO> getSampleDetailsDTOs(final Integer listId) {
		return this.sampleListServiceMW.getSampleDetailsDTOs(listId);
	}

	@Override
	public SampleListDto getSampleListDTO(final Integer listId) {
		final SampleList sampleList = sampleListServiceMW.getSampleList(listId);
		return SampleListMapper.getInstance().map(sampleList, SampleListDto.class);
	}

	@Override
	public void importSamplePlateInformation(final List<SampleDTO> sampleDTOs, final Integer listId){

		this.sampleValidator.validateSamplesForImportPlate(listId, sampleDTOs);

		final Map<String, SamplePlateInfo> samplePlateInfoMap = this.convertToSamplePlateInfoMap(sampleDTOs);
		this.sampleListServiceMW.updateSamplePlateInfo(listId, samplePlateInfoMap);

	}

	@Override
	public SampleList getSampleListById(final Integer sampleListId) {
		return this.sampleListServiceMW.getSampleListById(sampleListId);
	}

	@Override
	public SampleList saveOrUpdate(final SampleList sampleList) {
		return this.sampleListServiceMW.saveOrUpdate(sampleList);
	}

	@Override
	public Integer submitToGOBii (final Integer sampleListId) {
		final GOBiiToken token;
		try {
			token = goBiiAuthenticationService.authenticate();
		} catch (Exception e) {
			throw new ApiRuntimeException("Could not connect to GOBii. Try later");
		}
		final SampleList sampleList = this.getSampleListById(sampleListId);
		if (sampleList != null && sampleList.getGobiiProjectId() == null) {
			final GOBiiProject goBiiProject = this.buildGOBiiProject(sampleList);
			final Integer projectId = goBiiProjectService.postGOBiiProject(token, goBiiProject);
			if (projectId != null) {
				sampleList.setGobiiProjectId(projectId);
				this.saveOrUpdate(sampleList);
				final GOBiiSampleList goBiiSampleList = new GOBiiSampleList();
				goBiiSampleList.setProjectId(projectId);

				final List<SampleDetailsDTO> sampleDetailsDTOs = this.getSampleDetailsDTOs(sampleListId);
				for (final SampleDetailsDTO sample: sampleDetailsDTOs) {
					final GOBiiSample goBiiSample = new GOBiiSample();
					final GOBiiGermplasm goBiiGermplasm = new GOBiiGermplasm();
					goBiiGermplasm.setExternalCode(String.valueOf(sample.getGid()));
					goBiiGermplasm.setGermplasmName(sample.getDesignation());

					goBiiSample.setSampleNum(String.valueOf(sample.getEntryNumber()));
					goBiiSample.setGermplasm(goBiiGermplasm);
					goBiiSample.setSampleName(sample.getSampleName());
					goBiiSample.setSampleUuid(sample.getSampleBusinessKey());
					goBiiSample.setPlateName(sample.getPlateId());
					goBiiSample.setWellRow(sample.getWell());
					goBiiSample.setWellCol(sample.getWell());
					goBiiSample.setProperties(new HashMap<>());
					goBiiSampleList.getSamples().add(goBiiSample);
				}
				goBiiSampleService.postGOBiiSampleList(token, goBiiSampleList);
				return projectId;
			} else {
				throw new ApiRuntimeException("An error has occurred when trying to send data to GOBii ");
			}
		}
		throw new ApiRuntimeException("List was previously sent to GOBii");
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

	private GOBiiProject buildGOBiiProject(final SampleList sampleListDto) {
		final GOBiiProject goBiiProject = new GOBiiProject();
		goBiiProject.setCode(String.valueOf(sampleListDto.getId()));
		goBiiProject.setProjectName(sampleListDto.getListName());
		goBiiProject.setPiContactId(1);
		goBiiProject.setProperties(new HashMap<>());
		goBiiProject.setStatus(1);
		return goBiiProject;
	}

}
