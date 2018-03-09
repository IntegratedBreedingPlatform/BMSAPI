
package org.ibp.api.java.impl.middleware.study;

import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import org.generationcp.middleware.domain.fieldbook.FieldMapInfo;
import org.generationcp.middleware.domain.study.StudyTypeDto;
import org.generationcp.middleware.manager.api.StudyDataManager;
import org.generationcp.middleware.util.CrossExpansionProperties;
import org.ibp.api.domain.study.FieldMap;
import org.ibp.api.domain.study.FieldPlot;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.Mockito;

import java.net.URISyntaxException;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

/**
 * The class <code>FieldMapServiceTest</code> contains tests for the class <code>{@link FieldMapService}</code>.
 *
 */
public class FieldMapServiceTest {

	private Map<Integer, FieldMap> simpleFieldMap;

	@Before
	public void setup() throws Exception {
		final StudyDataManager studyDataManager = Mockito.mock(StudyDataManager.class);
		final List<FieldMapInfo> testFieldMapInfo = getSimpleMiddlewareFieldMapInfoObjectForTest();
		when(studyDataManager.getStudyType(123)).thenReturn(new StudyTypeDto("T"));
		when(
				studyDataManager.getFieldMapInfoOfStudy(Matchers.<List<Integer>>any(),
						any(CrossExpansionProperties.class))).thenReturn(testFieldMapInfo);
		final FieldMapService fieldMapService = new FieldMapService(studyDataManager, Mockito.mock(CrossExpansionProperties.class));
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
			public void checkValues(final int column, final int range) {
				Assert.assertEquals(String.format("Plot values for coordinate %d, %d must be the same", column, range),
						expectedPlotValues[column][range], resultingPlotsPlots[column][range].getPlotNumber());

			}
		});

		final Boolean[][] expectedDeletedValues = { {false, true, false}, {false, true, false}, {false, true, false}};
		checkplotValues(expectedDeletedValues, resultingPlotsPlots, new AssertFileMapValues() {

			@Override
			public void checkValues(final int column, final int range) {
				Assert.assertEquals(String.format("Deleted values for coordinate %d, %d must be the same", column, range),
						expectedDeletedValues[column][range], resultingPlotsPlots[column][range].isPlotDeleted());

			}
		});

		final Integer[][] expectedEntryNumber = { {6, null, 1}, {2, null, 4}, {3, null, 5}};
		checkplotValues(expectedEntryNumber, resultingPlotsPlots, new AssertFileMapValues() {

			@Override
			public void checkValues(final int column, final int range) {
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

		final ImmutableMap<Integer, List<Integer>> expectedRangeResults =
				new ImmutableMap.Builder<Integer, List<Integer>>().put(1, Lists.newArrayList(1, 2, 3)).put(2, Lists.newArrayList(0, 0, 0))
						.put(3, Lists.newArrayList(4, 5, 6)).build();

		checkplotValues(expectedRangeResults, actualRangeResults, new AssertFileMapValuesTwo() {

			@Override
			public void checkValues(final Integer expectedPlotValue, final FieldPlot actualFieldPlot) {
				Assert.assertEquals(expectedPlotValue,
						(Integer) (actualFieldPlot.getPlotNumber() == null ? 0 : actualFieldPlot.getPlotNumber()));

			}
		});

		final ImmutableMap<Integer, List<Integer>> expectedColumnResults =
				new ImmutableMap.Builder<Integer, List<Integer>>().put(1, Lists.newArrayList(1, 0, 4)).put(2, Lists.newArrayList(2, 0, 5))
						.put(3, Lists.newArrayList(3, 0, 6)).build();
		final Map<Integer, List<FieldPlot>> actualColumnResults = simpleFieldMap.get(600000078).getColumns();

		checkplotValues(expectedColumnResults, actualColumnResults, new AssertFileMapValuesTwo() {

			@Override
			public void checkValues(final Integer expectedPlotValue, final FieldPlot actualFieldPlot) {
				Assert.assertEquals(expectedPlotValue,
						(Integer) (actualFieldPlot.getPlotNumber() == null ? 0 : actualFieldPlot.getPlotNumber()));

			}
		});
	}

	private void checkplotValues(final ImmutableMap<Integer, List<Integer>> expectedResults, final Map<Integer, List<FieldPlot>> actualResults,
			final AssertFileMapValuesTwo assertFileMapValuesTwo) {

		final ImmutableCollection<Entry<Integer, List<Integer>>> expectedPlotValues = expectedResults.entrySet();

		for (final Entry<Integer, List<Integer>> expectedPlotValuesEntry : expectedPlotValues) {
			final Integer key = expectedPlotValuesEntry.getKey();
			final List<Integer> expectedValues = expectedPlotValuesEntry.getValue();
			final Iterator<Integer> expectedValueIterator = expectedValues.iterator();
			final List<FieldPlot> actualValues = actualResults.get(key);
			final Iterator<FieldPlot> actualValueIterator = actualValues.iterator();
			while (expectedValueIterator.hasNext()) {
				final Integer plotValues = (Integer) expectedValueIterator.next();
				final FieldPlot fieldPlot = (FieldPlot) actualValueIterator.next();
				assertFileMapValuesTwo.checkValues(plotValues, fieldPlot);
			}

		}

	}

	private int getNumberOfPlots(final Map<Integer, List<FieldPlot>> resultingPlotsInRange) {
		int counter = 0;
		final Collection<List<FieldPlot>> values = resultingPlotsInRange.values();
		for (final List<FieldPlot> list : values) {
			for (final FieldPlot fieldPlot : list) {
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

	private void checkplotValues(final Object[][] expectedPlotValues, final FieldPlot[][] resultingPlotsPlots, final AssertFileMapValues assertFileMapValues) {

		for (int i = 0; i < resultingPlotsPlots.length; i++) {
			for (int j = 0; j < resultingPlotsPlots[i].length; j++) {
				assertFileMapValues.checkValues(i, j);
			}
		}

	}

	private int getNumberOfPlots(final FieldPlot[][] fieldPlots) {

		int counter = 0;
		for (final FieldPlot[] fieldPlot : fieldPlots) {
			for (final FieldPlot plot : fieldPlot) {
				counter++;
			}
		}
		return counter;
	}

	private List<FieldMapInfo> getSimpleMiddlewareFieldMapInfoObjectForTest() throws URISyntaxException {
		return FieldMapTestUtility.getFieldMapInfoFromSerializedFile("/testData/SimpleMiddlewareFieldMapInfoObjectForTest.ser");
	}

}
