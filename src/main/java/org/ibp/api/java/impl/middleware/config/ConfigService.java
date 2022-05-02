package org.ibp.api.java.impl.middleware.config;

import org.generationcp.middleware.api.config.ConfigDTO;
import org.generationcp.middleware.api.config.ConfigPatchRequestDTO;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ConfigService {

	List<ConfigDTO> getConfig(Pageable pageable);

	void modifyConfig(String key, ConfigPatchRequestDTO request);
}
