package org.ibp.api.java.impl.middleware.common.validator;

import org.apache.commons.lang3.StringUtils;
import org.generationcp.commons.constant.AppConstants;
import org.generationcp.middleware.api.germplasmlist.GermplasmListMetadataRequest;
import org.generationcp.middleware.api.germplasmlist.GermplasmListService;
import org.generationcp.middleware.manager.Operation;
import org.generationcp.middleware.manager.api.GermplasmListManager;
import org.generationcp.middleware.pojos.GermplasmList;
import org.ibp.api.Util;
import org.ibp.api.exception.ApiRequestValidationException;
import org.ibp.api.exception.ApiValidationException;
import org.ibp.api.exception.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.BindingResult;
import org.springframework.validation.MapBindingResult;

import java.util.HashMap;
import java.util.List;

import static org.ibp.api.java.impl.middleware.common.validator.BaseValidator.checkArgument;
import static org.ibp.api.java.impl.middleware.common.validator.BaseValidator.checkNotEmpty;
import static org.ibp.api.java.impl.middleware.common.validator.BaseValidator.checkNotNull;

@Component
public class GermplasmListValidator {

	public static final int NAME_MAX_LENGTH = 50;
	public static final String PARAM_NULL = "param.null";
	public static final String TEXT_FIELD_MAX_LENGTH = "text.field.max.length";

	private BindingResult errors;

	@Autowired
	private GermplasmListService germplasmListService;

	@Autowired
	private GermplasmListManager germplasmListManager;

	public GermplasmList validateGermplasmList(final Integer germplasmListId) {
		this.errors = new MapBindingResult(new HashMap<>(), GermplasmListValidator.class.getName());

		if (!Util.isPositiveInteger(String.valueOf(germplasmListId))) {
			this.errors.reject("list.id.invalid", new String[] {String.valueOf(germplasmListId)}, "");
			throw new ResourceNotFoundException(this.errors.getAllErrors().get(0));
		}

		return this.germplasmListService.getGermplasmListById(germplasmListId)
			.orElseThrow(() -> {
				this.errors.reject("list.id.invalid", new String[] {germplasmListId.toString()}, "");
				return new ResourceNotFoundException(this.errors.getAllErrors().get(0));
			});
	}

	public void validateFolderName(final String folderName) {
		this.errors = new MapBindingResult(new HashMap<>(), GermplasmListValidator.class.getName());

		if (StringUtils.isEmpty(folderName)) {
			this.errors.reject("list.folder.empty", "");
			throw new ApiRequestValidationException(this.errors.getAllErrors());
		}

		if (folderName.length() > NAME_MAX_LENGTH) {
			this.errors.reject("list.folder.name.too.long", new Object[] { NAME_MAX_LENGTH }, "");
			throw new ApiRequestValidationException(this.errors.getAllErrors());
		}
	}

	public void validateListMetadata(final GermplasmListMetadataRequest request, final String currentProgram,
		final Integer listId) {
		checkNotNull(request, PARAM_NULL, new String[] {"request"});
		checkNotNull(request.getDate(), PARAM_NULL, new String[] {"date"});

		final String description = request.getDescription();
		checkNotEmpty(description, PARAM_NULL, new String[] {"description"});
		checkArgument(description.length() <= 255, TEXT_FIELD_MAX_LENGTH, new String[] {"description", "255"});

		if (!StringUtils.isBlank(request.getNotes())) {
			checkArgument(request.getNotes().length() <= 65535, TEXT_FIELD_MAX_LENGTH, new String[] {"notes", "65535"});
		}

		final String type = request.getType();
		checkNotEmpty(type, PARAM_NULL, new String[] {"type"});
		if (this.germplasmListManager.getGermplasmListTypes().stream().noneMatch(listType -> listType.getFcode().equals(type))) {
			throw new ApiValidationException("", "error.germplasmlist.save.type.not.exists", type);
		}
		this.validateListName(currentProgram, request.getName(), listId);
	}

	private void validateListName(final String currentProgram, final String name, final Integer listId) {
		checkNotEmpty(name, "param.null", new String[] {"name"});
		checkArgument(name.length() <= NAME_MAX_LENGTH, TEXT_FIELD_MAX_LENGTH, new String[] {"name", "50"});
		if (AppConstants.CROP_LISTS.getString().equals(name)) {
			throw new ApiValidationException("", "error.list.name.invalid", AppConstants.CROP_LISTS.getString());
		}
		if (AppConstants.PROGRAM_LISTS.getString().equals(name)) {
			throw new ApiValidationException("", "error.list.name.invalid", AppConstants.PROGRAM_LISTS.getString());
		}
		final List<GermplasmList>
			germplasmListByName = this.germplasmListManager.getGermplasmListByName(name, currentProgram, 0, 1, Operation.EQUAL);
		if (!germplasmListByName.isEmpty() && (listId == null || !listId.equals(germplasmListByName.get(0).getId()))) {
			throw new ApiValidationException("", "error.list.name.exists");
		}
	}

	public void validateNotSameFolderNameInParent(final String folderName, final Integer parent, final String programUUID) {
		this.errors = new MapBindingResult(new HashMap<>(), GermplasmListValidator.class.getName());

		this.germplasmListService.getGermplasmListByParentAndName(folderName, parent, programUUID)
			.ifPresent(germplasmList -> {
				this.errors.reject("list.folder.name.exists", "");
				throw new ApiRequestValidationException(this.errors.getAllErrors());
			});
	}

	public void validateListIsNotAFolder(final GermplasmList germplasmList) {
		this.errors = new MapBindingResult(new HashMap<>(), Integer.class.getName());

		if (germplasmList.isFolder()) {
			this.errors.reject("list.invalid", "");
			throw new ApiRequestValidationException(this.errors.getAllErrors());
		}

	}

	public void validateListIsUnlocked(final GermplasmList germplasmList) {
		if (germplasmList.isLockedList()) {
			this.errors.reject("list.locked", "");
			throw new ApiRequestValidationException(this.errors.getAllErrors());
		}
	}

}
