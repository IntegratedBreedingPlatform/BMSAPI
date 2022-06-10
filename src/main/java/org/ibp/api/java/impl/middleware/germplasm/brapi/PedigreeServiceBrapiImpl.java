package org.ibp.api.java.impl.middleware.germplasm.brapi;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import org.apache.commons.collections.MapUtils;
import org.generationcp.middleware.api.brapi.v2.germplasm.PedigreeNodeDTO;
import org.ibp.api.brapi.PedigreeServiceBrapi;
import org.ibp.api.brapi.v2.germplasm.PedigreeNodesUpdateResponse;
import org.ibp.api.brapi.v2.germplasm.PedigreeNodesUpdateValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindingResult;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@Transactional
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
			final List<PedigreeNodeDTO> result =
				this.pedigreeMiddlewareServiceBrapi.updatePedigreeNodes(pedigreeNodeDTOMap, conflictErrors);
			// Add the middleware conflict errors if there's any
			conflictErrors.entries().forEach(erorrEntry -> validationErrors.reject(erorrEntry.getKey(), erorrEntry.getValue(), ""));
			pedigreeNodesUpdateResponse.setEntityList(result);
			pedigreeNodesUpdateResponse.setUpdatedSize(result.size());
			pedigreeNodesUpdateResponse.setUpdateListSize(updateListSize);
		} else {
			pedigreeNodesUpdateResponse.setEntityList(new ArrayList<>());
			pedigreeNodesUpdateResponse.setUpdatedSize(0);
			pedigreeNodesUpdateResponse.setUpdateListSize(updateListSize);
		}
		pedigreeNodesUpdateResponse.setErrors(validationErrors.getAllErrors());
		return pedigreeNodesUpdateResponse;
	}
}
