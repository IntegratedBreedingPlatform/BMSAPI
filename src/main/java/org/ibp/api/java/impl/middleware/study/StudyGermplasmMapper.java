
package org.ibp.api.java.impl.middleware.study;

import org.generationcp.middleware.domain.gms.SystemDefinedEntryType;
import org.generationcp.middleware.pojos.GermplasmListData;
import org.generationcp.middleware.service.api.study.StudyGermplasmDto;
import org.ibp.api.domain.study.StudyGermplasm;
import org.ibp.api.mapper.ApiMapper;
import org.modelmapper.ModelMapper;
import org.modelmapper.PropertyMap;

public class StudyGermplasmMapper {

	private static final ModelMapper applicationWideModelMapper = ApiMapper.getInstance();
	private static final String DEFAULT_ENTRY_TYPE = String.valueOf(SystemDefinedEntryType.TEST_ENTRY.getEntryTypeCategoricalId());

	/**
	 * We do not want public constructor of this class as all methods are static
	 */
	private StudyGermplasmMapper() {

	}

	/**
	 * Configuring the application wide {@link ModelMapper} with ontology related configuration.
	 */
	static {
		StudyGermplasmMapper.addStudyGermplasmDtoMapping(StudyGermplasmMapper.applicationWideModelMapper);
		StudyGermplasmMapper.addGermplasmListDataMapping(StudyGermplasmMapper.applicationWideModelMapper);
	}

	public static ModelMapper getInstance() {
		return StudyGermplasmMapper.applicationWideModelMapper;
	}

	private static void addStudyGermplasmDtoMapping(final ModelMapper mapper) {
		mapper.addMappings(new PropertyMap<StudyGermplasmDto, StudyGermplasm>() {

			@Override
			protected void configure() {
				this.map().setEntryNumber(this.source.getEntryNumber());
				this.map().setEntryType(this.source.getEntryType());
				this.map().setPosition(this.source.getPosition());
				this.map().getGermplasmListEntrySummary().setGid(this.source.getGermplasmId());
				this.map().getGermplasmListEntrySummary().setCross(this.source.getCross());
				this.map().getGermplasmListEntrySummary().setDesignation(this.source.getDesignation());
				this.map().getGermplasmListEntrySummary().setEntryCode(this.source.getEntryCode());
				this.map().getGermplasmListEntrySummary().setSeedSource(this.source.getSeedSource());
			}
		});
	}

	private static void addGermplasmListDataMapping(final ModelMapper mapper) {
		mapper.addMappings(new PropertyMap<GermplasmListData, StudyGermplasmDto>() {

			@Override
			protected void configure() {
				this.map().setEntryNumber(this.source.getEntryId());
				this.map().setEntryType(DEFAULT_ENTRY_TYPE);
				this.map().setPosition(String.valueOf(this.source.getEntryId()));
				this.map().setGermplasmId(this.source.getGermplasmId());
				this.map().setCross(this.source.getGermplasm().getCrossName());
				this.map().setDesignation(this.source.getDesignation());
				this.map().setEntryCode(this.source.getEntryCode());
				this.map().setSeedSource(this.source.getSeedSource());
				this.map().setGroupId(this.source.getGermplasm().getMgid());
			}
		});
	}

}
