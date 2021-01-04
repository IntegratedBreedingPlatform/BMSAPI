package org.ibp.api.java.impl.middleware.design.validator;

import org.generationcp.middleware.domain.etl.MeasurementVariable;
import org.generationcp.middleware.service.api.study.StudyEntryDto;
import org.ibp.api.java.design.type.ExperimentalDesignTypeService;
import org.ibp.api.java.design.type.ExperimentalDesignTypeServicePostProcessor;
import org.ibp.api.java.impl.middleware.design.type.ExperimentalDesignTypeServiceFactory;
import org.ibp.api.rest.dataset.ObservationUnitRow;
import org.ibp.api.rest.design.ExperimentalDesignInput;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.util.List;

public class ExperimentalDesignTypeServicePostProcessorTest {

	@Mock
	private ExperimentalDesignTypeServiceFactory serviceFactory;

	@InjectMocks
	private ExperimentalDesignTypeServicePostProcessor processor;

	@Before
	public void init() {
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void testRegisterDesignTypeService() {
		final ExperimentalDesignTypeService service = new ExperimentalDesignTypeService() {

			@Override
			public List<ObservationUnitRow> generateDesign(final int studyId, final ExperimentalDesignInput experimentDesignInput,
				final String programUUID,
				final List<StudyEntryDto> studyEntryDtoList) {
				return null;
			}

			@Override
			public Boolean requiresLicenseCheck() {
				return null;
			}

			@Override
			public Integer getDesignTypeId() {
				return null;
			}

			@Override
			public List<MeasurementVariable> getMeasurementVariables(final int studyId, final ExperimentalDesignInput experimentDesignInput,
				final String programUUID) {
				return null;
			}
		};
		this.processor.postProcessAfterInitialization(service, "");
		Mockito.verify(this.serviceFactory).addService(service);
	}

}
