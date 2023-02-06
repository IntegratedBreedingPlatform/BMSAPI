package org.ibp.api.rest.sample;

import org.apache.commons.lang3.StringUtils;
import org.generationcp.middleware.ContextHolder;
import org.generationcp.middleware.domain.sample.SampleDTO;
import org.generationcp.middleware.pojos.SampleList;
import org.ibp.api.exception.ApiRequestValidationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.validation.BindingResult;
import org.springframework.validation.MapBindingResult;

import java.util.HashMap;
import java.util.List;

@Component
public class SampleListValidator {

	@Autowired
	private org.generationcp.middleware.service.api.SampleListService sampleListServiceMW;

	private BindingResult errors;

	public void validateSampleList(final SampleListDto sampleListDto) {

		this.errors = new MapBindingResult(new HashMap<String, String>(), Integer.class.getName());

		if (!sampleListDto.getEntries().isEmpty()) {
			// TODO IBP-4375 validate sample ids
		} else {
			if (sampleListDto.getInstanceIds() == null) {
				this.errors.reject("sample.list.instance.list.must.not.be.null","The Instance List must not be null");
			}
			if (sampleListDto.getInstanceIds() != null && sampleListDto.getInstanceIds().isEmpty()) {
				this.errors.reject("sample.list.instance.list.must.not.empty","The Instance List must not be empty");
			}
			if (sampleListDto.getSelectionVariableId() == null) {
				this.errors.reject("sample.list.selection.variable.id.must.not.empty", "The Selection Variable Id must not be empty");
			}
		}
		if (StringUtils.isBlank(sampleListDto.getListName())) {
			this.errors.reject("sample.list.listname.must.not.empty","The List Name must not be empty");
		}
		if (sampleListDto.getListName().length() > 100) {
			this.errors.reject("sample.list.listname.exceed.length","List Name must not exceed 100 characters");
		}
		if (StringUtils.isEmpty(sampleListDto.getCreatedDate())) {
			this.errors.reject("sample.list.created.date.empty","The Created Date must not be empty");
		}
		if (StringUtils.isNotBlank(sampleListDto.getDescription()) && sampleListDto.getDescription().length() > 255) {
			this.errors.reject("sample.list.description.exceed.length","List Description must not exceed 255 characters");
		}
		if (StringUtils.isNotBlank(sampleListDto.getNotes()) && sampleListDto.getNotes().length() > 65535) {
			this.errors.reject("sample.list.notes.exceed.length","Notes must not exceed 65535 characters");
		}

		if (this.errors.hasErrors()) {
			throw new ApiRequestValidationException(this.errors.getAllErrors());
		}

	}

	public void validateSampleList(final Integer sampleListId) {
		this.errors = new MapBindingResult(new HashMap<String, String>(), Integer.class.getName());
		final SampleList sampleList = this.sampleListServiceMW.getSampleList(sampleListId);
		if (sampleList == null) {
			this.errors.reject("sample.list.id.is.invalid","The sample list id is invalid");
			throw new ApiRequestValidationException(this.errors.getAllErrors());
		}
		if (sampleList.isFolder()) {
			this.errors.reject("sample.list.type.is.invalid","The sample list should not be a folder");
			throw new ApiRequestValidationException(this.errors.getAllErrors());
		}
	}

	public void verifySamplesExist(final Integer sampleListId, final List<Integer> sampleIds) {
		this.errors = new MapBindingResult(new HashMap<String, String>(), Integer.class.getName());

		if(CollectionUtils.isEmpty(sampleIds)) {
			this.errors.reject("sample.ids.selected.samples.empty", "Selected entries can not be empty");
			throw new ApiRequestValidationException(this.errors.getAllErrors());
		}

		final List<SampleDTO> samples = this.sampleListServiceMW.getSampleListEntries(sampleListId, sampleIds);
		if (samples.size() != sampleIds.size()) {
			this.errors.reject("sample.ids.not.exist", "Some sampleIds were not found in the system. Please check");
			throw new ApiRequestValidationException(this.errors.getAllErrors());
		}
	}

	public void validateFolderName(final String folderName) {

		this.errors = new MapBindingResult(new HashMap<String, String>(), Integer.class.getName());

		if (folderName == null) {
			this.errors.reject("sample.list.folder.is.null","The folder name must not be null");
			throw new ApiRequestValidationException(this.errors.getAllErrors());
		}
	}

	public void validateFolderId(final Integer folderId) {
		this.errors = new MapBindingResult(new HashMap<String, String>(), Integer.class.getName());

		if (folderId == null) {
			this.errors.reject("sample.list.parent.id.is.null","The parent Id must not be null");
			throw new ApiRequestValidationException(this.errors.getAllErrors());
		}
	}

	public void validateFolderIdAndProgram(final Integer folderId) {
		this.validateFolderId(folderId);

		// It is assumed that programUUID is set in ContextHolder from API path variable or request parameter
		final String contextProgramUUID = ContextHolder.getCurrentProgram();
		if (StringUtils.isBlank(contextProgramUUID)){
			this.errors.reject("sample.list.program.uuid.is.null","The program UUID must not be null");
			throw new ApiRequestValidationException(this.errors.getAllErrors());
		}

		// FolderID is zero if it's the root crop/program folder
		if (folderId != 0) {
			final SampleList sampleList = this.sampleListServiceMW.getSampleList(folderId);
			// Verify that folder belongs to program in ContextHolder
			if (sampleList != null && sampleList.getProgramUUID() != null && !contextProgramUUID.equals(sampleList.getProgramUUID())){
				this.errors.reject("sample.list.program.uuid.is.invalid","Invalid programUUID for sample list folder");
				throw new ApiRequestValidationException(this.errors.getAllErrors());
			}
		}

	}



}
