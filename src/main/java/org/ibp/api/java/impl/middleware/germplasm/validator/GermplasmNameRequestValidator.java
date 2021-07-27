package org.ibp.api.java.impl.middleware.germplasm.validator;

import com.google.common.collect.Lists;
import org.apache.commons.lang.StringUtils;
import org.generationcp.commons.util.DateUtil;
import org.generationcp.middleware.api.germplasm.GermplasmNameService;
import org.generationcp.middleware.api.nametype.GermplasmNameTypeDTO;
import org.generationcp.middleware.domain.germplasm.GermplasmNameDto;
import org.generationcp.middleware.domain.germplasm.GermplasmNameRequestDto;
import org.generationcp.middleware.pojos.Name;
import org.ibp.api.exception.ApiRequestValidationException;
import org.ibp.api.java.germplasm.GermplasmService;
import org.ibp.api.java.impl.middleware.common.validator.GermplasmValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.validation.BindingResult;
import org.springframework.validation.MapBindingResult;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class GermplasmNameRequestValidator {

    private static final Integer NAME_MAX_LENGTH = 255;
    public static final String PUI = "PUI";
    public static final String GERMPLASM_PUI_DUPLICATE = "germplasm.name.pui.duplicate";
    private static final String HAS_EXISTING_PUI_NAME = "germplasm.has.pui";

    private BindingResult errors;

    @Autowired
    private GermplasmValidator germplasmValidator;

    @Autowired
    private GermplasmNameService germplasmNameService;

    @Autowired
    private GermplasmService germplasmService;

    public void validate(final GermplasmNameRequestDto germplasmNameRequestDto, final Integer gid, final Integer nameId) {
        this.errors = new MapBindingResult(new HashMap<>(), GermplasmNameRequestDto.class.getName());
        this.germplasmValidator.validateGermplasmId(this.errors, gid);

        if (this.errors.hasErrors()) {
            throw new ApiRequestValidationException(this.errors.getAllErrors());
        }

        if (nameId != null) {
            final Name name = this.germplasmNameService.getNameById(nameId);
            this.validateNameBelongsToGermplasm(gid, name);

            if (!StringUtils.isBlank(germplasmNameRequestDto.getNameTypeCode())) {
                this.validateNameTypeCode(germplasmNameRequestDto);
            }
            if (germplasmNameRequestDto.getName() != null) {
                this.validateNameLength(germplasmNameRequestDto);
            }

            if (germplasmNameRequestDto.getDate() != null) {
                this.validateNameDate(germplasmNameRequestDto);
            }

            if (germplasmNameRequestDto.isPreferredName() != null) {
                this.validatePreferredNameUpdatable(germplasmNameRequestDto, name);
            }
            this.enforcePUIUniqueness(germplasmNameRequestDto, name);
            this.enforceSinglePUINameForGermplasm(germplasmNameRequestDto, gid, name);
        } else {
            this.validateNameTypeCode(germplasmNameRequestDto);
            this.validateNameLength(germplasmNameRequestDto);
            this.validateNameDate(germplasmNameRequestDto);
            this.validatePreferredName(germplasmNameRequestDto);
            this.enforcePUIUniqueness(germplasmNameRequestDto);
            this.enforceSinglePUINameForGermplasm(germplasmNameRequestDto, gid);

        }
    }

    public void validatePreferredName(final GermplasmNameRequestDto germplasmNameRequestDto) {
        if (germplasmNameRequestDto.isPreferredName() == null) {
            this.errors.reject("germplasm.name.preferred.required", "");
            throw new ApiRequestValidationException(this.errors.getAllErrors());
        }
    }

    public void validateNameDeletable(final Integer gid, final Integer nameId) {
        this.errors = new MapBindingResult(new HashMap<>(), GermplasmNameRequestDto.class.getName());
        this.germplasmValidator.validateGermplasmId(this.errors, gid);

        if (this.errors.hasErrors()) {
            throw new ApiRequestValidationException(this.errors.getAllErrors());
        }

        final Name name = this.germplasmNameService.getNameById(nameId);
        this.validateNameBelongsToGermplasm(gid, name);
        this.validateDeletepreferredName(name);

    }

    public void validateNameBelongsToGermplasm(final Integer gid, final Integer nameId) {
        final Name name = this.germplasmNameService.getNameById(nameId);
        this.validateNameBelongsToGermplasm(gid, name);
    }

    protected void validateNameBelongsToGermplasm(final Integer gid, final Name name) {
        if (name == null || name.getGermplasm() == null || !name.getGermplasm().getGid().equals(gid)) {
            this.errors.reject("germplasm.name.invalid", "");
            throw new ApiRequestValidationException(this.errors.getAllErrors());

        }
    }

    protected void validateDeletepreferredName(final Name name) {
        if (Integer.valueOf(1).equals(name.getNstat())) {
            this.errors.reject("germplasm.name.preferred.invalid", "");
            throw new ApiRequestValidationException(this.errors.getAllErrors());

        }
    }

    protected void validatePreferredNameUpdatable(final GermplasmNameRequestDto germplasmNameRequestDto,
                                                  final Name name) {
        if (!Boolean.TRUE.equals(germplasmNameRequestDto.isPreferredName()) && name.getNstat().equals(1)) {
            this.errors.reject("germplasm.name.preferred.invalid", "");
            throw new ApiRequestValidationException(this.errors.getAllErrors());
        }
    }

    protected void validateNameLength(final GermplasmNameRequestDto germplasmNameRequestDto) {
        if (StringUtils.isBlank(germplasmNameRequestDto.getName())) {
            this.errors.reject("germplasm.name.required", "");
            throw new ApiRequestValidationException(this.errors.getAllErrors());

        }

        if (germplasmNameRequestDto.getName().length() > NAME_MAX_LENGTH) {
            this.errors.reject("germplasm.name.length", "");
            throw new ApiRequestValidationException(this.errors.getAllErrors());

        }
    }

    protected void validateNameDate(final GermplasmNameRequestDto germplasmNameRequestDto) {
        if (germplasmNameRequestDto.getDate() == null) {
            this.errors.reject("germplasm.name.date.required", "");
            throw new ApiRequestValidationException(this.errors.getAllErrors());
        }

        if (!DateUtil.isValidDate(germplasmNameRequestDto.getDate())) {
            this.errors.reject("germplasm.name.date.invalid", new Object[]{
                            germplasmNameRequestDto.getDate()},
                    "Invalid date value found.");
            throw new ApiRequestValidationException(this.errors.getAllErrors());
        }
    }

    protected void enforcePUIUniqueness(final GermplasmNameRequestDto germplasmNameRequestDto) {
        if (PUI.equalsIgnoreCase(germplasmNameRequestDto.getNameTypeCode()) && !StringUtils.isEmpty(germplasmNameRequestDto.getName())
                && this.puiExists(germplasmNameRequestDto.getName())) {
            this.errors.reject(GERMPLASM_PUI_DUPLICATE, new Object[]{
                    germplasmNameRequestDto.getName()}, "");
            throw new ApiRequestValidationException(this.errors.getAllErrors());
        }
    }

    protected void enforcePUIUniqueness(final GermplasmNameRequestDto germplasmNameRequestDto, final Name existingName) {
        if (!StringUtils.isEmpty(germplasmNameRequestDto.getName()) && !existingName.getNval().equals(germplasmNameRequestDto.getName())) {
            // Case 1: Request name type = PUI, name value being updated --> Validate that name in request doesn't exist as PUI
            this.enforcePUIUniqueness(germplasmNameRequestDto);

            // Case 2: Request name type = empty, name value is being updated and name type in DB is PUI --> validate that new name is unique
            if (StringUtils.isEmpty(germplasmNameRequestDto.getNameTypeCode())) {
                final List<GermplasmNameTypeDTO> puiNameType =
                        this.germplasmService.filterGermplasmNameTypes(Collections.singleton(PUI));
                if (!CollectionUtils.isEmpty(puiNameType) && puiNameType.get(0).getId().equals(existingName.getTypeId()) && this
                        .puiExists(germplasmNameRequestDto.getName())) {
                    this.errors.reject(GERMPLASM_PUI_DUPLICATE, new Object[]{
                            germplasmNameRequestDto.getName()}, "");
                    throw new ApiRequestValidationException(this.errors.getAllErrors());
                }
            }


            // Case 3: Request name type = PUI, name = empty --> Validate that existing name in DB is unique
        } else if (PUI.equalsIgnoreCase(germplasmNameRequestDto.getNameTypeCode()) && StringUtils.isEmpty(germplasmNameRequestDto.getName())
                && this.puiExists(existingName.getNval())) {
            this.errors.reject(GERMPLASM_PUI_DUPLICATE, new Object[]{
                    existingName.getNval()}, "");
            throw new ApiRequestValidationException(this.errors.getAllErrors());
        }
    }


    private boolean puiExists(final String pui) {
        return !this.germplasmNameService.getExistingGermplasmPUIs(Collections.singletonList(pui)).isEmpty();
    }

    private void enforceSinglePUINameForGermplasm(final GermplasmNameRequestDto germplasmNameRequestDto, final Integer gid) {
        if (PUI.equalsIgnoreCase(germplasmNameRequestDto.getNameTypeCode())) {
            final List<GermplasmNameDto> puiNames = this.germplasmNameService.getGermplasmNamesByGids(Lists.newArrayList(gid))
                    .stream().filter(name -> name.getNameTypeCode().equalsIgnoreCase(PUI)).collect(Collectors.toList());
            if(!CollectionUtils.isEmpty(puiNames)) {
                this.errors.reject(HAS_EXISTING_PUI_NAME, "");
                throw new ApiRequestValidationException(this.errors.getAllErrors());
            }
        }
    }

    private void enforceSinglePUINameForGermplasm(final GermplasmNameRequestDto germplasmNameRequestDto, final Integer gid, final Name existingName) {
        final List<GermplasmNameTypeDTO> puiNameType =
                this.germplasmService.filterGermplasmNameTypes(Collections.singleton(PUI));
        // Validate that there's no existing PUI name when changing a germplasm name type to PUI
        if (!CollectionUtils.isEmpty(puiNameType) && !puiNameType.get(0).getId().equals(existingName.getTypeId())
            && PUI.equalsIgnoreCase(germplasmNameRequestDto.getNameTypeCode())){
            final List<GermplasmNameDto> puiNames = this.germplasmNameService.getGermplasmNamesByGids(Lists.newArrayList(gid))
                    .stream().filter(name -> name.getNameTypeCode().equalsIgnoreCase(PUI)).collect(Collectors.toList());
            if(!CollectionUtils.isEmpty(puiNames)) {
                this.errors.reject(HAS_EXISTING_PUI_NAME, "");
                throw new ApiRequestValidationException(this.errors.getAllErrors());
            }
        }
    }

    protected void validateNameTypeCode(final GermplasmNameRequestDto germplasmNameRequestDto) {
        final List<GermplasmNameTypeDTO> germplasmNameTypeDTOs = this.germplasmService.filterGermplasmNameTypes(Collections.singleton(germplasmNameRequestDto.getNameTypeCode()));
        if (germplasmNameTypeDTOs == null || germplasmNameTypeDTOs.isEmpty()) {
            this.errors.reject("germplasm.name.type.invalid", "");
            throw new ApiRequestValidationException(this.errors.getAllErrors());
        }
    }

    public void setGermplasmNameService(final GermplasmNameService germplasmNameService) {
        this.germplasmNameService = germplasmNameService;
    }

    public GermplasmNameService getGermplasmNameService() {
        return this.germplasmNameService;
    }
}
