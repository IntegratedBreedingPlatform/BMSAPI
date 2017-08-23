package org.ibp.api.rest.sample;

import org.generationcp.middleware.domain.sample.SampleDTO;

import java.util.List;

public interface SampleService {
	List<SampleDTO> getSamples(String plotId);
}
