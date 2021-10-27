package org.ibp.api.java.impl.middleware.germplasm;

import org.apache.commons.collections.CollectionUtils;
import org.generationcp.middleware.api.germplasmlist.GermplasmListColumnDTO;
import org.generationcp.middleware.api.germplasmlist.GermplasmListMeasurementVariableDTO;
import org.generationcp.middleware.api.germplasmlist.data.GermplasmListDataSearchRequest;
import org.generationcp.middleware.api.germplasmlist.data.GermplasmListDataSearchResponse;
import org.generationcp.middleware.api.germplasmlist.data.GermplasmListDataUpdateViewDTO;
import org.generationcp.middleware.api.germplasmlist.data.GermplasmListReorderEntriesRequest;
import org.generationcp.middleware.pojos.GermplasmList;
import org.ibp.api.exception.ApiRequestValidationException;
import org.ibp.api.java.germplasm.GermplasmListDataService;
import org.ibp.api.java.impl.middleware.common.validator.BaseValidator;
import org.ibp.api.java.impl.middleware.common.validator.GermplasmListValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindingResult;
import org.springframework.validation.MapBindingResult;

import java.util.HashMap;
import java.util.List;

@Service
@Transactional
public class GermplasmListDataServiceImpl implements GermplasmListDataService {

	@Autowired
	private org.generationcp.middleware.api.germplasmlist.data.GermplasmListDataService germplasmListDataService;

	@Autowired
	public GermplasmListValidator germplasmListValidator;

	private BindingResult errors;

	@Override
	public List<GermplasmListDataSearchResponse> searchGermplasmListData(final Integer listId, final GermplasmListDataSearchRequest request,
		final Pageable pageable) {
		return this.germplasmListDataService.searchGermplasmListData(listId, request, pageable);
	}

	@Override
	public long countSearchGermplasmListData(final Integer listId, final GermplasmListDataSearchRequest request) {
		return this.germplasmListDataService.countSearchGermplasmListData(listId, request);
	}

	@Override
	public List<GermplasmListColumnDTO> getGermplasmListColumns(final Integer listId, final String programUUID) {
		this.germplasmListValidator.validateGermplasmList(listId);

		return this.germplasmListDataService.getGermplasmListColumns(listId, programUUID);
	}

	@Override
	public List<GermplasmListMeasurementVariableDTO> getGermplasmListDataTableHeader(final Integer listId, final String programUUID) {
		this.germplasmListValidator.validateGermplasmList(listId);

		return this.germplasmListDataService.getGermplasmListDataTableHeader(listId, programUUID);
	}

	@Override
	public void updateGermplasmListDataView(final Integer listId, final List<GermplasmListDataUpdateViewDTO> view) {
		final GermplasmList germplasmList = this.germplasmListValidator.validateGermplasmList(listId);
		this.germplasmListValidator.validateListIsUnlocked(germplasmList);

		this.germplasmListDataService.updateGermplasmListDataView(listId, view);
	}

	@Override
	public void reOrderEntries(final Integer listId, final GermplasmListReorderEntriesRequest request) {
		BaseValidator.checkNotNull(request,"list.reorder.input.null");

		this.errors = new MapBindingResult(new HashMap<>(), String.class.getName());

		if (CollectionUtils.isEmpty(request.getSelectedEntries())) {
			this.errors.reject("list.reorder.selected.entries.empty", "");
			throw new ApiRequestValidationException(this.errors.getAllErrors());
		}

		final GermplasmList germplasmList = this.germplasmListValidator.validateGermplasmList(listId);
		this.germplasmListValidator.validateListIsUnlocked(germplasmList);

		final int position;
		final List<Integer> selectedEntries = request.getSelectedEntries();
		final Long totalEntries = this.germplasmListDataService.countByListId(listId);
		if (request.getAtTheEndPosition() != null && request.getAtTheEndPosition()) {
			position = totalEntries.intValue() - selectedEntries.size() + 1;
		} else {
			final Integer entryNumberPosition = request.getEntryNumberPosition();
			final long maxSelectionPosition = totalEntries - selectedEntries.size() + 1;
			if (entryNumberPosition == null || entryNumberPosition <= 0) {
				this.errors.reject("list.reorder.invalid.selected.position.number",
					new String[] { String.valueOf(maxSelectionPosition) }, "");
				throw new ApiRequestValidationException(this.errors.getAllErrors());
			}

			position = entryNumberPosition;

			final long maxPositionNumber = entryNumberPosition + selectedEntries.size() - 1;
			if (maxPositionNumber > totalEntries) {
				this.errors.reject("list.reorder.invalid.selected.position", new String[] { String.valueOf(entryNumberPosition),
					String.valueOf(totalEntries), String.valueOf(maxSelectionPosition)  }, "");
				throw new ApiRequestValidationException(this.errors.getAllErrors());
			}
		}

		this.germplasmListDataService.reOrderEntries(listId, selectedEntries, position);
	}

}
