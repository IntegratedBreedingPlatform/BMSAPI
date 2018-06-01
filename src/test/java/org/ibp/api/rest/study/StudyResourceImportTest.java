
package org.ibp.api.rest.study;

import org.generationcp.middleware.manager.api.GermplasmListManager;
import org.generationcp.middleware.service.api.DataImportService;
import org.generationcp.middleware.service.api.FieldbookService;
import org.ibp.ApiUnitTestBase;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

public class StudyResourceImportTest extends ApiUnitTestBase {

	@Autowired
	private FieldbookService fieldbookService;

	@Autowired
	private GermplasmListManager germplasmListManager;

	@Autowired
	private DataImportService dataImportService;

	@Configuration
	public static class TestConfiguration {

		@Bean
		@Primary
		public FieldbookService fieldbookService() {
			return Mockito.mock(FieldbookService.class);
		}

		@Bean
		@Primary
		public GermplasmListManager germplasmListManager() {
			return Mockito.mock(GermplasmListManager.class);
		}

		@Bean
		@Primary
		public DataImportService dataImportService() {
			return Mockito.mock(DataImportService.class);
		}
	}
}
