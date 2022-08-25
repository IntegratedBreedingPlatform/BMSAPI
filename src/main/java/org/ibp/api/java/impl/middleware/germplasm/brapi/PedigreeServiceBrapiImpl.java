package org.ibp.api.java.impl.middleware.germplasm.brapi;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.generationcp.middleware.api.brapi.v2.germplasm.PedigreeNodeDTO;
import org.generationcp.middleware.api.brapi.v2.germplasm.PedigreeNodeSearchRequest;
import org.ibp.api.brapi.PedigreeServiceBrapi;
import org.ibp.api.brapi.v2.germplasm.PedigreeNodesUpdateResponse;
import org.ibp.api.brapi.v2.germplasm.PedigreeNodesUpdateValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.validation.BindingResult;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class PedigreeServiceBrapiImpl implements PedigreeServiceBrapi {

	@Autowired
	private PedigreeNodesUpdateValidator pedigreeNodesUpdateValidator;

	@Autowired
	private org.generationcp.middleware.api.brapi.PedigreeServiceBrapi pedigreeMiddlewareServiceBrapi;

	@Override
	public PedigreeNodesUpdateResponse updatePedigreeNodes(final Map<String, PedigreeNodeDTO> pedigreeNodeDTOMap) {

		final int updateListSize = pedigreeNodeDTOMap.size();
		final PedigreeNodesUpdateResponse pedigreeNodesUpdateResponse = new PedigreeNodesUpdateResponse();
		final BindingResult validationErrors = this.pedigreeNodesUpdateValidator.prunePedigreeNodesForUpdate(pedigreeNodeDTOMap);

		if (!MapUtils.isEmpty(pedigreeNodeDTOMap)) {
			final Multimap<String, Object[]> conflictErrors = ArrayListMultimap.create();
			final Set<String> updatedGermplasmDbIds =
				this.pedigreeMiddlewareServiceBrapi.updatePedigreeNodes(pedigreeNodeDTOMap, conflictErrors);

			final List<PedigreeNodeDTO> result = new ArrayList<>();
			if (!CollectionUtils.isEmpty(updatedGermplasmDbIds)) {
				final PedigreeNodeSearchRequest pedigreeNodeSearchRequest = new PedigreeNodeSearchRequest();
				pedigreeNodeSearchRequest.setGermplasmDbIds(new ArrayList<>(updatedGermplasmDbIds));
				result.addAll(this.pedigreeMiddlewareServiceBrapi.searchPedigreeNodes(pedigreeNodeSearchRequest, null));
			}

			// Add the middleware conflict errors if there's any
			conflictErrors.entries().forEach(erorrEntry -> validationErrors.reject(erorrEntry.getKey(), erorrEntry.getValue(), ""));
			pedigreeNodesUpdateResponse.setEntityList(result);
			pedigreeNodesUpdateResponse.setUpdatedSize(result.size());
		} else {
			pedigreeNodesUpdateResponse.setEntityList(new ArrayList<>());
			pedigreeNodesUpdateResponse.setUpdatedSize(0);
		}
		pedigreeNodesUpdateResponse.setUpdateListSize(updateListSize);
		pedigreeNodesUpdateResponse.setErrors(validationErrors.getAllErrors());
		return pedigreeNodesUpdateResponse;
	}

	@Override
	public List<PedigreeNodeDTO> searchPedigreeNodes(final PedigreeNodeSearchRequest pedigreeNodeSearchRequest, final Pageable pageable) {
		return this.pedigreeMiddlewareServiceBrapi.searchPedigreeNodes(pedigreeNodeSearchRequest, pageable);
	}

	@Override
	public long countPedigreeNodes(final PedigreeNodeSearchRequest pedigreeNodeSearchRequest) {
		return this.pedigreeMiddlewareServiceBrapi.countPedigreeNodes(pedigreeNodeSearchRequest);
	}

}
