package org.ibp.api.rest.sample;

import org.generationcp.middleware.domain.sample.SampleDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class SampleServiceImpl implements SampleService {

	@Autowired
	org.generationcp.middleware.service.api.SampleService sampleService;

	@Override
	public List<SampleDTO> getSamples(final String plotId) {
		return this.sampleService.getSamples(plotId);
	}
}
