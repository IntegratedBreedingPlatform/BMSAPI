package org.ibp.api.java.impl.middleware.study;

import org.generationcp.middleware.api.study.AdvanceSampledPlantsRequest;
import org.generationcp.middleware.api.study.AdvanceStudyRequest;
import org.ibp.api.java.impl.middleware.study.validator.AdvanceValidator;
import org.ibp.api.java.study.AdvanceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;

@Service
@Transactional
public class AdvanceServiceImpl implements AdvanceService {

	@Resource
	private AdvanceValidator advanceValidator;

	@Autowired
	private org.generationcp.middleware.service.api.study.advance.AdvanceService advanceService;

	@Override
	public List<Integer> advanceStudy(final Integer studyId, final AdvanceStudyRequest request) {
		this.advanceValidator.validateAdvanceStudy(studyId, request);
		return this.advanceService.advanceStudy(studyId, request);
	}

	@Override
	public List<Integer> advanceSamples(final Integer studyId, final AdvanceSampledPlantsRequest request) {
		this.advanceValidator.validateAdvanceSamples(studyId, request);
		return this.advanceService.advanceSamples(studyId, request);
	}

}
