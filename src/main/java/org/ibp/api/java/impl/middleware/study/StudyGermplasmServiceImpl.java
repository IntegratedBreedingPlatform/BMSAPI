package org.ibp.api.java.impl.middleware.study;

import org.generationcp.middleware.service.api.PedigreeService;
import org.generationcp.middleware.service.api.study.StudyGermplasmDto;
import org.generationcp.middleware.util.CrossExpansionProperties;
import org.ibp.api.java.impl.middleware.study.validator.StudyGermplasmValidator;
import org.ibp.api.java.impl.middleware.study.validator.StudyValidator;
import org.ibp.api.java.study.StudyGermplasmService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

@Service
@Transactional
public class StudyGermplasmServiceImpl implements StudyGermplasmService {

    @Resource
    private StudyValidator studyValidator;

    @Autowired
    private PedigreeService pedigreeService;

    @Autowired
    private CrossExpansionProperties crossExpansionProperties;


    @Resource
    private StudyGermplasmValidator studyGermplasmValidator;

    @Resource
    private org.generationcp.middleware.service.api.study.StudyGermplasmService middlewareStudyGermplasmService;

    @Override
    public StudyGermplasmDto replaceStudyGermplasm(final Integer studyId, final Integer entryId, final Integer gid) {
        this.studyValidator.validate(studyId, true);
        this.studyGermplasmValidator.validate(studyId, entryId, gid);

        return this.middlewareStudyGermplasmService.replaceStudyGermplasm(studyId, entryId, gid, this.pedigreeService.getCrossExpansion(gid, this.crossExpansionProperties));
    }


}
