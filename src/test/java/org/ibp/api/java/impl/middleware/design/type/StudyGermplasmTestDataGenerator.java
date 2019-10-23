package org.ibp.api.java.impl.middleware.design.type;

import org.generationcp.middleware.domain.gms.SystemDefinedEntryType;
import org.generationcp.middleware.service.api.study.StudyGermplasmDto;

import java.util.ArrayList;
import java.util.List;

public abstract class StudyGermplasmTestDataGenerator {

	public static List<StudyGermplasmDto> createStudyGermplasmDtoList(final int numberOfTestEntries, final int numberOfCheckEntries) {
		final List<StudyGermplasmDto> studyGermplasmDtoList = new ArrayList<>();

		int entryNumber = 1;
		int germplasmId = 100;
		for (int i = 1; i <= numberOfCheckEntries; i++) {
			studyGermplasmDtoList.add(crreateStudyGermplasmDto(entryNumber, germplasmId, SystemDefinedEntryType.CHECK_ENTRY));
			entryNumber++;
			germplasmId++;
		}
		for (int i = 1; i <= numberOfTestEntries; i++) {
			studyGermplasmDtoList.add(crreateStudyGermplasmDto(entryNumber, germplasmId, SystemDefinedEntryType.TEST_ENTRY));
			entryNumber++;
			germplasmId++;
		}
		return studyGermplasmDtoList;
	}

	public static StudyGermplasmDto crreateStudyGermplasmDto(final int entryNumber, final int germplasmId,
		final SystemDefinedEntryType systemDefinedEntryType) {
		final StudyGermplasmDto studyGermplasmDto = new StudyGermplasmDto();
		studyGermplasmDto.setEntryNumber(entryNumber);
		studyGermplasmDto.setEntryCode(String.valueOf(entryNumber));
		studyGermplasmDto.setDesignation("DESIG" + entryNumber);
		studyGermplasmDto.setSeedSource("SOURCE" + entryNumber);
		studyGermplasmDto.setGermplasmId(germplasmId);
		studyGermplasmDto.setCheckType(systemDefinedEntryType.getEntryTypeCategoricalId());
		studyGermplasmDto.setCross("");
		studyGermplasmDto.setGroupId(0);
		return studyGermplasmDto;
	}

}
