package org.ibp.api.java.impl.middleware.inventory.manager.validator;

import org.generationcp.middleware.domain.inventory.manager.LotDto;
import org.generationcp.middleware.domain.inventory.manager.LotItemDto;
import org.generationcp.middleware.manager.api.GermplasmDataManager;
import org.generationcp.middleware.pojos.Germplasm;
import org.ibp.api.exception.ApiRequestValidationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.BindingResult;
import org.springframework.validation.MapBindingResult;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Component
public class LotListValidator {

	private BindingResult errors;

	@Autowired
	private GermplasmDataManager germplasmDataManager;

	public void validate(final List<LotItemDto> lotList) {
		this.errors = new MapBindingResult(new HashMap<String, String>(), LotDto.class.getName());

		if (lotList == null) {
			errors.reject("lot.input.list.null", "");
			throw new ApiRequestValidationException(this.errors.getAllErrors());
		}

		//Validate that none of the elements in the list is null
		if (lotList.stream().filter(Objects::nonNull).collect(Collectors.toList()).size() != lotList.size()){
			errors.reject("lot.input.list.item.null", "");
			throw new ApiRequestValidationException(this.errors.getAllErrors());
		}

		//Validate gids. All of them must: 1) not be null, 2) exists
		final List<Integer> gids = lotList.stream().map(LotItemDto::getGid).distinct().collect(Collectors.toList());
		if (gids.stream().filter(Objects::nonNull).collect(Collectors.toList()).size() != gids.size()){
			errors.reject("lot.input.list.gid.null", "");
			throw new ApiRequestValidationException(this.errors.getAllErrors());
		}

		final List<Germplasm> germplasms = germplasmDataManager.getGermplasms(gids);
		if (germplasms.size()!= gids.size()) {
			final List<Integer> existingGids = germplasms.stream().map(Germplasm::getGid).collect(Collectors.toList());
			List<Integer> invalidGids = new ArrayList<>(gids);
			invalidGids.removeAll(existingGids);
			errors.reject("lot.input.invalid.gids", new String[]{this.buildErrorMessageFromList(invalidGids)}, "");
			throw new ApiRequestValidationException(this.errors.getAllErrors());
		}

		if (this.errors.hasErrors()) {
			throw new ApiRequestValidationException(this.errors.getAllErrors());
		}

	}

	private <T> String buildErrorMessageFromList(List<T> elements) {
		final StringBuilder stringBuilder = new StringBuilder();

		stringBuilder.append(elements
			.stream()
			.limit(3).map(Object::toString).collect(Collectors.joining(",")));

		if (elements.size() > 3) {
			stringBuilder.append(" and ").append(elements.size() - 3).append(" more");
		}

		return stringBuilder.toString();
	}

}
