package org.ibp.api.java.impl.middleware.config;

import org.generationcp.middleware.api.config.ConfigDTO;
import org.generationcp.middleware.api.config.ConfigPatchRequestDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class ConfigServiceImpl implements ConfigService {

	@Autowired
	private org.generationcp.middleware.api.config.ConfigService configService;

	@Override
	public List<ConfigDTO> getConfig(final Pageable pageable) {
		return this.configService.getConfig(pageable).stream().map(ConfigDTO::new).collect(Collectors.toList());
	}

	@Override
	public void modifyConfig(final String key, final ConfigPatchRequestDTO request) {
		this.configService.modifyConfig(key, request);
	}
}
