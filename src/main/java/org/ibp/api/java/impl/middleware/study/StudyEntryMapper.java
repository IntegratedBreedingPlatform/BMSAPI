
package org.ibp.api.java.impl.middleware.study;

import org.generationcp.middleware.domain.gms.SystemDefinedEntryType;
import org.generationcp.middleware.domain.oms.TermId;
import org.generationcp.middleware.pojos.GermplasmListData;
import org.generationcp.middleware.service.api.study.StudyEntryDto;
import org.generationcp.middleware.service.api.study.StudyEntryPropertyData;
import org.generationcp.middleware.service.api.study.StudyGermplasmDto;
import org.ibp.api.domain.study.StudyGermplasm;
import org.ibp.api.mapper.ApiMapper;
import org.modelmapper.ModelMapper;
import org.modelmapper.PropertyMap;

import java.util.HashMap;
import java.util.Map;

public class StudyEntryMapper {

	private static final ModelMapper applicationWideModelMapper = ApiMapper.getInstance();
	private static final String DEFAULT_ENTRY_TYPE = String.valueOf(SystemDefinedEntryType.TEST_ENTRY.getEntryTypeCategoricalId());

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
		mapper.addMappings(new PropertyMap<GermplasmListData, StudyEntryDto>() {

			@Override
			protected void configure() {
				this.map().setEntryNumber(this.source.getEntryId());
				this.map().setEntryId(this.source.getEntryId());
				this.map().setGid(this.source.getGermplasmId());
				this.map().setDesignation(this.source.getDesignation());
				this.map().setEntryCode(this.source.getEntryCode());

				final Map<Integer, StudyEntryPropertyData> stringStudyEntryPropertyDataMap = new HashMap<>();
				stringStudyEntryPropertyDataMap
					.put(TermId.ENTRY_TYPE.getId(), new StudyEntryPropertyData(null, TermId.ENTRY_TYPE.getId(), DEFAULT_ENTRY_TYPE));
				stringStudyEntryPropertyDataMap
					.put(TermId.SEED_SOURCE.getId(), new StudyEntryPropertyData(null, TermId.SEED_SOURCE.getId(), this.source.getSeedSource()));
				stringStudyEntryPropertyDataMap
					.put(TermId.GROUPGID.getId(),
						new StudyEntryPropertyData(null, TermId.GROUPGID.getId(), String.valueOf(this.source.getGermplasm().getMgid())));

				this.map().setVariables(stringStudyEntryPropertyDataMap);
			}
		});
	}

}
