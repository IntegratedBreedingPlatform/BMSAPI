package org.ibp.api.java.impl.middleware.study.validator;

import org.generationcp.middleware.domain.study.StudyEntrySearchDto;
import org.generationcp.middleware.service.api.study.StudyEntryDto;
import org.generationcp.middleware.service.api.study.germplasm.source.GermplasmStudySourceDto;
import org.generationcp.middleware.service.api.study.germplasm.source.GermplasmStudySourceSearchRequest;
import org.generationcp.middleware.service.api.study.germplasm.source.GermplasmStudySourceService;
import org.ibp.api.exception.ApiRequestValidationException;
import org.ibp.api.java.study.StudyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.BindingResult;
import org.springframework.validation.MapBindingResult;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;

@Component
public class GermplasmStudySourceValidator {

	@Autowired
	private StudyService studyService;

	@Autowired
	private GermplasmStudySourceService germplasmStudySourceService;

	private BindingResult errors;

	public void validateDifferentGermplasmStudySource(final int studyId, final int entryId) {
		this.errors = new MapBindingResult(new HashMap<String, String>(), Integer.class.getName());
		final StudyEntrySearchDto studyEntrySearchDto = new StudyEntrySearchDto();
		studyEntrySearchDto.setEntryIds(Collections.singletonList(entryId));
		final List<StudyEntryDto> studyEntryDtoList = studyService.getStudyEntries(studyId, studyEntrySearchDto, null);
		final Integer gid = studyEntryDtoList.get(0).getGid();
		final GermplasmStudySourceSearchRequest germplasmStudySourceSearchRequest = new GermplasmStudySourceSearchRequest();
		final GermplasmStudySourceSearchRequest.Filter filter = new GermplasmStudySourceSearchRequest.Filter();
		filter.setGidList(Collections.singletonList(gid));
		germplasmStudySourceSearchRequest.setStudyId(studyId);
		germplasmStudySourceSearchRequest.setFilter(filter);
		final List<GermplasmStudySourceDto> germplasmStudySourceDtos =
			germplasmStudySourceService.getGermplasmStudySources(germplasmStudySourceSearchRequest);
		if (!germplasmStudySourceDtos.isEmpty()) {
			errors.reject("study.entry.replace.invalid.gid", new Object[] {String.valueOf(gid)}, "");
			throw new ApiRequestValidationException(this.errors.getAllErrors());
		}

	}

}
