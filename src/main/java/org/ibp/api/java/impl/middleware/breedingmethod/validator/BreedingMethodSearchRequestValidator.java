package org.ibp.api.java.impl.middleware.breedingmethod.validator;

import org.apache.commons.lang3.StringUtils;
import org.generationcp.middleware.api.breedingmethod.BreedingMethodSearchRequest;
import org.generationcp.middleware.api.program.ProgramDTO;
import org.generationcp.middleware.pojos.MethodType;
import org.ibp.api.exception.ApiRequestValidationException;
import org.ibp.api.java.impl.middleware.common.validator.ProgramValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.validation.BindingResult;
import org.springframework.validation.MapBindingResult;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class BreedingMethodSearchRequestValidator {

  @Autowired
  private ProgramValidator programValidator;

  public void validate(final String crop, final BreedingMethodSearchRequest request) {
      final BindingResult errors = new MapBindingResult(new HashMap<>(), Integer.class.getName());

      if (request.getFilterFavoriteProgramUUID() != null && request
          .getFilterFavoriteProgramUUID() && StringUtils.isEmpty(request.getFavoriteProgramUUID())) {
        errors.reject("breeding.methods.favorite.requires.program", "");
        throw new ApiRequestValidationException(errors.getAllErrors());
      }

      if (!CollectionUtils.isEmpty(request.getMethodTypes()) ) {
        final List<String> allMethodTypes = Arrays.stream(MethodType.values()).map(MethodType::getCode).collect(
            Collectors.toList());
        final boolean hasInvalidMethodType = request.getMethodTypes().stream().anyMatch(type -> !allMethodTypes.contains(type));
        if (hasInvalidMethodType) {
          errors.reject("invalid.breeding.method.type", "");
          throw new ApiRequestValidationException(errors.getAllErrors());
        }
      }

      if (request.getFavoriteProgramUUID() != null) {
        this.programValidator.validate(new ProgramDTO(crop, request.getFavoriteProgramUUID()), errors);
        if (errors.hasErrors()) {
          throw new ApiRequestValidationException(errors.getAllErrors());
        }
      }


  }

}
