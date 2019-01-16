
package org.ibp.api.java.impl.middleware.study;

import org.generationcp.middleware.domain.fieldbook.FieldMapInfo;
import org.generationcp.middleware.domain.study.StudyTypeDto;
import org.generationcp.middleware.manager.api.StudyDataManager;
import org.generationcp.middleware.util.CrossExpansionProperties;
import org.ibp.api.domain.study.FieldMap;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

/**
 * The class <code>FieldMapServiceTest</code> contains tests for the class <code>{@link FieldMapService}</code>.
 *
 */
@RunWith(MockitoJUnitRunner.class)
public class ComplexFieldMapServiceTest {

	private Map<Integer, FieldMap> simpleFieldMap;

	@Mock
	private StudyDataManager studyDataManager;

	@Mock
	private CrossExpansionProperties crossExpansionProperties;

	@InjectMocks
	private FieldMapService fieldMapService;

	@Before
	public void setup() throws Exception {
		final List<FieldMapInfo> testFieldMapInfo = getComplexMiddlewareFieldMapInfoObjectForTest();
		when(studyDataManager.getStudyTypeByStudyId(123)).thenReturn(StudyTypeDto.getTrialDto());
		when(
				studyDataManager.getFieldMapInfoOfStudy(Matchers.<List<Integer>>any(),
						any(CrossExpansionProperties.class))).thenReturn(testFieldMapInfo);

		simpleFieldMap = fieldMapService.getFieldMap("123");
	}

	/**
	 * Just testing that there are not errors when we have a more complex fieldmap
	 *
	 */
	@Test
	public void testPlotValues() throws Exception {
		Assert.assertEquals("For the test data provided there should be one filed map", 3, simpleFieldMap.size());
	}

	private List<FieldMapInfo> getComplexMiddlewareFieldMapInfoObjectForTest() throws URISyntaxException {
		return FieldMapTestUtility.getFieldMapInfoFromSerializedFile("/testData/ComplexMiddlewareFieldMapInfoObjectForTest.ser");
	}
}
