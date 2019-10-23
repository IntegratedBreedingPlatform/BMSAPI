package org.ibp.api.java.impl.middleware.design.validator;

import org.generationcp.middleware.domain.etl.MeasurementVariable;
import org.generationcp.middleware.service.api.study.StudyGermplasmDto;
import org.ibp.api.java.design.type.ExperimentDesignTypeService;
import org.ibp.api.java.design.type.ExperimentDesignTypeServicePostProcessor;
import org.ibp.api.java.impl.middleware.design.type.ExperimentDesignTypeServiceFactory;
import org.ibp.api.rest.dataset.ObservationUnitRow;
import org.ibp.api.rest.design.ExperimentDesignInput;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.context.annotation.Bean;

import java.util.List;

public class ExperimentDesignTypeServicePostProcessorTest {

	@Mock
	private ExperimentDesignTypeServiceFactory serviceFactory;

	@InjectMocks
	private ExperimentDesignTypeServicePostProcessor processor;

	@Before
	public void init() {
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void testRegisterDesignTypeService() {
		final ExperimentDesignTypeService service = new ExperimentDesignTypeService() {

			@Override
			public List<ObservationUnitRow> generateDesign(final int studyId, final ExperimentDesignInput experimentDesignInput,
				final String programUUID,
				final List<StudyGermplasmDto> studyGermplasmDtoList) {
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
			public List<MeasurementVariable> getMeasurementVariables(final int studyId, final ExperimentDesignInput experimentDesignInput,
				final String programUUID) {
				return null;
			}
		};
		this.processor.postProcessAfterInitialization(service, "");
		Mockito.verify(this.serviceFactory).addService(service);
	}

}
