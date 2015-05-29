package org.ibp.api.java.impl.middleware.study;

import org.generationcp.middleware.service.api.study.StudyGermplasmDto;
import org.ibp.api.domain.study.StudyGermplasm;
import org.junit.Test;
import org.modelmapper.ModelMapper;

import static org.junit.Assert.assertEquals;

public class StudyMapperTest {
	
	/**
	 * Making sure that the mapper can handle nulls and the deep mapping happes if necessary.
	 */
	@Test
	public void testForNullPointers() {
		final ModelMapper instance = StudyMapper.getInstance();
		final StudyGermplasmDto studyGermplasmDto = new StudyGermplasmDto();
		studyGermplasmDto.setCross("Cross");
		studyGermplasmDto.setEntryNo("1");
		final StudyGermplasm mappedStudyGermplasm = instance.map(studyGermplasmDto, StudyGermplasm.class);
		assertEquals("The cross deep mapping has failed",studyGermplasmDto.getCross(), mappedStudyGermplasm.getGermplasmListEntrySummary().getCross());
		assertEquals("The entry no mapping has failed.",studyGermplasmDto.getEntryNo(), mappedStudyGermplasm.getEntryNo());

	}
}
