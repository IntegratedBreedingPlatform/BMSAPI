package org.ibp.api.rest.sample;

import com.google.common.base.Preconditions;
import org.apache.commons.lang3.StringUtils;
import org.generationcp.commons.util.DateUtil;
import org.generationcp.middleware.domain.sample.SampleDetailsDTO;
import org.generationcp.middleware.domain.samplelist.SampleListDTO;
import org.generationcp.middleware.pojos.SampleList;
import org.generationcp.middleware.pojos.workbench.WorkbenchUser;
import org.generationcp.middleware.service.impl.study.SamplePlateInfo;
import org.ibp.api.exception.ApiRequestValidationException;
import org.ibp.api.java.impl.middleware.security.SecurityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindingResult;
import org.springframework.validation.MapBindingResult;

import java.text.ParseException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@Transactional(propagation = Propagation.NEVER)
public class SampleListServiceImpl implements SampleListService {

	protected static final String PARENT_ID = "parentId";
	protected static final String SAMPLE_ID = "Sample Id";
	protected static final String PLATE_ID = "Plate Id";
	protected static final String WELL = "Well";

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
		Preconditions.checkNotNull(sampleListDto.getListName(), "The List Name must not be empty");
		Preconditions.checkArgument(StringUtils.isNotBlank(sampleListDto.getListName()), "The List Name must not be empty");
		Preconditions.checkArgument(sampleListDto.getListName().length() <= 100, "List Name must not exceed 100 characters");
		Preconditions.checkNotNull(sampleListDto.getCreatedDate(), "The Created Date must not be empty");
		Preconditions.checkArgument(StringUtils.isBlank(sampleListDto.getDescription()) || sampleListDto.getDescription().length() <= 255,
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
	 * @param programUUID
	 * @throws Exception
	 */

	@Override
	public Map<String, Object> moveSampleListFolder(final Integer folderId, final Integer newParentId, final boolean isCropList,
			final String programUUID) {
		Preconditions.checkArgument(folderId != null, "The folder id must not be null");
		Preconditions.checkArgument(newParentId != null, "The new parent id must not be null");

		final HashMap<String, Object> mapResponse = new HashMap<>();
		final SampleList result = this.sampleListServiceMW.moveSampleList(folderId, newParentId, isCropList, programUUID);
		mapResponse.put(PARENT_ID, String.valueOf(result.getHierarchy().getId()));
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

	@Override
	public List<SampleList> search(final String searchString, final boolean exactMatch, final String programUUID, final Pageable pageable) {
		return this.sampleListServiceMW.searchSampleLists(searchString, exactMatch, programUUID, pageable);
	}

	@Override
	public List<SampleDetailsDTO> getSampleDetailsDTOs(final Integer listId) {
		return sampleListServiceMW.getSampleDetailsDTOs(listId);
	}

	@Override
	public void importSamplePlateInformation(final PlateInformationDto plateInformationDto){
		final BindingResult bindingResult = new MapBindingResult(new HashMap<String, String>(), PlateInformationDto.class.getName());
		final Map<String, SamplePlateInfo> samplePlateInfoMap = convertToSamplePlateInfoMap(plateInformationDto, bindingResult);

		final Set<String> sampleBusinessKeys = samplePlateInfoMap.keySet();

		final long count = this.sampleListServiceMW.countSamplesByUIDs(sampleBusinessKeys, plateInformationDto.getListId());

		if (sampleBusinessKeys.size() == count) {
			this.sampleListServiceMW.updateSamplePlateInfo(plateInformationDto.getListId(), samplePlateInfoMap);
		} else {
			throwApiRequestValidationError(bindingResult, "sample.sample.ids.not.present.in.file");
		}
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

	protected Map<String, SamplePlateInfo> convertToSamplePlateInfoMap(final PlateInformationDto plateInformationDto,
		final BindingResult bindingResult) {

		final Map<String, SamplePlateInfo> map = new HashMap<>();

		if (StringUtils.isBlank(plateInformationDto.getSampleIdHeader())) {
			throwApiRequestValidationError(bindingResult, "sample.header.not.mapped", new Object[] {SampleListServiceImpl.SAMPLE_ID});

		}
		if (StringUtils.isBlank(plateInformationDto.getPlateIdHeader())) {
			throwApiRequestValidationError(bindingResult, "sample.header.not.mapped", new Object[] {SampleListServiceImpl.PLATE_ID});

		}
		if (StringUtils.isBlank(plateInformationDto.getWellHeader())) {
			throwApiRequestValidationError(bindingResult, "sample.header.not.mapped", new Object[] {SampleListServiceImpl.WELL});

		}
		final int sampleIdHeaderIndex = plateInformationDto.getImportData().get(0).indexOf(plateInformationDto.getSampleIdHeader());
		final int plateIdHeaderIndex = plateInformationDto.getImportData().get(0).indexOf(plateInformationDto.getPlateIdHeader());
		final int wellHeaderIndex = plateInformationDto.getImportData().get(0).indexOf(plateInformationDto.getWellHeader());
		final Iterator<List<String>> iterator = plateInformationDto.getImportData().iterator();
		// Ignore the first row which is the header.
		iterator.next();

		if (sampleIdHeaderIndex == -1) {
			throwApiRequestValidationError(bindingResult, "sample.header.not.matched", new Object[] {"Sample Id"});
		}

		if (plateIdHeaderIndex == -1) {
			throwApiRequestValidationError(bindingResult, "sample.header.not.matched", new Object[] {"Plate Id"});
		}

		if (wellHeaderIndex == -1) {
			throwApiRequestValidationError(bindingResult, "sample.header.not.matched", new Object[] {"Well"});
		}

		// Convert the rows to SamplePlateInfo map.
		while (iterator.hasNext()) {
			final List<String> rowData = iterator.next();
			final SamplePlateInfo samplePlateInfo = new SamplePlateInfo();
			final int numElements = rowData.size() - 1;

			if (numElements >= plateIdHeaderIndex) {
				final String plateId = rowData.get(plateIdHeaderIndex);
				if (StringUtils.isNotBlank(plateId) && plateId.length() > 255) {
					throwApiRequestValidationError(bindingResult, "sample.plate.id.exceed.length");
				}
				samplePlateInfo.setPlateId(plateId);
			}

			if (numElements >= wellHeaderIndex) {
				final String well = rowData.get(wellHeaderIndex);
				if (StringUtils.isNotBlank(well) && well.length() > 255) {
					throwApiRequestValidationError(bindingResult, "sample.well.exceed.length");
				}
				samplePlateInfo.setWell(well);
			}

			if (numElements >= sampleIdHeaderIndex) {
				final String sampleId = rowData.get(sampleIdHeaderIndex);

				if (StringUtils.isBlank(sampleId)) {
					throwApiRequestValidationError(bindingResult, "sample.record.not.include.sample.id.in.file");
				}
				if (map.get(sampleId) != null) {
					throwApiRequestValidationError(bindingResult, "sample.id.repeat.in.file");
				}
				map.put(sampleId, samplePlateInfo);
			} else {
				throwApiRequestValidationError(bindingResult, "sample.record.not.include.sample.id.in.file");
			}
		}
		return map;
	}

	private void throwApiRequestValidationError(final BindingResult bindingResult, final String errorDescription) {
		bindingResult.reject(errorDescription, "");
		throw new ApiRequestValidationException(bindingResult.getAllErrors());

	}

	private void throwApiRequestValidationError(final BindingResult bindingResult, final String errorDescription,
		final Object[] arguments) {
		bindingResult.reject(errorDescription, arguments, null);
		throw new ApiRequestValidationException(bindingResult.getAllErrors());
	}
}
