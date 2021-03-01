package org.ibp.api.rest.sample;

import org.apache.commons.lang3.StringUtils;
import org.generationcp.commons.constant.AppConstants;
import org.generationcp.commons.pojo.treeview.TreeNode;
import org.generationcp.commons.util.DateUtil;
import org.generationcp.commons.util.TreeViewUtil;
import org.generationcp.middleware.api.program.ProgramDTO;
import org.generationcp.middleware.domain.sample.SampleDTO;
import org.generationcp.middleware.domain.sample.SampleDetailsDTO;
import org.generationcp.middleware.domain.samplelist.SampleListDTO;
import org.generationcp.middleware.pojos.ListMetadata;
import org.generationcp.middleware.pojos.SampleList;
import org.generationcp.middleware.pojos.workbench.WorkbenchUser;
import org.generationcp.middleware.service.impl.study.SamplePlateInfo;
import org.ibp.api.Util;
import org.ibp.api.exception.ApiRequestValidationException;
import org.ibp.api.exception.ResourceNotFoundException;
import org.ibp.api.java.impl.middleware.common.validator.BaseValidator;
import org.ibp.api.java.impl.middleware.common.validator.ProgramValidator;
import org.ibp.api.java.impl.middleware.security.SecurityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindingResult;
import org.springframework.validation.MapBindingResult;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional(propagation = Propagation.NEVER)
public class SampleListServiceImpl implements SampleListService {

	protected static final String PARENT_ID = "parentId";
	public static final String ID = "id";
	protected static final String PROGRAM_LISTS = "LISTS";
	protected static final String CROP_LISTS = "CROPLISTS";
	private static final String LEAD_CLASS = "lead";
	public static final int BATCH_SIZE = 500;

	@Autowired
	private org.generationcp.middleware.service.api.SampleListService sampleListServiceMW;

	@Autowired
	private SecurityService securityService;

	@Autowired
	private SampleListValidator sampleListValidator;

	@Autowired
	private SampleValidator sampleValidator;

	@Autowired
	private ProgramValidator programValidator;

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
	public void importSamplePlateInformation(final List<SampleDTO> sampleDTOs, final Integer listId){

		this.sampleValidator.validateSamplesForImportPlate(listId, sampleDTOs);

		final Map<String, SamplePlateInfo> samplePlateInfoMap = this.convertToSamplePlateInfoMap(sampleDTOs);
		this.sampleListServiceMW.updateSamplePlateInfo(listId, samplePlateInfoMap);

	}

	@Override
	public List<TreeNode> getSampleListChildrenNodes(final String crop, final String programUUID, final String parentId,
		final Boolean folderOnly) {
		final BindingResult errors = new MapBindingResult(new HashMap<String, String>(), String.class.getName());
		if (!StringUtils.isEmpty(programUUID)) {
			this.programValidator.validate(new ProgramDTO(crop, programUUID), errors);
			if (errors.hasErrors()) {
				throw new ResourceNotFoundException(errors.getAllErrors().get(0));
			}
		}
		this.validateParentId(parentId, programUUID, errors);
		BaseValidator.checkNotNull(folderOnly, "list.folder.only");

		final List<TreeNode> treeNodes = new ArrayList<>();
		if (parentId == null) {
			final TreeNode cropFolderNode = new TreeNode(SampleListServiceImpl.CROP_LISTS, AppConstants.CROP_LISTS.getString(), true, LEAD_CLASS,
				AppConstants.FOLDER_ICON_PNG.getString(), null);
			cropFolderNode.setNumOfChildren(this.sampleListServiceMW.getAllSampleTopLevelLists(null).size());
			treeNodes.add(cropFolderNode);
			if (programUUID != null) {
				final TreeNode programFolderNode = new TreeNode(SampleListServiceImpl.PROGRAM_LISTS, AppConstants.SAMPLE_LISTS.getString(), true, LEAD_CLASS,
					AppConstants.FOLDER_ICON_PNG.getString(), programUUID);
				programFolderNode.setNumOfChildren(this.sampleListServiceMW.getAllSampleTopLevelLists(programUUID).size());
				treeNodes.add(programFolderNode);
			}
		} else {
			final List<SampleList> rootLists;
			if (SampleListServiceImpl.PROGRAM_LISTS.equals(parentId)) {
				rootLists = this.sampleListServiceMW.getAllSampleTopLevelLists(programUUID);
			} else if (SampleListServiceImpl.CROP_LISTS.equals(parentId)) {
				rootLists = this.sampleListServiceMW.getAllSampleTopLevelLists(null);
			} else {
				rootLists = this.sampleListServiceMW.getSampleListByParentFolderIdBatched(Integer.parseInt(parentId), programUUID, SampleListServiceImpl.BATCH_SIZE);
			}

			final List<TreeNode> childNodes = TreeViewUtil.convertListToTreeView(rootLists, folderOnly);

			final Map<Integer, ListMetadata> allListMetaData = this.sampleListServiceMW.getListMetadata(rootLists);

			for (final TreeNode newNode : childNodes) {
				final ListMetadata nodeMetaData = allListMetaData.get(Integer.parseInt(newNode.getKey()));
				if (nodeMetaData != null) {
					if (nodeMetaData.getNumberOfChildren() > 0) {
						newNode.setIsLazy(true);
						newNode.setNumOfChildren(nodeMetaData.getNumberOfChildren());
					}
					if (!newNode.getIsFolder()) {
						newNode.setNoOfEntries(nodeMetaData.getNumberOfEntries());
					}
				}
				newNode.setParentId(parentId);
			}
			return childNodes;
		}
		return treeNodes;
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

			if (!StringUtils.isBlank(dto.getSamplingDate())) {
				sampleListDTO.setSamplingDate(DateUtil.getSimpleDateFormat(DateUtil.FRONTEND_DATE_FORMAT).parse(dto.getSamplingDate()));
			}

			if (!StringUtils.isBlank(dto.getCreatedDate())) {
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
		sampleListDTO.setEntries(dto.getEntries());
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

	private void validateParentId(final String parentId, final String programUUID, final BindingResult errors) {
		if (parentId != null && !PROGRAM_LISTS.equals(parentId) && !CROP_LISTS.equals(parentId) && !Util.isPositiveInteger(parentId)) {
			errors.reject("list.parent.id.invalid", "");
			throw new ApiRequestValidationException(errors.getAllErrors());
		}

		if ((PROGRAM_LISTS.equals(parentId) || Util.isPositiveInteger(parentId)) && StringUtils.isEmpty(programUUID)) {
			errors.reject("list.project.mandatory", "");
			throw new ApiRequestValidationException(errors.getAllErrors());
		}

		if (Util.isPositiveInteger(parentId) && !StringUtils.isEmpty(programUUID)) {
			final SampleList sampleList = this.sampleListServiceMW.getSampleList(Integer.parseInt(parentId));
			if (sampleList == null || !sampleList.isFolder()) {
				errors.reject("list.parent.id.not.exist", "");
				throw new ApiRequestValidationException(errors.getAllErrors());
			}
		}
	}

}
