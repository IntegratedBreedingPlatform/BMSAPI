package org.ibp.api.brapi;

import org.generationcp.middleware.api.brapi.v2.germplasm.PedigreeNodeDTO;

import java.util.List;
import java.util.Map;

public interface PedigreeServiceBrapi {

	List<PedigreeNodeDTO> updatePedigree(Map<String, PedigreeNodeDTO> pedigreeNodeDTOMap);

}
