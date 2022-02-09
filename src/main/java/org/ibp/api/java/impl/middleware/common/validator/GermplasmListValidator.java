package org.ibp.api.java.impl.middleware.common.validator;

import org.apache.commons.lang3.StringUtils;
import org.generationcp.commons.constant.AppConstants;
import org.generationcp.middleware.api.germplasmlist.GermplasmListBasicInfoDTO;
import org.generationcp.middleware.api.germplasmlist.GermplasmListService;
import org.generationcp.middleware.manager.Operation;
import org.generationcp.middleware.manager.api.GermplasmListManager;
import org.generationcp.middleware.pojos.GermplasmList;
import org.ibp.api.Util;
import org.ibp.api.exception.ApiRequestValidationException;
import org.ibp.api.exception.ApiValidationException;
import org.ibp.api.exception.ResourceNotFoundException;
import org.ibp.api.java.impl.middleware.germplasm.GermplasmListTreeServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.BindingResult;
import org.springframework.validation.MapBindingResult;

import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static org.ibp.api.java.impl.middleware.common.validator.BaseValidator.checkArgument;
import static org.ibp.api.java.impl.middleware.common.validator.BaseValidator.checkNotEmpty;
import static org.ibp.api.java.impl.middleware.common.validator.BaseValidator.checkNotNull;

@Component
public class GermplasmListValidator {

	public enum ListNodeType {
		PARENT("parent"),
		FOLDER("folder");

		private final String value;

		ListNodeType(final String value) {
			this.value = value;
		}

		public String getValue() {
			return this.value;
		}
	}

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

	public void validateListMetadata(final GermplasmListBasicInfoDTO germplasmListDto, final String currentProgram) {
		checkNotNull(germplasmListDto, PARAM_NULL, new String[] {"request"});
		checkNotNull(germplasmListDto.getCreationDate(), PARAM_NULL, new String[] {"date"});

		final String description = germplasmListDto.getDescription();
		if (!StringUtils.isBlank(description)) {
			checkArgument(description.length() <= 255, TEXT_FIELD_MAX_LENGTH, new String[] {"description", "255"});
		}

		if (!StringUtils.isBlank(germplasmListDto.getNotes())) {
			checkArgument(germplasmListDto.getNotes().length() <= 65535, TEXT_FIELD_MAX_LENGTH, new String[] {"notes", "65535"});
		}

		final String type = germplasmListDto.getListType();
		checkNotEmpty(type, PARAM_NULL, new String[] {"type"});
		if (this.germplasmListManager.getGermplasmListTypes().stream().noneMatch(listType -> listType.getFcode().equals(type))) {
			throw new ApiValidationException("", "error.germplasmlist.save.type.not.exists", type);
		}
		this.validateListName(currentProgram, germplasmListDto.getListName(), germplasmListDto.getListId());
	}

	//TODO Find by name and parent instead of name and program
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

	public void validateParentFolder(final String parentFolderId) {
		checkNotNull(parentFolderId, PARAM_NULL, new String[] {"parentFolderId"});
	}

	public Optional<GermplasmList> validateFolderId(final String folderId, final String programUUID, final ListNodeType nodeType) {
		this.errors = new MapBindingResult(new HashMap<>(), Integer.class.getName());

		this.validateNodeId(folderId, nodeType);

		if (Util.isPositiveInteger(folderId)) {

			final GermplasmList folder = this.germplasmListService.getGermplasmListById(Integer.parseInt(folderId))
				.orElseThrow(() -> {
					this.errors.reject("list.folder.id.not.exist", "");
					return new ApiRequestValidationException(this.errors.getAllErrors());
				});

			if (!folder.isFolder()) {
				this.errors.reject("list.folder.id.not.exist", "");
				throw new ApiRequestValidationException(this.errors.getAllErrors());
			}

			//verify that folder belongs to the program when it is not a crop folder
			if (!StringUtils.isEmpty(folder.getProgramUUID())) {
				if (StringUtils.isEmpty(programUUID) || !programUUID.equals(folder.getProgramUUID())) {
					this.errors.reject("list.project.mandatory", "");
					throw new ApiRequestValidationException(this.errors.getAllErrors());
				}
			}

			return Optional.of(folder);
		}

		return Optional.empty();
	}

	public void validateNodeId(final String nodeId, final ListNodeType nodeType) {
		this.errors = new MapBindingResult(new HashMap<>(), Integer.class.getName());

		if (!Objects.isNull(nodeId) && !GermplasmListTreeServiceImpl.PROGRAM_LISTS.equals(nodeId) && !GermplasmListTreeServiceImpl.CROP_LISTS.equals(nodeId) && !Util.isPositiveInteger(nodeId)) {
			this.errors.reject("list." + nodeType.getValue() + ".id.invalid", "");
			throw new ApiRequestValidationException(this.errors.getAllErrors());
		}
	}

	public void validateFolderHasNoChildren(final Integer nodeId, final String message) {
		this.errors = new MapBindingResult(new HashMap<>(), Integer.class.getName());

		final List<GermplasmList> listChildren = this.germplasmListManager.getGermplasmListByParentFolderId(nodeId);
		if (!listChildren.isEmpty()) {
			this.errors.reject(message, "");
			throw new ApiRequestValidationException(this.errors.getAllErrors());
		}
	}

}
