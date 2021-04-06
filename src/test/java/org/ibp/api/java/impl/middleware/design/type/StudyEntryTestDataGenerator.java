package org.ibp.api.java.impl.middleware.design.type;

import org.generationcp.middleware.domain.gms.SystemDefinedEntryType;
import org.generationcp.middleware.domain.oms.TermId;
import org.generationcp.middleware.service.api.study.StudyEntryDto;
import org.generationcp.middleware.service.api.study.StudyEntryPropertyData;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class StudyEntryTestDataGenerator {

	public static List<StudyEntryDto> createStudyEntryDtoList(final int numberOfTestEntries, final int numberOfCheckEntries) {
		final List<StudyEntryDto> studyEntryDtoList = new ArrayList<>();

		int entryNumber = 1;
		int germplasmId = 100;
		for (int i = 1; i <= numberOfCheckEntries; i++) {
			studyEntryDtoList.add(createStudyEntry(entryNumber, germplasmId, SystemDefinedEntryType.CHECK_ENTRY));
			entryNumber++;
			germplasmId++;
		}
		for (int i = 1; i <= numberOfTestEntries; i++) {
			studyEntryDtoList.add(createStudyEntry(entryNumber, germplasmId, SystemDefinedEntryType.TEST_ENTRY));
			entryNumber++;
			germplasmId++;
		}
		return studyEntryDtoList;
	}

	public static StudyEntryDto createStudyEntry(final int entryNumber, final int germplasmId,
													 final SystemDefinedEntryType systemDefinedEntryType) {
		final StudyEntryDto studyEntryDto = new StudyEntryDto();
		studyEntryDto.setEntryNumber(entryNumber);
		studyEntryDto.setEntryCode(String.valueOf(entryNumber));
		studyEntryDto.setDesignation("DESIG" + entryNumber);
		studyEntryDto.setGid(germplasmId);

		final Map<Integer, StudyEntryPropertyData> properties = new HashMap<>();
		properties.put(TermId.SEED_SOURCE.getId(), new StudyEntryPropertyData(null, TermId.SEED_SOURCE.getId(), "SOURCE" + entryNumber));
		properties.put(TermId.ENTRY_TYPE.getId(), new StudyEntryPropertyData(null, TermId.ENTRY_TYPE.getId(), String.valueOf(systemDefinedEntryType.getEntryTypeCategoricalId())));
		properties.put(TermId.GROUPGID.getId(), new StudyEntryPropertyData(null, TermId.GROUPGID.getId(), String.valueOf(0)));
		studyEntryDto.setProperties(properties);
		return studyEntryDto;
	}

	public static List<StudyEntryDto> createStudyEntryDtoList(final int numberOfTestEntries, final int numberOfCheckEntries, final int numberOfNonReplicatedEntries) {
		final List<StudyEntryDto> studyEntryDtoList = new ArrayList<>();

		int entryNumber = 1;
		int germplasmId = 100;
		for (int i = 1; i <= numberOfCheckEntries; i++) {
			studyEntryDtoList.add(createStudyEntry(entryNumber, germplasmId, SystemDefinedEntryType.CHECK_ENTRY));
			entryNumber++;
			germplasmId++;
		}
		for (int i = 1; i <= numberOfTestEntries; i++) {
			studyEntryDtoList.add(createStudyEntry(entryNumber, germplasmId, SystemDefinedEntryType.TEST_ENTRY));
			entryNumber++;
			germplasmId++;
		}

		for (int i = 1; i <= numberOfNonReplicatedEntries; i++) {
			studyEntryDtoList.add(createStudyEntry(entryNumber, germplasmId, SystemDefinedEntryType.NON_REPLICATED_ENTRY));
			entryNumber++;
			germplasmId++;
		}
		return studyEntryDtoList;
	}

}
