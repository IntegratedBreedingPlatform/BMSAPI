
package org.ibp.api.java.impl.middleware.study;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

import java.net.URISyntaxException;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.generationcp.middleware.domain.fieldbook.FieldMapInfo;
import org.generationcp.middleware.domain.oms.StudyType;
import org.generationcp.middleware.manager.api.StudyDataManager;
import org.generationcp.middleware.util.CrossExpansionProperties;
import org.ibp.api.domain.study.FieldMap;
import org.ibp.api.domain.study.FieldPlot;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.Mockito;

import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;

/**
 * The class <code>FieldMapServiceTest</code> contains tests for the class <code>{@link FieldMapService}</code>.
 *
 */
public class FieldMapServiceTest {

	private Map<Integer, FieldMap> simpleFieldMap;

	@Before
	public void setup() throws Exception {
		StudyDataManager studyDataManager = Mockito.mock(StudyDataManager.class);
		List<FieldMapInfo> testFieldMapInfo = getSimpleMiddlewareFieldMapInfoObjectForTest();
		when(studyDataManager.getStudyType(123)).thenReturn(StudyType.T);
		when(
				studyDataManager.getFieldMapInfoOfStudy(Matchers.<List<Integer>>any(), any(StudyType.class),
						any(CrossExpansionProperties.class))).thenReturn(testFieldMapInfo);
		FieldMapService fieldMapService = new FieldMapService(studyDataManager);
		simpleFieldMap = fieldMapService.getFieldMap("123");
	}

	/**
	 * Test plot values
	 *
	 */
	@Test
	public void testPlotValues() throws Exception {

		Assert.assertEquals("For the test data provided there should be one filed map", 1, simpleFieldMap.size());
		final FieldPlot[][] resultingPlotsPlots = simpleFieldMap.get(600000078).getPlots();
		Assert.assertEquals("For the test data provided there should be 9 plots", 9, getNumberOfPlots(resultingPlotsPlots));

		final Integer[][] expectedPlotValues = { {1, null, 4}, {2, null, 5}, {3, null, 6}};
		checkplotValues(expectedPlotValues, resultingPlotsPlots, new AssertFileMapValues() {

			@Override
			public void checkValues(int column, int range) {
				Assert.assertEquals(String.format("Plot values for coordinate %d, %d must be the same", column, range),
						expectedPlotValues[column][range], resultingPlotsPlots[column][range].getPlotNumber());

			}
		});

		final Boolean[][] expectedDeletedValues = { {false, true, false}, {false, true, false}, {false, true, false}};
		checkplotValues(expectedDeletedValues, resultingPlotsPlots, new AssertFileMapValues() {

			@Override
			public void checkValues(int column, int range) {
				Assert.assertEquals(String.format("Deleted values for coordinate %d, %d must be the same", column, range),
						expectedDeletedValues[column][range], resultingPlotsPlots[column][range].isPlotDeleted());

			}
		});

		final Integer[][] expectedEntryNumber = { {6, null, 1}, {2, null, 4}, {3, null, 5}};
		checkplotValues(expectedEntryNumber, resultingPlotsPlots, new AssertFileMapValues() {

			@Override
			public void checkValues(int column, int range) {
				Assert.assertEquals(String.format("Entry number for coordinate %d, %d must be the same", column, range),
						expectedEntryNumber[column][range], resultingPlotsPlots[column][range].getEntryNumber());

			}
		});

	}

	/**
	 * Test Range and columns correctly created
	 *
	 */
	@Test
	public void testRangeValues() throws Exception {

		Assert.assertEquals("For the test data provided there should be one filed map", 1, simpleFieldMap.size());
		final Map<Integer, List<FieldPlot>> actualRangeResults = simpleFieldMap.get(600000078).getRange();

		Assert.assertEquals("For the test data provided there should be 9 plots", 9, getNumberOfPlots(actualRangeResults));

		ImmutableMap<Integer, List<Integer>> expectedRangeResults =
				new ImmutableMap.Builder<Integer, List<Integer>>().put(1, Lists.newArrayList(1, 2, 3)).put(2, Lists.newArrayList(0, 0, 0))
						.put(3, Lists.newArrayList(4, 5, 6)).build();

		checkplotValues(expectedRangeResults, actualRangeResults, new AssertFileMapValuesTwo() {

			@Override
			public void checkValues(Integer expectedPlotValue, FieldPlot actualFieldPlot) {
				Assert.assertEquals(expectedPlotValue,
						(Integer) (actualFieldPlot.getPlotNumber() == null ? 0 : actualFieldPlot.getPlotNumber()));

			}
		});

		ImmutableMap<Integer, List<Integer>> expectedColumnResults =
				new ImmutableMap.Builder<Integer, List<Integer>>().put(1, Lists.newArrayList(1, 0, 4)).put(2, Lists.newArrayList(2, 0, 5))
						.put(3, Lists.newArrayList(3, 0, 6)).build();
		final Map<Integer, List<FieldPlot>> actualColumnResults = simpleFieldMap.get(600000078).getColumns();

		checkplotValues(expectedColumnResults, actualColumnResults, new AssertFileMapValuesTwo() {

			@Override
			public void checkValues(Integer expectedPlotValue, FieldPlot actualFieldPlot) {
				Assert.assertEquals(expectedPlotValue,
						(Integer) (actualFieldPlot.getPlotNumber() == null ? 0 : actualFieldPlot.getPlotNumber()));

			}
		});
	}

	private void checkplotValues(ImmutableMap<Integer, List<Integer>> expectedResults, Map<Integer, List<FieldPlot>> actualResults,
			AssertFileMapValuesTwo assertFileMapValuesTwo) {

		final ImmutableCollection<Entry<Integer, List<Integer>>> expectedPlotValues = expectedResults.entrySet();

		for (final Entry<Integer, List<Integer>> expectedPlotValuesEntry : expectedPlotValues) {
			Integer key = expectedPlotValuesEntry.getKey();
			List<Integer> expectedValues = expectedPlotValuesEntry.getValue();
			Iterator<Integer> expectedValueIterator = expectedValues.iterator();
			List<FieldPlot> actualValues = actualResults.get(key);
			Iterator<FieldPlot> actualValueIterator = actualValues.iterator();
			while (expectedValueIterator.hasNext()) {
				Integer plotValues = (Integer) expectedValueIterator.next();
				FieldPlot fieldPlot = (FieldPlot) actualValueIterator.next();
				assertFileMapValuesTwo.checkValues(plotValues, fieldPlot);
			}

		}

	}

	private int getNumberOfPlots(Map<Integer, List<FieldPlot>> resultingPlotsInRange) {
		int counter = 0;
		Collection<List<FieldPlot>> values = resultingPlotsInRange.values();
		for (List<FieldPlot> list : values) {
			for (FieldPlot fieldPlot : list) {
				counter++;
			}
		}
		return counter;
	}

	private interface AssertFileMapValues {

		void checkValues(int column, int range);

	}

	private interface AssertFileMapValuesTwo {

		void checkValues(Integer expectedPlotValue, FieldPlot actualFieldPlot);
	}

	private void checkplotValues(Object[][] expectedPlotValues, FieldPlot[][] resultingPlotsPlots, AssertFileMapValues assertFileMapValues) {

		for (int i = 0; i < resultingPlotsPlots.length; i++) {
			for (int j = 0; j < resultingPlotsPlots[i].length; j++) {
				assertFileMapValues.checkValues(i, j);
			}
		}

	}

	private int getNumberOfPlots(final FieldPlot[][] fieldPlots) {

		int counter = 0;
		for (FieldPlot[] fieldPlot : fieldPlots) {
			for (FieldPlot plot : fieldPlot) {
				counter++;
			}
		}
		return counter;
	}

	private List<FieldMapInfo> getSimpleMiddlewareFieldMapInfoObjectForTest() throws URISyntaxException {
		return FieldMapTestUtility.getFieldMapInfoFromSerializedFile("/testData/SimpleMiddlewareFieldMapInfoObjectForTest.ser");
	}

}
