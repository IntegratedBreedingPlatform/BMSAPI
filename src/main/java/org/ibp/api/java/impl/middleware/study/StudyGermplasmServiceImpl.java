package org.ibp.api.java.impl.middleware.study;

import org.generationcp.middleware.pojos.GermplasmList;
import org.generationcp.middleware.service.api.PedigreeService;
import org.generationcp.middleware.service.api.study.StudyEntryPropertyData;
import org.generationcp.middleware.service.api.study.StudyGermplasmDto;
import org.generationcp.middleware.util.CrossExpansionProperties;
import org.ibp.api.java.germplasm.GermplamListService;
import org.ibp.api.java.impl.middleware.common.validator.GermplasmListValidator;
import org.ibp.api.java.impl.middleware.ontology.validator.TermValidator;
import org.ibp.api.java.impl.middleware.study.validator.StudyGermplasmValidator;
import org.ibp.api.java.impl.middleware.study.validator.StudyValidator;
import org.ibp.api.java.study.StudyGermplasmService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

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

	@Autowired
	private GermplasmListValidator germplasmListValidator;

	@Autowired
	private GermplamListService germplasmListService;

	@Autowired
	private TermValidator termValidator;

	@Resource
	private org.generationcp.middleware.service.api.study.StudyGermplasmService middlewareStudyGermplasmService;

	@Override
	public StudyGermplasmDto replaceStudyEntry(final Integer studyId, final Integer entryId,
		final StudyGermplasmDto studyGermplasmDto) {
		final Integer gid = studyGermplasmDto.getGermplasmId();
		this.studyValidator.validate(studyId, true);
		this.studyGermplasmValidator.validate(studyId, entryId, gid);

		return this.middlewareStudyGermplasmService
			.replaceStudyGermplasm(studyId, entryId, gid, this.pedigreeService.getCrossExpansion(gid, this.crossExpansionProperties));
	}

	@Override
	public List<StudyGermplasmDto> createStudyEntries(final Integer studyId, final Integer germplasmListId) {
		final GermplasmList germplasmList = this.germplasmListService.getGermplasmList(germplasmListId);

		this.germplasmListValidator.validateGermplasmList(germplasmListId);
		this.studyGermplasmValidator.validateStudyAlreadyHasStudyEntries(studyId);
		this.studyValidator.validate(studyId, true);

		final ModelMapper mapper = StudyEntryMapper.getInstance();
		final List<StudyGermplasmDto> studyGermplasmList =
			germplasmList.getListData().stream().map(l -> mapper.map(l, StudyGermplasmDto.class)).collect(Collectors.toList());

		return this.middlewareStudyGermplasmService.saveStudyEntries(studyId, studyGermplasmList);
	}

	@Override
	public void deleteStudyEntries(final Integer studyId) {
		this.studyValidator.validate(studyId, true);
		this.middlewareStudyGermplasmService.deleteStudyEntries(studyId);
	}

	@Override
	public void updateStudyEntryProperty(final Integer studyId, final Integer entryId,
		final StudyEntryPropertyData studyEntryPropertyData) {
		this.studyValidator.validate(studyId, true);
		this.studyValidator.validateStudyContainsEntry(studyId, entryId);
		this.termValidator.validate(studyEntryPropertyData.getVariableId());
		this.studyGermplasmValidator.validateStudyEntryProperty(studyEntryPropertyData.getStudyEntryPropertyId());
		this.middlewareStudyGermplasmService.updateStudyEntryProperty(studyId, studyEntryPropertyData);
	}

}
