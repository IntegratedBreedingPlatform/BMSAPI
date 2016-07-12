
package org.ibp.api.java.impl.middleware.study;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;

import org.generationcp.middleware.domain.fieldbook.FieldMapInfo;
import org.generationcp.middleware.domain.oms.StudyType;
import org.generationcp.middleware.manager.api.StudyDataManager;
import org.generationcp.middleware.util.CrossExpansionProperties;
import org.ibp.api.domain.study.FieldMap;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.Mockito;

/**
 * The class <code>FieldMapServiceTest</code> contains tests for the class <code>{@link FieldMapService}</code>.
 *
 */
public class ComplexFieldMapServiceTest {

	private Map<Integer, FieldMap> simpleFieldMap;

	@Before
	public void setup() throws Exception {
		StudyDataManager studyDataManager = Mockito.mock(StudyDataManager.class);
		List<FieldMapInfo> testFieldMapInfo = getComplexMiddlewareFieldMapInfoObjectForTest();
		when(studyDataManager.getStudyType(123)).thenReturn(StudyType.T);
		when(
				studyDataManager.getFieldMapInfoOfStudy(Matchers.<List<Integer>>any(), any(StudyType.class),
						any(CrossExpansionProperties.class), Matchers.anyBoolean())).thenReturn(testFieldMapInfo);
		FieldMapService fieldMapService = new FieldMapService(studyDataManager, Mockito.mock(CrossExpansionProperties.class));
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
