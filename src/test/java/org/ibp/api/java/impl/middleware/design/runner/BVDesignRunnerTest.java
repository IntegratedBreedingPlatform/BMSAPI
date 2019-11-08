package org.ibp.api.java.impl.middleware.design.runner;

import org.apache.commons.lang3.RandomStringUtils;
import org.generationcp.commons.constant.AppConstants;
import org.generationcp.middleware.domain.dms.ExperimentDesignType;
import org.ibp.api.domain.design.BVDesignOutput;
import org.ibp.api.domain.design.ListItem;
import org.ibp.api.domain.design.MainDesign;
import org.ibp.api.java.impl.middleware.design.breedingview.BreedingViewDesignParameter;
import org.ibp.api.java.impl.middleware.design.breedingview.BreedingViewVariableParameter;
import org.ibp.api.java.impl.middleware.design.generator.ExperimentalDesignGeneratorTestDataUtil;
import org.ibp.api.java.impl.middleware.design.generator.PRepDesignGenerator;
import org.ibp.api.java.impl.middleware.design.generator.RandomizeCompleteBlockDesignGenerator;
import org.ibp.api.java.impl.middleware.design.generator.ResolvableIncompleteBlockDesignGenerator;
import org.ibp.api.java.impl.middleware.design.generator.ResolvableRowColumnDesignGenerator;
import org.ibp.api.rest.design.ExperimentalDesignInput;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class BVDesignRunnerTest {

	private static final String BV_DESIGN_EXECUTABLE_PATH = "bvDesignExecutablePath";
	private static final String ENTRY_NO = RandomStringUtils.randomAlphabetic(10);
	private static final String PLOT_NO = RandomStringUtils.randomAlphabetic(10);
	private static final String REP_NO = RandomStringUtils.randomAlphabetic(10);
	private static final String BLOCK_NO = RandomStringUtils.randomAlphabetic(10);
	private static final String ROW = RandomStringUtils.randomAlphabetic(10);
	private static final String COL = RandomStringUtils.randomAlphabetic(10);

	@Mock
	private BVDesignRunner.BVDesignOutputReader outputReader;

	@Mock
	private BVDesignRunner.BVDesignProcessRunner processRunner;

	@Mock
	private BVDesignRunner.BVDesignXmlInputWriter inputWriter;

	private BVDesignRunner bvDesignRunner;

	@Before
	public void init() {

		this.bvDesignRunner = new BVDesignRunner();
		this.bvDesignRunner.setOutputReader(this.outputReader);
		this.bvDesignRunner.setProcessRunner(this.processRunner);
		this.bvDesignRunner.setInputWriter(this.inputWriter);
		this.bvDesignRunner.setBvDesignPath(BV_DESIGN_EXECUTABLE_PATH);
	}

	@Test
	public void testRunBVDesignSuccess() throws IOException {

		final String xmlInputFilePath = "xmlInputFilePath";
		final Integer successfulReturnCode = 0;

		final MainDesign mainDesign = this.createRandomizedCompleteBlockDesign();

		when(this.inputWriter.write(anyString())).thenReturn(xmlInputFilePath);
		when(this.processRunner.run(BV_DESIGN_EXECUTABLE_PATH, "-i" + xmlInputFilePath)).thenReturn(successfulReturnCode);
		when(this.outputReader.read(anyString())).thenReturn(new ArrayList<>());

		final BVDesignOutput bvDesignOutput = this.bvDesignRunner.runBVDesign(mainDesign);

		verify(this.processRunner).run(BV_DESIGN_EXECUTABLE_PATH, "-i" + xmlInputFilePath);
		verify(this.outputReader).read(mainDesign.getDesign().getParameterValue(BreedingViewDesignParameter.OUTPUTFILE.getParameterName()));
		assertTrue(bvDesignOutput.isSuccess());

	}

	@Test
	public void testRunBVDesignFail() throws IOException {

		final String xmlInputFilePath = "xmlInputFilePath";
		final Integer failureReturnCode = -1;

		final MainDesign mainDesign = this.createRandomizedCompleteBlockDesign();

		when(this.inputWriter.write(anyString())).thenReturn(xmlInputFilePath);
		when(this.processRunner.run(BV_DESIGN_EXECUTABLE_PATH, "-i" + xmlInputFilePath)).thenReturn(failureReturnCode);

		final BVDesignOutput bvDesignOutput = this.bvDesignRunner.runBVDesign(mainDesign);

		verify(this.processRunner).run(BV_DESIGN_EXECUTABLE_PATH, "-i" + xmlInputFilePath);
		verify(this.outputReader, never()).read(mainDesign.getDesign().getParameterValue(BreedingViewDesignParameter.OUTPUTFILE.getParameterName()));
		assertFalse(bvDesignOutput.isSuccess());

	}

	@Test
	public void testGetXMLStringForRandomizedCompleteBlockDesign() {

		final MainDesign mainDesign = this.createRandomizedCompleteBlockDesign();

		final String expectedString =
			"<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?><Templates><Template name=\"RandomizedBlock\">"
				+ "<Parameter name=\"" + BreedingViewDesignParameter.SEED.getParameterName() + "\" value=\":seedValue\"/>" + "<Parameter name=\""
				+ BreedingViewDesignParameter.NBLOCKS.getParameterName() + "\" value=\"6\"/>" + "<Parameter name=\""
				+BreedingViewVariableParameter.BLOCK.getParameterName() + "\" value=\"" + REP_NO + "\"/>" + "<Parameter name=\""
				+BreedingViewVariableParameter.PLOT.getParameterName() + "\" value=\"" + PLOT_NO + "\"/>" + "<Parameter name=\""
				+ BreedingViewDesignParameter.INITIAL_PLOT_NUMBER.getParameterName() + "\" value=\"301\"/>" + "<Parameter name=\""
				+ BreedingViewDesignParameter.INITIAL_TREATMENT_NUMBER.getParameterName() + "\">"
				+ "<ListItem value=\"1\"/><ListItem value=\"1\"/></Parameter>" + "<Parameter name=\""
				+ BreedingViewDesignParameter.TREATMENTFACTORS.getParameterName()
				+ "\"><ListItem value=\"ENTRY_NO\"/><ListItem value=\"FERTILIZER\"/></Parameter>"
				+ "<Parameter name=\"levels\"><ListItem value=\"24\"/><ListItem value=\"3\"/></Parameter>" + "<Parameter name=\""
				+ BreedingViewDesignParameter.TIMELIMIT.getParameterName() + "\" value=\"" + AppConstants.EXP_DESIGN_TIME_LIMIT.getString()
				+ "\"/>" + "<Parameter name=\"" + BreedingViewDesignParameter.OUTPUTFILE.getParameterName()
				+ "\" value=\":outputFile\"/></Template></Templates>";

		final BVDesignRunner runner = new BVDesignRunner();
		final String xmlString = runner.getXMLStringForDesign(mainDesign);

		this.assertXMLStringEqualsExpected(mainDesign, expectedString, xmlString);
	}

	@Test
	public void testGetXMLStringForResolvableIncompleteBlockDesign() {
		final ExperimentalDesignInput experimentalDesignInput = new ExperimentalDesignInput();
		experimentalDesignInput.setBlockSize(6);
		experimentalDesignInput.setReplicationsCount(2);
		experimentalDesignInput.setStartingPlotNo(301);
		experimentalDesignInput.setNblatin(0);
		experimentalDesignInput.setUseLatenized(false);

		final MainDesign mainDesign = new ResolvableIncompleteBlockDesignGenerator()
			.generate(experimentalDesignInput,
				ExperimentalDesignGeneratorTestDataUtil.getRIBDVariablesMap(BLOCK_NO, PLOT_NO, ENTRY_NO, REP_NO), 24, null, null);

		final String expectedString =
			"<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?><Templates><Template name=\"ResolvableIncompleteBlock\">"
				+ "<Parameter name=\"" + BreedingViewDesignParameter.SEED.getParameterName() + "\" value=\":seedValue\"/><Parameter name=\""
				+ BreedingViewDesignParameter.BLOCKSIZE.getParameterName() + "\" value=\"6\"/>" + "<Parameter name=\""
				+ BreedingViewDesignParameter.NTREATMENTS.getParameterName() + "\" value=\"24\"/><Parameter name=\""
				+ BreedingViewDesignParameter.NREPLICATES.getParameterName() + "\" value=\"2\"/>" + "<Parameter name=\""
				+ BreedingViewVariableParameter.ENTRY.getParameterName() + "\" value=\"" + ENTRY_NO + "\"/><Parameter name=\""
				+ BreedingViewDesignParameter.INITIAL_TREATMENT_NUMBER.getParameterName() + "\" value=\"1\"/><Parameter name=\""
				+BreedingViewVariableParameter.REP.getParameterName() + "\" value=\"" + REP_NO + "\"/>" + "<Parameter name=\""
				+BreedingViewVariableParameter.BLOCK.getParameterName() + "\" value=\"" + BLOCK_NO + "\"/><Parameter name=\""
				+BreedingViewVariableParameter.PLOT.getParameterName() + "\" value=\"" + PLOT_NO + "\"/>" + "<Parameter name=\""
				+ BreedingViewDesignParameter.INITIAL_PLOT_NUMBER.getParameterName() + "\" value=\"301\"/>" + "<Parameter name=\""
				+ BreedingViewDesignParameter.NBLATIN.getParameterName() + "\" value=\"0\"/><Parameter name=\""
				+ BreedingViewDesignParameter.TIMELIMIT.getParameterName() + "\" value=\"" + AppConstants.EXP_DESIGN_TIME_LIMIT.getString()
				+ "\"/>" + "<Parameter name=\"" + BreedingViewDesignParameter.OUTPUTFILE.getParameterName()
				+ "\" value=\":outputFile\"/></Template></Templates>";

		final BVDesignRunner runner = new BVDesignRunner();
		final String xmlString = runner.getXMLStringForDesign(mainDesign);

		this.assertXMLStringEqualsExpected(mainDesign, expectedString, xmlString);
	}

	@Test
	public void testGetXMLStringForResolvableRowColExpDesign() {
		final ExperimentalDesignInput experimentalDesignInput = new ExperimentalDesignInput();
		experimentalDesignInput.setReplicationsCount(2);
		experimentalDesignInput.setRowsPerReplications(5);
		experimentalDesignInput.setColsPerReplications(10);
		experimentalDesignInput.setStartingPlotNo(301);
		experimentalDesignInput.setNrlatin(0);
		experimentalDesignInput.setNclatin(0);
		experimentalDesignInput.setUseLatenized(false);


		final MainDesign mainDesign = new ResolvableRowColumnDesignGenerator()
			.generate(experimentalDesignInput, ExperimentalDesignGeneratorTestDataUtil.getRowColVariablesMap(ROW, COL, PLOT_NO, ENTRY_NO, REP_NO), 50, null, null);

		final String expectedString =
			"<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?><Templates><Template name=\"ResolvableRowColumn\">"
				+ "<Parameter name=\"" + BreedingViewDesignParameter.SEED.getParameterName() + "\" value=\":seedValue\"/><Parameter name=\""
				+ BreedingViewDesignParameter.NTREATMENTS.getParameterName() + "\" value=\"50\"/>" + "<Parameter name=\""
				+ BreedingViewDesignParameter.NREPLICATES.getParameterName() + "\" value=\"2\"/><Parameter name=\""
				+ BreedingViewDesignParameter.NROWS.getParameterName() + "\" value=\"5\"/>" + "<Parameter name=\""
				+ BreedingViewDesignParameter.NCOLUMNS.getParameterName() + "\" value=\"10\"/><Parameter name=\""
				+ BreedingViewVariableParameter.ENTRY.getParameterName() + "\" value=\"" + ENTRY_NO + "\"/>" + "<Parameter name=\""
				+ BreedingViewDesignParameter.INITIAL_TREATMENT_NUMBER.getParameterName() + "\" value=\"1\"/>" + "<Parameter name=\""
				+BreedingViewVariableParameter.REP.getParameterName() + "\" value=\"" + REP_NO + "\"/><Parameter name=\""
				+BreedingViewVariableParameter.ROW.getParameterName() + "\" value=\"" + ROW + "\"/>" + "<Parameter name=\""
				+BreedingViewVariableParameter.COLUMN.getParameterName() + "\" value=\"" + COL + "\"/><Parameter name=\""
				+BreedingViewVariableParameter.PLOT.getParameterName() + "\" value=\"" + PLOT_NO + "\"/>" + "<Parameter name=\""
				+ BreedingViewDesignParameter.INITIAL_PLOT_NUMBER.getParameterName() + "\" value=\"301\"/>" + "<Parameter name=\""
				+ BreedingViewDesignParameter.NRLATIN.getParameterName() + "\" value=\"0\"/><Parameter name=\""
				+ BreedingViewDesignParameter.NCLATIN.getParameterName() + "\" value=\"0\"/>" + "<Parameter name=\""
				+ BreedingViewDesignParameter.TIMELIMIT.getParameterName() + "\" value=\"" + AppConstants.EXP_DESIGN_TIME_LIMIT.getString()
				+ "\"/>" + "<Parameter name=\"" + BreedingViewDesignParameter.OUTPUTFILE.getParameterName()
				+ "\" value=\":outputFile\"/></Template></Templates>";

		final BVDesignRunner runner = new BVDesignRunner();
		final String xmlString = runner.getXMLStringForDesign(mainDesign);

		this.assertXMLStringEqualsExpected(mainDesign, expectedString, xmlString);
	}

	@Test
	public void testGetXMLStringForResolvableIncompleteBlockDesignWithEntryNumber() {
		final ExperimentalDesignInput experimentalDesignInput = new ExperimentalDesignInput();
		experimentalDesignInput.setBlockSize(6);
		experimentalDesignInput.setReplicationsCount(2);
		experimentalDesignInput.setStartingPlotNo(301);
		experimentalDesignInput.setNblatin(0);
		experimentalDesignInput.setUseLatenized(false);

		final MainDesign mainDesign = new ResolvableIncompleteBlockDesignGenerator()
			.generate(experimentalDesignInput, ExperimentalDesignGeneratorTestDataUtil.getRIBDVariablesMap(BLOCK_NO, PLOT_NO, ENTRY_NO, REP_NO), 24, null, null);

		final String expectedString =
			"<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?><Templates><Template name=\"ResolvableIncompleteBlock\">"
				+ "<Parameter name=\"" + BreedingViewDesignParameter.SEED.getParameterName() + "\" value=\":seedValue\"/>" + "<Parameter name=\""
				+ BreedingViewDesignParameter.BLOCKSIZE.getParameterName() + "\" value=\"6\"/>" + "<Parameter name=\""
				+ BreedingViewDesignParameter.NTREATMENTS.getParameterName() + "\" value=\"24\"/>" + "<Parameter name=\""
				+ BreedingViewDesignParameter.NREPLICATES.getParameterName() + "\" value=\"2\"/>" + "<Parameter name=\""
				+ BreedingViewVariableParameter.ENTRY.getParameterName() + "\" value=\"" + ENTRY_NO + "\"/>" + "<Parameter name=\""
				+ BreedingViewDesignParameter.INITIAL_TREATMENT_NUMBER.getParameterName() + "\" value=\"1\"/>" + "<Parameter name=\""
				+BreedingViewVariableParameter.REP.getParameterName() + "\" value=\"" + REP_NO + "\"/>" + "<Parameter name=\""
				+BreedingViewVariableParameter.BLOCK.getParameterName() + "\" value=\"" + BLOCK_NO + "\"/>" + "<Parameter name=\""
				+BreedingViewVariableParameter.PLOT.getParameterName() + "\" value=\"" + PLOT_NO + "\"/>" + "<Parameter name=\""
				+ BreedingViewDesignParameter.INITIAL_PLOT_NUMBER.getParameterName() + "\" value=\"301\"/>" + "<Parameter name=\""
				+ BreedingViewDesignParameter.NBLATIN.getParameterName() + "\" value=\"0\"/>" + "<Parameter name=\""
				+ BreedingViewDesignParameter.TIMELIMIT.getParameterName() + "\" value=\"" + AppConstants.EXP_DESIGN_TIME_LIMIT.getString()
				+ "\"/>" + "<Parameter name=\"" + BreedingViewDesignParameter.OUTPUTFILE.getParameterName()
				+ "\" value=\":outputFile\"/></Template></Templates>";

		final BVDesignRunner runner = new BVDesignRunner();
		final String xmlString = runner.getXMLStringForDesign(mainDesign);

		this.assertXMLStringEqualsExpected(mainDesign, expectedString, xmlString);
	}

	@Test
	public void testGetXMLStringForResolvableRowColumnDesignWithEntryNumber() {
		final ExperimentalDesignInput experimentalDesignInput = new ExperimentalDesignInput();
		experimentalDesignInput.setReplicationsCount(2);
		experimentalDesignInput.setRowsPerReplications(5);
		experimentalDesignInput.setColsPerReplications(10);
		experimentalDesignInput.setStartingPlotNo(301);
		experimentalDesignInput.setNrlatin(0);
		experimentalDesignInput.setNclatin(0);
		experimentalDesignInput.setUseLatenized(false);

		final MainDesign mainDesign = new ResolvableRowColumnDesignGenerator()
			.generate(experimentalDesignInput, ExperimentalDesignGeneratorTestDataUtil.getRowColVariablesMap(ROW, COL, PLOT_NO, ENTRY_NO, REP_NO), 24, null, null);

		final String expectedString =
			"<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?><Templates><Template name=\"ResolvableRowColumn\">"
				+ "<Parameter name=\"" + BreedingViewDesignParameter.SEED.getParameterName() + "\" value=\":seedValue\"/><Parameter name=\""
				+ BreedingViewDesignParameter.NTREATMENTS.getParameterName() + "\" value=\"24\"/>" + "<Parameter name=\""
				+ BreedingViewDesignParameter.NREPLICATES.getParameterName() + "\" value=\"2\"/><Parameter name=\""
				+ BreedingViewDesignParameter.NROWS.getParameterName() + "\" value=\"5\"/>" + "<Parameter name=\""
				+ BreedingViewDesignParameter.NCOLUMNS.getParameterName() + "\" value=\"10\"/><Parameter name=\""
				+ BreedingViewVariableParameter.ENTRY.getParameterName() + "\" value=\"" + ENTRY_NO + "\"/>" + "<Parameter name=\""
				+ BreedingViewDesignParameter.INITIAL_TREATMENT_NUMBER.getParameterName() + "\" value=\"1\"/>" + "<Parameter name=\""
				+BreedingViewVariableParameter.REP.getParameterName() + "\" value=\"" + REP_NO + "\"/><Parameter name=\""
				+BreedingViewVariableParameter.ROW.getParameterName() + "\" value=\"" + ROW + "\"/>" + "<Parameter name=\""
				+BreedingViewVariableParameter.COLUMN.getParameterName() + "\" value=\"" + COL + "\"/><Parameter name=\""
				+BreedingViewVariableParameter.PLOT.getParameterName() + "\" value=\"" + PLOT_NO + "\"/>" + "<Parameter name=\""
				+ BreedingViewDesignParameter.INITIAL_PLOT_NUMBER.getParameterName() + "\" value=\"301\"/>" + "<Parameter name=\""
				+ BreedingViewDesignParameter.NRLATIN.getParameterName() + "\" value=\"0\"/>" + "<Parameter name=\""
				+ BreedingViewDesignParameter.NCLATIN.getParameterName() + "\" value=\"0\"/>" + "<Parameter name=\""
				+ BreedingViewDesignParameter.TIMELIMIT.getParameterName() + "\" value=\"" + AppConstants.EXP_DESIGN_TIME_LIMIT.getString()
				+ "\"/>" + "<Parameter name=\"" + BreedingViewDesignParameter.OUTPUTFILE.getParameterName()
				+ "\" value=\":outputFile\"/></Template></Templates>";

		final BVDesignRunner runner = new BVDesignRunner();
		final String xmlString = runner.getXMLStringForDesign(mainDesign);
		this.assertXMLStringEqualsExpected(mainDesign, expectedString, xmlString);
	}

	@Test
	public void testGetXMLStringForPRepDesign() {
		final int numberOfBlocks = 1;
		final int nTreatments = 20;
		final ArrayList<ListItem> nRepeatsListItem = new ArrayList<>();
		nRepeatsListItem.add(new ListItem("1"));
		nRepeatsListItem.add(new ListItem("2"));
		nRepeatsListItem.add(new ListItem("3"));

		final int initialPlotNumber = 99;

		final ExperimentalDesignInput experimentalDesignInput = new ExperimentalDesignInput();
		experimentalDesignInput.setStartingPlotNo(initialPlotNumber);
		experimentalDesignInput.setNumberOfBlocks(numberOfBlocks);


		final MainDesign mainDesign = new PRepDesignGenerator()
			.generate(experimentalDesignInput, ExperimentalDesignGeneratorTestDataUtil.getPRepVariablesMap(BLOCK_NO, ENTRY_NO, PLOT_NO), nTreatments, null, Collections
				.singletonMap(BreedingViewDesignParameter.NREPEATS, nRepeatsListItem));

		final String expectedString = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"
			+ "<Templates>"
			+ "<Template name=\"" + ExperimentDesignType.P_REP.getBvDesignName() + "\"><"
			+ "Parameter name=\"" + BreedingViewDesignParameter.SEED.getParameterName() + "\" value=\":seedValue\"/>"
			+ "<Parameter name=\"" + BreedingViewDesignParameter.NTREATMENTS.getParameterName() + "\" value=\"20\"/>"
			+ "<Parameter name=\"" + BreedingViewDesignParameter.NBLOCKS.getParameterName() + "\" value=\"1\"/>"
			+ "<Parameter name=\"" + BreedingViewDesignParameter.NREPEATS.getParameterName() + "\">"
			+ "<ListItem value=\"1\"/>"
			+ "<ListItem value=\"2\"/>"
			+ "<ListItem value=\"3\"/>"
			+ "</Parameter>"
			+ "<Parameter name=\"" + BreedingViewVariableParameter.ENTRY.getParameterName() + "\" value=\"ENTRY_NO\"/>"
			+ "<Parameter name=\"" + BreedingViewDesignParameter.INITIAL_TREATMENT_NUMBER.getParameterName() + "\" value=\"1\"/>"
			+ "<Parameter name=\"" +BreedingViewVariableParameter.BLOCK.getParameterName() + "\" value=\"BLOCK_NO\"/>"
			+ "<Parameter name=\"" +BreedingViewVariableParameter.PLOT.getParameterName()
			+ "\" value=\"PLOT_NO\"/><Parameter name=\"initialplotnum\" value=\"99\"/>"
			+ "<Parameter name=\"" + BreedingViewDesignParameter.TIMELIMIT.getParameterName() + "\" value=\"0.1\"/>"
			+ "<Parameter name=\"" + BreedingViewDesignParameter.OUTPUTFILE.getParameterName()
			+ "\" value=\":outputFile\"/>"
			+ "</Template>"
			+ "</Templates>";

		final BVDesignRunner runner = new BVDesignRunner();
		final String xmlString = runner.getXMLStringForDesign(mainDesign);

		this.assertXMLStringEqualsExpected(mainDesign, expectedString, xmlString);
	}

	private void assertXMLStringEqualsExpected(final MainDesign mainDesign, String expectedString, final String xmlString) {
		final String outputFile = mainDesign.getDesign().getParameterValue(BreedingViewDesignParameter.OUTPUTFILE.getParameterName());
		final String outputFileMillisecs = outputFile.replace(BVDesignRunner.BV_PREFIX + BVDesignRunner.CSV_EXTENSION, "");
		final String seedValue = this.getSeedValue(outputFileMillisecs);
		expectedString = expectedString.replace(":seedValue", seedValue);
		expectedString = expectedString.replace(":outputFile", outputFile);

		assertEquals(expectedString, xmlString);
	}

	private String getSeedValue(final String currentTimeMillis) {
		String seedValue = currentTimeMillis;
		if (Long.parseLong(currentTimeMillis) > Integer.MAX_VALUE) {
			seedValue = seedValue.substring(seedValue.length() - 9);
		}
		return seedValue;
	}

	private MainDesign createRandomizedCompleteBlockDesign() {

		final List<String> treatmentFactors = new ArrayList<>();
		treatmentFactors.add("ENTRY_NO");
		treatmentFactors.add("FERTILIZER");

		final List<String> levels = new ArrayList<>();
		levels.add("24");
		levels.add("3");

		final ExperimentalDesignInput experimentalDesignInput = new ExperimentalDesignInput();
		experimentalDesignInput.setNumberOfBlocks(6);
		experimentalDesignInput.setStartingPlotNo(301);

		return new RandomizeCompleteBlockDesignGenerator()
			.generate(experimentalDesignInput, ExperimentalDesignGeneratorTestDataUtil.getRCBDVariablesMap(REP_NO, PLOT_NO), null, null,
				ExperimentalDesignGeneratorTestDataUtil.getTreatmentFactorsParametersMap(treatmentFactors, levels));
	}

}
