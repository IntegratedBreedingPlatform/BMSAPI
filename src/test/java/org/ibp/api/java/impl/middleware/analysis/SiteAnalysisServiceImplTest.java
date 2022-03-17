package org.ibp.api.java.impl.middleware.analysis;

import com.google.common.collect.Sets;
import org.apache.commons.lang.math.RandomUtils;
import org.generationcp.middleware.domain.dms.DatasetDTO;
import org.generationcp.middleware.service.api.dataset.DatasetService;
import org.generationcp.middleware.service.impl.analysis.MeansImportRequest;
import org.ibp.api.java.impl.middleware.common.validator.MeansImportRequestValidator;
import org.ibp.api.java.impl.middleware.study.validator.StudyEntryValidator;
import org.ibp.api.java.impl.middleware.study.validator.StudyValidator;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.modelmapper.internal.util.Assert;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class SiteAnalysisServiceImplTest {

	@Mock
	private org.generationcp.middleware.service.api.analysis.SiteAnalysisService middlewareSiteAnalysisService;

	@Mock
	private DatasetService middlewareDatasetService;

	@Mock
	private StudyValidator studyValidator;

	@Mock
	private StudyEntryValidator studyEntryValidator;

	@Mock
	private MeansImportRequestValidator meansImportRequestValidator;

	@InjectMocks
	private SiteAnalysisServiceImpl siteAnalysisService;

	@Test
	public void testCreateMeansDataset() {
		final int studyId = RandomUtils.nextInt();

		when(this.middlewareDatasetService.getDataset(anyInt())).thenReturn(new DatasetDTO());

		final MeansImportRequest meansImportRequest = this.createMeansImportRequest();
		Assert.notNull(this.siteAnalysisService.createMeansDataset(studyId, meansImportRequest));

		verify(this.studyValidator).validate(studyId, true);
		verify(this.studyValidator).validateStudyHasNoMeansDataset(studyId);
		verify(this.meansImportRequestValidator).validateEnvironmentNumberIsNotEmpty(meansImportRequest);
		verify(this.meansImportRequestValidator).validateMeansDataIsNotEmpty(meansImportRequest);
		verify(this.meansImportRequestValidator).validateDataValuesIsNotEmpty(meansImportRequest);
		verify(this.meansImportRequestValidator).validateEntryNumberIsNotEmptyAndDistinctPerEnvironment(meansImportRequest);
		verify(this.studyValidator).validateStudyInstanceNumbers(studyId, Sets.newHashSet(1));
		verify(this.studyEntryValidator).validateStudyContainsEntryNumbers(studyId, Sets.newHashSet("1"));
		verify(this.meansImportRequestValidator).validateAnalysisVariableNames(meansImportRequest);
		verify(this.middlewareSiteAnalysisService).createMeansDataset(studyId, meansImportRequest);
	}

	private MeansImportRequest createMeansImportRequest() {
		final MeansImportRequest meansImportRequest = new MeansImportRequest();
		final List<MeansImportRequest.MeansData> meansDataList = new ArrayList<>();
		final MeansImportRequest.MeansData meansData = new MeansImportRequest.MeansData();
		meansData.setEntryNo(1);
		meansData.setEnvironmentNumber(1);
		meansData.setValues(new HashMap<>());
		meansDataList.add(meansData);
		meansImportRequest.setData(meansDataList);
		return meansImportRequest;
	}
}