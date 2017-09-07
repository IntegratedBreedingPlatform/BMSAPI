package org.ibp.api.java.impl.middleware.sample;

import org.generationcp.middleware.domain.sample.SampleDTO;

import java.util.List;

public interface SampleService {
	List<SampleDTO> getSamples(final String plotId);
}
