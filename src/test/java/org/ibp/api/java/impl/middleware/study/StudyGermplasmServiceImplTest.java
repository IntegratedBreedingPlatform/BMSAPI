package org.ibp.api.java.impl.middleware.study;

import org.apache.commons.lang3.RandomStringUtils;
import org.generationcp.middleware.service.api.PedigreeService;
import org.generationcp.middleware.util.CrossExpansionProperties;
import org.ibp.api.java.impl.middleware.study.validator.StudyGermplasmValidator;
import org.ibp.api.java.impl.middleware.study.validator.StudyValidator;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.util.Random;

public class StudyGermplasmServiceImplTest {

    @Mock
    private StudyValidator studyValidator;

    @Mock
    private PedigreeService pedigreeService;

    @Mock
    private CrossExpansionProperties crossExpansionProperties;

    @Mock
    private StudyGermplasmValidator studyGermplasmValidator;

    @Mock
    private org.generationcp.middleware.service.api.study.StudyGermplasmService middlewareStudyGermplasmService;

    @InjectMocks
    private final StudyGermplasmServiceImpl studyGermplasmService = new StudyGermplasmServiceImpl();

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testReplaceStudyGermplasm() {
        final Random random = new Random();
        final Integer studyId = random.nextInt();
        final Integer entryId = random.nextInt();
        final Integer newGid = random.nextInt();
        final String crossExpansion = RandomStringUtils.randomAlphabetic(20);
        Mockito.doReturn(crossExpansion).when(this.pedigreeService).getCrossExpansion(newGid, this.crossExpansionProperties);
        this.studyGermplasmService.replaceStudyGermplasm(studyId, entryId, newGid);
        Mockito.verify(this.studyValidator).validate(studyId, true);
        Mockito.verify(this.studyGermplasmValidator).validate(studyId, entryId, newGid);
        Mockito.verify(this.middlewareStudyGermplasmService).replaceStudyGermplasm(studyId, entryId, newGid, crossExpansion);
    }


}
