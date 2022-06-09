package org.ibp.api.java.impl.middleware.germplasm.brapi;

import org.generationcp.middleware.api.brapi.v2.germplasm.PedigreeNodeDTO;
import org.ibp.api.brapi.PedigreeServiceBrapi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
@Transactional
public class PedigreeServiceBrapiImpl implements PedigreeServiceBrapi {

	@Autowired
	private org.generationcp.middleware.api.brapi.PedigreeServiceBrapi pedigreeMiddlewareServiceBrapi;

	@Override
	public List<PedigreeNodeDTO> updatePedigree(final Map<String, PedigreeNodeDTO> pedigreeNodeDTOMap) {
		// TODO: Add validation
		return this.pedigreeMiddlewareServiceBrapi.updatePedigreeNodes(pedigreeNodeDTOMap);
	}
}
