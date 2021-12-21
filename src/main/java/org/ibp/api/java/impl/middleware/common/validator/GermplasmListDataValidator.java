package org.ibp.api.java.impl.middleware.common.validator;

import org.generationcp.middleware.api.germplasmlist.data.GermplasmListDataSearchRequest;
import org.generationcp.middleware.api.germplasmlist.data.GermplasmListDataSearchResponse;
import org.generationcp.middleware.api.germplasmlist.data.GermplasmListDataService;
import org.ibp.api.exception.ApiRequestValidationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.BindingResult;
import org.springframework.validation.MapBindingResult;

import java.util.HashMap;
import java.util.List;

@Component
public class GermplasmListDataValidator {

	@Autowired
	private GermplasmListDataService germplasmListDataService;

	public void verifyListDataIdsExist(final Integer listId, final List<Integer> listDataIds) {
		final BindingResult errors = new MapBindingResult(new HashMap<>(), GermplasmListDataValidator.class.getName());
		final GermplasmListDataSearchRequest germplasmListDataSearchRequest = new GermplasmListDataSearchRequest();
		germplasmListDataSearchRequest.setListDataIds(listDataIds);
		final List<GermplasmListDataSearchResponse> responseList =
			this.germplasmListDataService.searchGermplasmListData(listId, germplasmListDataSearchRequest, null);

		if (listDataIds.size() != responseList.size()) {
			errors.reject("germplasm.list.data.ids.not.exist", "");
			throw new ApiRequestValidationException(errors.getAllErrors());
		}

	}
}
