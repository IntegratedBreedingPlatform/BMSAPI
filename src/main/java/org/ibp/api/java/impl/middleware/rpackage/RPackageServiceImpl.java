package org.ibp.api.java.impl.middleware.rpackage;

import org.generationcp.middleware.manager.api.WorkbenchDataManager;
import org.generationcp.middleware.pojos.workbench.RCall;
import org.generationcp.middleware.pojos.workbench.RCallParameter;
import org.ibp.api.domain.rpackage.RCallDTO;
import org.ibp.api.java.rpackage.RPackageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional
public class RPackageServiceImpl implements RPackageService {

	@Autowired
	private WorkbenchDataManager workbenchDataManager;

	@Override
	public List<RCallDTO> getAllRCalls() {
		return this.mapToDTO(this.workbenchDataManager.getAllRCalls());
	}

	@Override
	public List<RCallDTO> getRCallsByPackageId(final Integer packageId) {
		return this.mapToDTO(this.workbenchDataManager.getRCallsByPackageId(packageId));
	}

	private List<RCallDTO> mapToDTO(final List<RCall> rCalls) {
		final List<RCallDTO> rCallDTOS = new ArrayList<>();
		for (final RCall rCall : rCalls) {
			rCallDTOS.add(this.map(rCall));
		}
		return rCallDTOS;
	}

	private RCallDTO map(final RCall rCall) {
		final RCallDTO rCallDTO = new RCallDTO();
		rCallDTO.setDescription(rCall.getDescription());
		rCallDTO.setEndpoint(rCall.getrPackage().getEndpoint());
		final Map<String, String> parameters = new HashMap<>();
		for (final RCallParameter rCallParameter : rCall.getrCallParameters()) {
			parameters.put(rCallParameter.getKey(), rCallParameter.getValue());
		}
		rCallDTO.setParameters(parameters);
		return rCallDTO;
	}

}
