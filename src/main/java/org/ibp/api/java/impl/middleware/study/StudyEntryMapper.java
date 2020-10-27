
package org.ibp.api.java.impl.middleware.study;

import org.generationcp.middleware.domain.oms.TermId;
import org.generationcp.middleware.pojos.GermplasmListData;
import org.generationcp.middleware.service.api.study.StudyEntryDto;
import org.ibp.api.domain.study.StudyGermplasm;
import org.ibp.api.mapper.ApiMapper;
import org.modelmapper.Converter;
import org.modelmapper.ModelMapper;
import org.modelmapper.PropertyMap;
import org.modelmapper.spi.MappingContext;

public class StudyEntryMapper {

	private static final ModelMapper applicationWideModelMapper = ApiMapper.getInstance();

	/**
	 * We do not want public constructor of this class as all methods are static
	 */
	private StudyEntryMapper() {

	}

	/**
	 * Configuring the application wide {@link ModelMapper} with ontology related configuration.
	 */
	static {
		StudyEntryMapper.addStudyEntryDtoMapping(StudyEntryMapper.applicationWideModelMapper);
		StudyEntryMapper.addGermplasmListDataMapping(StudyEntryMapper.applicationWideModelMapper);
	}

	public static ModelMapper getInstance() {
		return StudyEntryMapper.applicationWideModelMapper;
	}

	private static void addStudyEntryDtoMapping(final ModelMapper mapper) {
		mapper.addMappings(new PropertyMap<StudyEntryDto, StudyGermplasm>() {

			@Override
			protected void configure() {
				this.map().setEntryNumber(this.source.getEntryNumber());
				this.using(new OptionalConverter(TermId.ENTRY_TYPE.getId())).map(this.source).setEntryType(null);

				this.map().getGermplasmListEntrySummary().setGid(this.source.getGid());
				this.using(new OptionalConverter(TermId.CROSS.getId())).map(this.source).getGermplasmListEntrySummary().setCross(null);
				this.map().getGermplasmListEntrySummary().setDesignation(this.source.getDesignation());
				this.map().getGermplasmListEntrySummary().setEntryCode(this.source.getEntryCode());
				this.using(new OptionalConverter(TermId.SEED_SOURCE.getId())).map(this.source).getGermplasmListEntrySummary().setSeedSource(null);
			}
		});
	}

	private static class OptionalConverter implements Converter<StudyEntryDto, String> {

		private Integer termId;

		public OptionalConverter(final Integer termId) {
			this.termId = termId;
		}

		@Override
		public String convert(MappingContext<StudyEntryDto, String> mappingContext) {
			return mappingContext.getSource().getStudyEntryPropertyValue(this.termId).orElse("");
		}
	}

	private static void addGermplasmListDataMapping(final ModelMapper mapper) {
		mapper.addMappings(new PropertyMap<GermplasmListData, StudyEntryDto>() {

			@Override
			protected void configure() {
				this.map().setEntryNumber(this.source.getEntryId());
				this.map().setEntryId(this.source.getEntryId());
				this.map().setGid(this.source.getGermplasmId());
				this.map().setDesignation(this.source.getDesignation());
				this.map().setEntryCode(this.source.getEntryCode());
			}
		});
	}
}
