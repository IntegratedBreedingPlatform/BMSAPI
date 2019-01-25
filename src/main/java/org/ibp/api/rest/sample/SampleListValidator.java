package org.ibp.api.rest.sample;

import org.apache.commons.lang3.StringUtils;
import org.ibp.api.exception.ApiRequestValidationException;
import org.springframework.stereotype.Component;
import org.springframework.validation.BindingResult;
import org.springframework.validation.MapBindingResult;

import java.util.HashMap;

@Component
public class SampleListValidator {

	private BindingResult errors;

	public void validateSampleList(final SampleListDto sampleListDto) {

		this.errors = new MapBindingResult(new HashMap<String, String>(), Integer.class.getName());

		if (sampleListDto.getInstanceIds() == null) {
			this.errors.reject("sample.list.instance.list.must.not.be.null","The Instance List must not be null");
		}
		if (sampleListDto.getInstanceIds().isEmpty()) {
			this.errors.reject("sample.list.instance.list.must.not.empty","The Instance List must not be empty");
		}
		if (sampleListDto.getSelectionVariableId() == null) {
			this.errors.reject("sample.list.selection.variable.id.must.not.empty", "The Selection Variable Id must not be empty");
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

	public void validateFolderName(final String folderName) {

		if (folderName == null) {
			this.errors.reject("sample.list.folder.is.null","The folder name must not be null");
			throw new ApiRequestValidationException(this.errors.getAllErrors());
		}
	}

	public void validateFolderId(final Integer parentId) {
		if (parentId == null) {
			this.errors.reject("sample.list.parent.id.is.null","The parent Id must not be null");
			throw new ApiRequestValidationException(this.errors.getAllErrors());
		}
	}

	public void validateProgramUUID(final String programUUID) {
		if (programUUID == null) {
			this.errors.reject("sample.list.program.uuid.is.null","The program UUID must not be null");
			throw new ApiRequestValidationException(this.errors.getAllErrors());
		}
	}

}
