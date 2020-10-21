package org.ibp.api.java.impl.middleware.study;

import org.generationcp.middleware.domain.gms.SystemDefinedEntryType;
import org.generationcp.middleware.domain.oms.TermId;
import org.generationcp.middleware.pojos.GermplasmListData;
import org.generationcp.middleware.service.api.study.StudyEntryPropertyData;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StudyEntryPropertiesMapper {
	private static final String DEFAULT_ENTRY_TYPE = String.valueOf(SystemDefinedEntryType.TEST_ENTRY.getEntryTypeCategoricalId());

	public static Map<Integer, StudyEntryPropertyData> map(final GermplasmListData source, final List<Integer> germplasmDescriptorIds) {
		final Map<Integer, StudyEntryPropertyData> studyEntryProperties = new HashMap<>();
		for(final Integer variableId: germplasmDescriptorIds) {
			if(TermId.ENTRY_TYPE.getId() == variableId) {
				studyEntryProperties.put(TermId.ENTRY_TYPE.getId(),
					createStudyEntryPropertyValue(TermId.ENTRY_TYPE.getId(), DEFAULT_ENTRY_TYPE));
			} else if(TermId.SEED_SOURCE.getId() == variableId) {
				studyEntryProperties.put(TermId.SEED_SOURCE.getId(),
					createStudyEntryPropertyValue(TermId.SEED_SOURCE.getId(), source.getSeedSource()));
			} else if(TermId.GROUPGID.getId() == variableId) {
				studyEntryProperties.put(TermId.GROUPGID.getId(),
						createStudyEntryPropertyValue(TermId.GROUPGID.getId(), String.valueOf(source.getGermplasm().getMgid())));
			} else if(TermId.CROSS.getId() == variableId){
				studyEntryProperties.put(TermId.CROSS.getId(),
						createStudyEntryPropertyValue(TermId.CROSS.getId(), String.valueOf(source.getGroupName())));
			} else if(TermId.GERMPLASM_SOURCE.getId() == variableId) {
				studyEntryProperties.put(TermId.GERMPLASM_SOURCE.getId(),
					createStudyEntryPropertyValue(TermId.GERMPLASM_SOURCE.getId(), source.getSeedSource()));
			}
		}

		return studyEntryProperties;
	}

	static StudyEntryPropertyData createStudyEntryPropertyValue(final Integer variableId, final String value) {
		return new StudyEntryPropertyData(null, variableId, value);
	}



}
