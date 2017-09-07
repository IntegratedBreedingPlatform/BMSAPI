package org.ibp.api.java.impl.middleware.sample;

import org.generationcp.middleware.domain.sample.SampleDTO;
import org.generationcp.middleware.domain.sample.SampleDetailsDTO;
import org.ibp.api.domain.sample.SampleObservationDto;
import org.ibp.api.domain.sample.SampleObservationMapper;
import org.modelmapper.ModelMapper;
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

	@Override
	public SampleObservationDto getSampleObservation(final String sampleId) {
		final ModelMapper mapper = SampleObservationMapper.getInstance();
		final SampleDetailsDTO sampleDetailsDTO = this.sampleService.getSampleObservation(sampleId);
		return mapper.map(sampleDetailsDTO, SampleObservationDto.class);

	}
}
