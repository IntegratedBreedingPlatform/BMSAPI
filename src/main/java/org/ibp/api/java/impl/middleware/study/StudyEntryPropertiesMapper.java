package org.ibp.api.java.impl.middleware.study;

import org.generationcp.middleware.domain.gms.SystemDefinedEntryType;
import org.generationcp.middleware.domain.oms.TermId;
import org.generationcp.middleware.pojos.Germplasm;
import org.generationcp.middleware.pojos.GermplasmListData;
import org.generationcp.middleware.service.api.study.StudyEntryPropertyData;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StudyEntryPropertiesMapper {

	private static final Integer DEFAULT_ENTRY_TYPE = SystemDefinedEntryType.TEST_ENTRY.getEntryTypeCategoricalId();

	public static Map<Integer, StudyEntryPropertyData> map(final Germplasm source, final List<Integer> germplasmDescriptorIds,
		final Integer entryTypeId, final String cross) {
		final Map<Integer, StudyEntryPropertyData> studyEntryProperties = new HashMap<>();
		for(final Integer variableId: germplasmDescriptorIds) {
			if(TermId.ENTRY_TYPE.getId() == variableId) {
				studyEntryProperties.put(TermId.ENTRY_TYPE.getId(),
					createStudyEntryPropertyValue(TermId.ENTRY_TYPE.getId(), null, entryTypeId));
			} else if(TermId.SEED_SOURCE.getId() == variableId) {
				//Add empty string for seedsource value from germplasm search
				studyEntryProperties.put(TermId.SEED_SOURCE.getId(),
					createStudyEntryPropertyValue(TermId.SEED_SOURCE.getId(), "", null));
			} else if(TermId.GROUPGID.getId() == variableId) {
				studyEntryProperties.put(TermId.GROUPGID.getId(),
					createStudyEntryPropertyValue(TermId.GROUPGID.getId(), String.valueOf(source.getMgid()), null));
			} else if(TermId.CROSS.getId() == variableId){
				studyEntryProperties.put(TermId.CROSS.getId(),
					createStudyEntryPropertyValue(TermId.CROSS.getId(), cross, null));
			} else if(TermId.GERMPLASM_SOURCE.getId() == variableId) {
				//Add empty string for seedsource value from germplasm search
				studyEntryProperties.put(TermId.GERMPLASM_SOURCE.getId(),
					createStudyEntryPropertyValue(TermId.GERMPLASM_SOURCE.getId(), "", null));
			}
		}

		return studyEntryProperties;
	}

	public static Map<Integer, StudyEntryPropertyData> map(final GermplasmListData source, final List<Integer> germplasmDescriptorIds) {
		final Map<Integer, StudyEntryPropertyData> studyEntryProperties = new HashMap<>();
		for(final Integer variableId: germplasmDescriptorIds) {
			if(TermId.ENTRY_TYPE.getId() == variableId) {
				studyEntryProperties.put(TermId.ENTRY_TYPE.getId(),
					createStudyEntryPropertyValue(TermId.ENTRY_TYPE.getId(), null, DEFAULT_ENTRY_TYPE));
			} else if(TermId.SEED_SOURCE.getId() == variableId) {
				studyEntryProperties.put(TermId.SEED_SOURCE.getId(),
					createStudyEntryPropertyValue(TermId.SEED_SOURCE.getId(), source.getSeedSource(), null));
			} else if(TermId.GROUPGID.getId() == variableId) {
				studyEntryProperties.put(TermId.GROUPGID.getId(),
						createStudyEntryPropertyValue(TermId.GROUPGID.getId(), String.valueOf(source.getGermplasm().getMgid()), null));
			} else if(TermId.CROSS.getId() == variableId){
				studyEntryProperties.put(TermId.CROSS.getId(),
						createStudyEntryPropertyValue(TermId.CROSS.getId(), String.valueOf(source.getGroupName()), null));
			} else if(TermId.GERMPLASM_SOURCE.getId() == variableId) {
				studyEntryProperties.put(TermId.GERMPLASM_SOURCE.getId(),
					createStudyEntryPropertyValue(TermId.GERMPLASM_SOURCE.getId(), source.getSeedSource(), null));
			}
		}

		return studyEntryProperties;
	}

	static StudyEntryPropertyData createStudyEntryPropertyValue(final Integer variableId, final String value, final Integer categoricalValueId) {
		return new StudyEntryPropertyData(null, variableId, value, categoricalValueId);
	}



}
