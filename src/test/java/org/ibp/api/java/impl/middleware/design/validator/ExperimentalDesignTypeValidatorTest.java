package org.ibp.api.java.impl.middleware.design.validator;

import org.apache.commons.lang3.RandomStringUtils;
import org.generationcp.middleware.domain.dms.ExperimentDesignType;
import org.generationcp.middleware.domain.dms.InsertionMannerItem;
import org.generationcp.middleware.domain.gms.SystemDefinedEntryType;
import org.generationcp.middleware.domain.oms.TermId;
import org.generationcp.middleware.service.api.study.StudyEntryDto;
import org.generationcp.middleware.service.api.study.StudyEntryPropertyData;
import org.ibp.api.exception.ApiRequestValidationException;
import org.ibp.api.rest.design.ExperimentalDesignInput;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.validation.BindingResult;
import org.springframework.validation.MapBindingResult;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.MatcherAssert.assertThat;

public class ExperimentalDesignTypeValidatorTest {
	
	private ExperimentalDesignTypeValidator designTypeValidator = new ExperimentalDesignTypeValidator();

	@Before
	public void init() {
		this.designTypeValidator = new ExperimentalDesignTypeValidator();
	}

	@Test
	public void testValidateAugmentedDesignSuccess() {

		final ExperimentalDesignInput designInput = new ExperimentalDesignInput();
		designInput.setNumberOfBlocks(2);
		designInput.setStartingPlotNo(1);
		designInput.setDesignType(ExperimentDesignType.AUGMENTED_RANDOMIZED_BLOCK.getId());

		final List<StudyEntryDto> importedGermplasmList = this.createStudyGermplasmList();
		// Make the first ImportedGermplasm a check entry type.
		importedGermplasmList.get(0).getProperties().get(TermId.ENTRY_TYPE.getId()).setCategoricalValueId(SystemDefinedEntryType.CHECK_ENTRY.getEntryTypeCategoricalId());

		try {
			this.designTypeValidator.validate(designInput, importedGermplasmList);
		} catch (final ApiRequestValidationException e) {
			Assert.fail("validateAugmentedDesign() should not throw an ApiRequestValidationException.");
		}
	}

	@Test
	public void testValidateAugmentedDesignFail_WhenNoGermplasmList() {

		final ExperimentalDesignInput designInput = new ExperimentalDesignInput();
		designInput.setNumberOfBlocks(2);
		designInput.setStartingPlotNo(1);
		designInput.setDesignType(ExperimentDesignType.AUGMENTED_RANDOMIZED_BLOCK.getId());


		try {
			this.designTypeValidator.validate(designInput, new ArrayList<>());
			Assert.fail("validateAugmentedDesign() should throw an ApiRequestValidationException.");
		} catch (final ApiRequestValidationException e) {
			assertThat(Arrays.asList(e.getErrors().get(0).getCodes()),
				hasItem("experiment.design.generate.no.germplasm"));
		}
	}

	@Test
	public void testValidateAugmentedDesignFail_WhenNoChecks() {

		final ExperimentalDesignInput designInput = new ExperimentalDesignInput();
		designInput.setNumberOfBlocks(2);
		designInput.setStartingPlotNo(1);
		designInput.setDesignType(ExperimentDesignType.AUGMENTED_RANDOMIZED_BLOCK.getId());

		final List<StudyEntryDto> importedGermplasmList = this.createStudyGermplasmList();

		try {
			this.designTypeValidator.validate(designInput, importedGermplasmList);
			Assert.fail("validateAugmentedDesign() should throw an ApiRequestValidationException.");
		} catch (final ApiRequestValidationException e) {
			assertThat(Arrays.asList(e.getErrors().get(0).getCodes()),
				hasItem("germplasm.list.check.required.augmented.design"));
		}
	}

	@Test
	public void testValidateAugmentedDesignFail_WhenTreatmentFactorsPresent() {

		final ExperimentalDesignInput designInput = new ExperimentalDesignInput();
		designInput.setNumberOfBlocks(2);
		designInput.setStartingPlotNo(1);
		designInput.setDesignType(ExperimentDesignType.AUGMENTED_RANDOMIZED_BLOCK.getId());

		final Map<String, String> treatmentFactorsData = new HashMap<>();
		treatmentFactorsData.put("1", "100");
		designInput.setTreatmentFactorsData(treatmentFactorsData);

		final List<StudyEntryDto> importedGermplasmList = this.createStudyGermplasmList();
		// Make the first ImportedGermplasm a check entry type.
		importedGermplasmList.get(0).getProperties().get(TermId.ENTRY_TYPE.getId()).setCategoricalValueId(SystemDefinedEntryType.CHECK_ENTRY.getEntryTypeCategoricalId());

		try {
			this.designTypeValidator.validate(designInput, importedGermplasmList);
			Assert.fail("validateAugmentedDesign() should throw an ApiRequestValidationException.");
		} catch (final ApiRequestValidationException e) {
			assertThat(Arrays.asList(e.getErrors().get(0).getCodes()),
				hasItem("experiment.design.treatment.factors.error"));
		}
	}

	@Test
	public void testValidateRandomizedCompleteBlockDesignSuccess() {

		final ExperimentalDesignInput designInput = new ExperimentalDesignInput();
		designInput.setNumberOfBlocks(2);
		designInput.setStartingPlotNo(1);
		designInput.setReplicationsCount(2);
		designInput.setDesignType(ExperimentDesignType.RANDOMIZED_COMPLETE_BLOCK.getId());

		final List<StudyEntryDto> importedGermplasmList = this.createStudyGermplasmList();

		try {
			this.designTypeValidator.validate(designInput, importedGermplasmList);
		} catch (final ApiRequestValidationException e) {
			Assert.fail("Should not throw an ApiRequestValidationException.");
		}
	}

	@Test
	public void testValidateRandomizedCompleteBlockDesignFail_WhenNoReplicationCount() {

		final ExperimentalDesignInput designInput = new ExperimentalDesignInput();
		designInput.setNumberOfBlocks(2);
		designInput.setStartingPlotNo(1);
		designInput.setDesignType(ExperimentDesignType.RANDOMIZED_COMPLETE_BLOCK.getId());

		final List<StudyEntryDto> importedGermplasmList = this.createStudyGermplasmList();

		try {
			this.designTypeValidator.validate(designInput, importedGermplasmList);
			Assert.fail("Should throw an ApiRequestValidationException.");
		} catch (final ApiRequestValidationException e) {
			assertThat(Arrays.asList(e.getErrors().get(0).getCodes()),
				hasItem("experiment.design.replication.count.rcbd.error"));
		}
	}

	@Test
	public void testValidateRandomizedCompleteBlockDesignFail_WhenNoGermplasmList() {

		final ExperimentalDesignInput designInput = new ExperimentalDesignInput();
		designInput.setNumberOfBlocks(2);
		designInput.setStartingPlotNo(1);
		designInput.setDesignType(ExperimentDesignType.RANDOMIZED_COMPLETE_BLOCK.getId());

		try {
			this.designTypeValidator.validate(designInput, new ArrayList<>());
			Assert.fail("Should throw an ApiRequestValidationException.");
		} catch (final ApiRequestValidationException e) {
			assertThat(Arrays.asList(e.getErrors().get(0).getCodes()),
				hasItem("experiment.design.generate.no.germplasm"));
		}
	}

	@Test
	public void testValidateResolvableRowColumnDesignFail_WhenNoReplicationCount() {

		final ExperimentalDesignInput designInput = new ExperimentalDesignInput();
		designInput.setNumberOfBlocks(2);
		designInput.setStartingPlotNo(1);
		designInput.setRowsPerReplications(1);
		designInput.setColsPerReplications(1);
		designInput.setDesignType(ExperimentDesignType.ROW_COL.getId());

		final List<StudyEntryDto> importedGermplasmList = this.createStudyGermplasmList();

		try {
			this.designTypeValidator.validate(designInput, importedGermplasmList);
			Assert.fail("Should throw an ApiRequestValidationException.");
		} catch (final ApiRequestValidationException e) {
			assertThat(Arrays.asList(e.getErrors().get(0).getCodes()),
				hasItem("experiment.design.replication.count.resolvable.error"));
		}
	}

	@Test
	public void testValidateResolvableRowColumnDesignSuccess() {
		final ExperimentalDesignInput designInput = new ExperimentalDesignInput();
		designInput.setStartingPlotNo(1);
		designInput.setReplicationsCount(1);
		designInput.setRowsPerReplications(2);
		designInput.setColsPerReplications(5);
		designInput.setDesignType(ExperimentDesignType.ROW_COL.getId());


		final List<StudyEntryDto> importedGermplasmList = this.createStudyGermplasmList();
		try {
			this.designTypeValidator.validate(designInput, importedGermplasmList);
		} catch (final ApiRequestValidationException e) {
			Assert.fail("Should not throw an ApiRequestValidationException.");
		}
	}

	@Test
	public void testValidateResolvableIncompleteBlockDesignFail_TreatmentFactorsPresent() {
		final ExperimentalDesignInput designInput = new ExperimentalDesignInput();
		designInput.setStartingPlotNo(1);
		designInput.setReplicationsCount(1);
		designInput.setBlockSize(1);
		designInput.setDesignType(ExperimentDesignType.RESOLVABLE_INCOMPLETE_BLOCK.getId());

		final Map<String, String> treatmentFactorsData = new HashMap<>();
		treatmentFactorsData.put("1", "100");
		designInput.setTreatmentFactorsData(treatmentFactorsData);

		final List<StudyEntryDto> importedGermplasmList = this.createStudyGermplasmList();
		try {
			this.designTypeValidator.validate(designInput, importedGermplasmList);
			Assert.fail("Should throw an ApiRequestValidationException.");
		} catch (final ApiRequestValidationException e) {
			assertThat(Arrays.asList(e.getErrors().get(0).getCodes()),
				hasItem("experiment.design.treatment.factors.error"));
		}
	}

	@Test
	public void testValidateResolvableIncompleteBlockDesignFail_NoReplicationCount() {
		final ExperimentalDesignInput designInput = new ExperimentalDesignInput();
		designInput.setStartingPlotNo(1);
		designInput.setBlockSize(1);
		designInput.setDesignType(ExperimentDesignType.RESOLVABLE_INCOMPLETE_BLOCK.getId());

		final List<StudyEntryDto> importedGermplasmList = this.createStudyGermplasmList();
		try {
			this.designTypeValidator.validate(designInput, importedGermplasmList);
			Assert.fail("Should throw an ApiRequestValidationException.");
		} catch (final ApiRequestValidationException e) {
			assertThat(Arrays.asList(e.getErrors().get(0).getCodes()),
				hasItem("experiment.design.replication.count.resolvable.error"));
		}
	}

	@Test
	public void testValidateResolvableIncompleteBlockDesignFail_NoBlockSize() {
		final ExperimentalDesignInput designInput = new ExperimentalDesignInput();
		designInput.setStartingPlotNo(1);
		designInput.setReplicationsCount(1);
		designInput.setDesignType(ExperimentDesignType.RESOLVABLE_INCOMPLETE_BLOCK.getId());

		final List<StudyEntryDto> importedGermplasmList = this.createStudyGermplasmList();
		try {
			this.designTypeValidator.validate(designInput, importedGermplasmList);
			Assert.fail("Should throw an ApiRequestValidationException.");
		} catch (final ApiRequestValidationException e) {
			assertThat(Arrays.asList(e.getErrors().get(0).getCodes()),
				hasItem("experiment.design.block.size.should.be.a.greater.than.1"));
		}
	}

	@Test
	public void testValidateResolvableIncompleteBlockDesignFail_InvalidBlockLevel() {
		final ExperimentalDesignInput designInput = new ExperimentalDesignInput();
		designInput.setStartingPlotNo(1);
		designInput.setReplicationsCount(1);
		designInput.setBlockSize(10);
		designInput.setDesignType(ExperimentDesignType.RESOLVABLE_INCOMPLETE_BLOCK.getId());

		final List<StudyEntryDto> importedGermplasmList = this.createStudyGermplasmList();
		try {
			this.designTypeValidator.validate(designInput, importedGermplasmList);
			Assert.fail("Should throw an ApiRequestValidationException.");
		} catch (final ApiRequestValidationException e) {
			assertThat(Arrays.asList(e.getErrors().get(0).getCodes()),
				hasItem("experiment.design.block.level.should.be.greater.than.one"));
		}
	}

	@Test
	public void testValidateResolvableIncompleteBlockDesignFail_InvalidBlockSize() {
		final ExperimentalDesignInput designInput = new ExperimentalDesignInput();
		designInput.setStartingPlotNo(1);
		designInput.setReplicationsCount(1);
		designInput.setBlockSize(3);
		designInput.setDesignType(ExperimentDesignType.RESOLVABLE_INCOMPLETE_BLOCK.getId());

		final List<StudyEntryDto> importedGermplasmList = this.createStudyGermplasmList();
		try {
			this.designTypeValidator.validate(designInput, importedGermplasmList);
			Assert.fail("Should throw an ApiRequestValidationException.");
		} catch (final ApiRequestValidationException e) {
			assertThat(Arrays.asList(e.getErrors().get(0).getCodes()),
				hasItem("experiment.design.block.size.not.a.factor.of.treatment.size"));
		}
	}

	@Test
	public void testValidateResolvableIncompleteBlockDesignFail_WhenNoGermplasmList() {
		final ExperimentalDesignInput designInput = new ExperimentalDesignInput();
		designInput.setBlockSize(1);
		designInput.setStartingPlotNo(1);
		designInput.setReplicationsCount(1);
		designInput.setDesignType(ExperimentDesignType.RESOLVABLE_INCOMPLETE_BLOCK.getId());

		try {
			this.designTypeValidator.validate(designInput, new ArrayList<>());
			Assert.fail("Should throw an ApiRequestValidationException.");
		} catch (final ApiRequestValidationException e) {
			assertThat(Arrays.asList(e.getErrors().get(0).getCodes()),
				hasItem("experiment.design.generate.no.germplasm"));
		}
	}

	@Test
	public void testValidateResolvableRowColumnDesignFail_TreatmentFactorsPresent() {
		final ExperimentalDesignInput designInput = new ExperimentalDesignInput();
		designInput.setStartingPlotNo(1);
		designInput.setReplicationsCount(1);
		designInput.setRowsPerReplications(1);
		designInput.setColsPerReplications(1);
		designInput.setDesignType(ExperimentDesignType.ROW_COL.getId());

		final Map<String, String> treatmentFactorsData = new HashMap<>();
		treatmentFactorsData.put("1", "100");
		designInput.setTreatmentFactorsData(treatmentFactorsData);

		final List<StudyEntryDto> importedGermplasmList = this.createStudyGermplasmList();
		try {
			this.designTypeValidator.validate(designInput, importedGermplasmList);
			Assert.fail("Should throw an ApiRequestValidationException.");
		} catch (final ApiRequestValidationException e) {
			assertThat(Arrays.asList(e.getErrors().get(0).getCodes()),
				hasItem("experiment.design.treatment.factors.error"));
		}
	}

	@Test
	public void testValidateResolvableRowColumnDesignFail_InvalidRowColProduct() {
		final ExperimentalDesignInput designInput = new ExperimentalDesignInput();
		designInput.setStartingPlotNo(1);
		designInput.setReplicationsCount(1);
		designInput.setRowsPerReplications(1);
		designInput.setColsPerReplications(1);
		designInput.setDesignType(ExperimentDesignType.ROW_COL.getId());

		final List<StudyEntryDto> importedGermplasmList = this.createStudyGermplasmList();
		try {
			this.designTypeValidator.validate(designInput, importedGermplasmList);
			Assert.fail("Should throw an ApiRequestValidationException.");
		} catch (final ApiRequestValidationException e) {
			assertThat(Arrays.asList(e.getErrors().get(0).getCodes()),
				hasItem("experiment.design.resolvable.incorrect.row.and.col.product.to.germplasm.size"));
		}
	}

	@Test
	public void testValidateResolvableRowColumnDesignFail_WhenNoGermplasmList() {
		final ExperimentalDesignInput designInput = new ExperimentalDesignInput();
		designInput.setNumberOfBlocks(2);
		designInput.setStartingPlotNo(1);
		designInput.setDesignType(ExperimentDesignType.ROW_COL.getId());

		try {
			this.designTypeValidator.validate(designInput, new ArrayList<>());
			Assert.fail("Should throw an ApiRequestValidationException.");
		} catch (final ApiRequestValidationException e) {
			assertThat(Arrays.asList(e.getErrors().get(0).getCodes()),
				hasItem("experiment.design.generate.no.germplasm"));
		}
	}


	@Test
	public void testValidatePrepDesignSuccess() {
		final ExperimentalDesignInput designInput = new ExperimentalDesignInput();
		designInput.setStartingPlotNo(1);
		designInput.setReplicationPercentage(50);
		designInput.setDesignType(ExperimentDesignType.P_REP.getId());

		final List<StudyEntryDto> importedGermplasmList = this.createStudyGermplasmList();
		try {
			this.designTypeValidator.validate(designInput, importedGermplasmList);
		} catch (final ApiRequestValidationException e) {
			Assert.fail("Should not throw an ApiRequestValidationException.");
		}
	}

	@Test
	public void testValidatePrepDesignFail_NoReplicationPercentage() {
		final ExperimentalDesignInput designInput = new ExperimentalDesignInput();
		designInput.setDesignType(ExperimentDesignType.P_REP.getId());

		final List<StudyEntryDto> importedGermplasmList = this.createStudyGermplasmList();
		try {
			this.designTypeValidator.validate(designInput, importedGermplasmList);
			Assert.fail("Should throw an ApiRequestValidationException.");
		} catch (final ApiRequestValidationException e) {
			assertThat(Arrays.asList(e.getErrors().get(0).getCodes()),
				hasItem("experiment.design.replication.percentage.should.be.between.zero.and.hundred"));
		}
	}

	@Test
	public void testValidatePrepDesignFail_InvalidReplicationPercentage() {
		final ExperimentalDesignInput designInput = new ExperimentalDesignInput();
		designInput.setStartingPlotNo(1);
		designInput.setReplicationPercentage(101);
		designInput.setDesignType(ExperimentDesignType.P_REP.getId());

		final List<StudyEntryDto> importedGermplasmList = this.createStudyGermplasmList();
		try {
			this.designTypeValidator.validate(designInput, importedGermplasmList);
			Assert.fail("Should throw an ApiRequestValidationException.");
		} catch (final ApiRequestValidationException e) {
			assertThat(Arrays.asList(e.getErrors().get(0).getCodes()),
				hasItem("experiment.design.replication.percentage.should.be.between.zero.and.hundred"));
		}
	}

	@Test
	public void testValidatePrepDesignFail_TreatmentFactorsPresent() {
		final ExperimentalDesignInput designInput = new ExperimentalDesignInput();
		designInput.setStartingPlotNo(1);
		designInput.setReplicationPercentage(50);
		designInput.setDesignType(ExperimentDesignType.P_REP.getId());

		final Map<String, String> treatmentFactorsData = new HashMap<>();
		treatmentFactorsData.put("1", "100");
		designInput.setTreatmentFactorsData(treatmentFactorsData);

		final List<StudyEntryDto> importedGermplasmList = this.createStudyGermplasmList();
		try {
			this.designTypeValidator.validate(designInput, importedGermplasmList);
			Assert.fail("Should throw an ApiRequestValidationException.");
		} catch (final ApiRequestValidationException e) {
			assertThat(Arrays.asList(e.getErrors().get(0).getCodes()),
				hasItem("experiment.design.treatment.factors.error"));
		}
	}

	@Test
	public void testValidateEntryListOrderDesignSuccess_WithChecks() {
		final ExperimentalDesignInput designInput = new ExperimentalDesignInput();
		designInput.setStartingPlotNo(1);
		designInput.setCheckSpacing(1);
		designInput.setCheckStartingPosition(1);
		designInput.setCheckInsertionManner(InsertionMannerItem.INSERT_ALL_CHECKS.getId());
		designInput.setDesignType(ExperimentDesignType.ENTRY_LIST_ORDER.getId());

		final List<StudyEntryDto> importedGermplasmList = this.createStudyGermplasmList();
		importedGermplasmList.get(0).getProperties().get(TermId.ENTRY_TYPE.getId()).setCategoricalValueId(SystemDefinedEntryType.CHECK_ENTRY.getEntryTypeCategoricalId());
		try {
			this.designTypeValidator.validate(designInput, importedGermplasmList);
		} catch (final ApiRequestValidationException e) {
			Assert.fail("Should not throw an ApiRequestValidationException.");
		}
	}

	@Test
	public void testValidateEntryListOrderDesignSuccess_NoChecks() {
		final ExperimentalDesignInput designInput = new ExperimentalDesignInput();
		designInput.setStartingPlotNo(1);
		designInput.setDesignType(ExperimentDesignType.ENTRY_LIST_ORDER.getId());

		final List<StudyEntryDto> importedGermplasmList = this.createStudyGermplasmList();
		try {
			this.designTypeValidator.validate(designInput, importedGermplasmList);
		} catch (final ApiRequestValidationException e) {
			Assert.fail("Should not throw an ApiRequestValidationException.");
		}
	}

	@Test
	public void testValidateEntryListOrderDesignFail_TreatmentFactorsPresent() {
		final ExperimentalDesignInput designInput = new ExperimentalDesignInput();
		designInput.setStartingPlotNo(1);
		designInput.setReplicationPercentage(50);
		designInput.setDesignType(ExperimentDesignType.ENTRY_LIST_ORDER.getId());

		final Map<String, String> treatmentFactorsData = new HashMap<>();
		treatmentFactorsData.put("1", "100");
		designInput.setTreatmentFactorsData(treatmentFactorsData);

		final List<StudyEntryDto> importedGermplasmList = this.createStudyGermplasmList();
		importedGermplasmList.get(0).getProperties().get(TermId.ENTRY_TYPE.getId()).setCategoricalValueId(SystemDefinedEntryType.CHECK_ENTRY.getEntryTypeCategoricalId());
		try {
			this.designTypeValidator.validate(designInput, importedGermplasmList);
			Assert.fail("Should throw an ApiRequestValidationException.");
		} catch (final ApiRequestValidationException e) {
			assertThat(Arrays.asList(e.getErrors().get(0).getCodes()),
				hasItem("experiment.design.treatment.factors.error"));
		}
	}

	@Test
	public void testValidateEntryListOrderDesignFail_NoTestEntries() {
		final ExperimentalDesignInput designInput = new ExperimentalDesignInput();
		designInput.setStartingPlotNo(1);
		designInput.setDesignType(ExperimentDesignType.ENTRY_LIST_ORDER.getId());

		final List<StudyEntryDto> importedGermplasmList = this.createStudyGermplasmList();
		for (final StudyEntryDto dto : importedGermplasmList) {
			dto.getProperties().get(TermId.ENTRY_TYPE.getId()).setCategoricalValueId(SystemDefinedEntryType.CHECK_ENTRY.getEntryTypeCategoricalId());
		}
		try {
			this.designTypeValidator.validate(designInput, importedGermplasmList);
			Assert.fail("Should throw an ApiRequestValidationException.");
		} catch (final ApiRequestValidationException e) {
			assertThat(Arrays.asList(e.getErrors().get(0).getCodes()),
				hasItem("germplasm.list.all.entries.can.not.be.checks"));
		}
	}

	@Test
	public void testValidateEntryListOrderDesignFail_NoCheckInsertionMannerForChecks() {
		final ExperimentalDesignInput designInput = new ExperimentalDesignInput();
		designInput.setStartingPlotNo(1);
		designInput.setCheckSpacing(1);
		designInput.setCheckStartingPosition(1);
		designInput.setDesignType(ExperimentDesignType.ENTRY_LIST_ORDER.getId());

		final List<StudyEntryDto> importedGermplasmList = this.createStudyGermplasmList();
		importedGermplasmList.get(0).getProperties().get(TermId.ENTRY_TYPE.getId()).setCategoricalValueId(SystemDefinedEntryType.CHECK_ENTRY.getEntryTypeCategoricalId());
		try {
			this.designTypeValidator.validate(designInput, importedGermplasmList);
			Assert.fail("Should throw an ApiRequestValidationException.");
		} catch (final ApiRequestValidationException e) {
			assertThat(Arrays.asList(e.getErrors().get(0).getCodes()),
				hasItem("check.manner.of.insertion.invalid"));
		}
	}

	@Test
	public void testValidateEntryListOrderDesignFail_NoCheckSpacing() {
		final ExperimentalDesignInput designInput = new ExperimentalDesignInput();
		designInput.setStartingPlotNo(1);
		designInput.setCheckStartingPosition(1);
		designInput.setCheckInsertionManner(InsertionMannerItem.INSERT_ALL_CHECKS.getId());
		designInput.setDesignType(ExperimentDesignType.ENTRY_LIST_ORDER.getId());

		final List<StudyEntryDto> importedGermplasmList = this.createStudyGermplasmList();
		importedGermplasmList.get(0).getProperties().get(TermId.ENTRY_TYPE.getId()).setCategoricalValueId(SystemDefinedEntryType.CHECK_ENTRY.getEntryTypeCategoricalId());
		try {
			this.designTypeValidator.validate(designInput, importedGermplasmList);
			Assert.fail("Should throw an ApiRequestValidationException.");
		} catch (final ApiRequestValidationException e) {
			assertThat(Arrays.asList(e.getErrors().get(0).getCodes()),
				hasItem("germplasm.list.number.of.rows.between.insertion.should.be.a.whole.number"));
		}
	}

	@Test
	public void testValidateEntryListOrderDesignFail_NoCheckStartingPosition() {
		final ExperimentalDesignInput designInput = new ExperimentalDesignInput();
		designInput.setStartingPlotNo(1);
		designInput.setCheckSpacing(1);
		designInput.setCheckInsertionManner(InsertionMannerItem.INSERT_ALL_CHECKS.getId());
		designInput.setDesignType(ExperimentDesignType.ENTRY_LIST_ORDER.getId());

		final List<StudyEntryDto> importedGermplasmList = this.createStudyGermplasmList();
		importedGermplasmList.get(0).getProperties().get(TermId.ENTRY_TYPE.getId()).setCategoricalValueId(SystemDefinedEntryType.CHECK_ENTRY.getEntryTypeCategoricalId());
		try {
			this.designTypeValidator.validate(designInput, importedGermplasmList);
			Assert.fail("Should throw an ApiRequestValidationException.");
		} catch (final ApiRequestValidationException e) {
			assertThat(Arrays.asList(e.getErrors().get(0).getCodes()),
				hasItem("germplasm.list.start.index.whole.number.error"));
		}
	}

	@Test
	public void testValidateEntryListOrderDesignFail_InvalidCheckStartingPosition() {
		final ExperimentalDesignInput designInput = new ExperimentalDesignInput();
		designInput.setStartingPlotNo(1);
		designInput.setCheckSpacing(1);
		designInput.setCheckStartingPosition(100);
		designInput.setCheckInsertionManner(InsertionMannerItem.INSERT_ALL_CHECKS.getId());
		designInput.setDesignType(ExperimentDesignType.ENTRY_LIST_ORDER.getId());

		final List<StudyEntryDto> importedGermplasmList = this.createStudyGermplasmList();
		importedGermplasmList.get(0).getProperties().get(TermId.ENTRY_TYPE.getId()).setCategoricalValueId(SystemDefinedEntryType.CHECK_ENTRY.getEntryTypeCategoricalId());
		try {
			this.designTypeValidator.validate(designInput, importedGermplasmList);
			Assert.fail("Should throw an ApiRequestValidationException.");
		} catch (final ApiRequestValidationException e) {
			assertThat(Arrays.asList(e.getErrors().get(0).getCodes()),
				hasItem("germplasm.list.start.index.less.than.germplasm.error"));
		}
	}

	@Test
	public void testValidateEntryListOrderDesignFail_InvalidCheckSpacing() {
		final ExperimentalDesignInput designInput = new ExperimentalDesignInput();
		designInput.setStartingPlotNo(1);
		designInput.setCheckSpacing(100);
		designInput.setCheckStartingPosition(1);
		designInput.setCheckInsertionManner(InsertionMannerItem.INSERT_ALL_CHECKS.getId());
		designInput.setDesignType(ExperimentDesignType.ENTRY_LIST_ORDER.getId());

		final List<StudyEntryDto> importedGermplasmList = this.createStudyGermplasmList();
		importedGermplasmList.get(0).getProperties().get(TermId.ENTRY_TYPE.getId()).setCategoricalValueId(SystemDefinedEntryType.CHECK_ENTRY.getEntryTypeCategoricalId());
		try {
			this.designTypeValidator.validate(designInput, importedGermplasmList);
			Assert.fail("Should throw an ApiRequestValidationException.");
		} catch (final ApiRequestValidationException e) {
			assertThat(Arrays.asList(e.getErrors().get(0).getCodes()),
				hasItem("germplasm.list.spacing.less.than.germplasm.error"));
		}
	}

	@Test
	public void testValidateReplicationCountLimit_ExceedsMaximum() {
		final BindingResult errors = new MapBindingResult(new HashMap<String, String>(), Integer.class.getName());
		this.designTypeValidator.setErrors(errors);

		final String errorCode = RandomStringUtils.randomAlphabetic(20);
		final ExperimentalDesignInput experimentalDesignInput = new ExperimentalDesignInput();
		experimentalDesignInput.setReplicationsCount(13);
		this.designTypeValidator.validateReplicationCountLimit(experimentalDesignInput, errorCode);
		assertThat(Arrays.asList(errors.getAllErrors().get(0).getCodes()),
			hasItem(errorCode));
	}

	@Test
	public void testValidateReplicationCountLimit_WhenZero() {
		final BindingResult errors = new MapBindingResult(new HashMap<String, String>(), Integer.class.getName());
		this.designTypeValidator.setErrors(errors);

		final String errorCode = RandomStringUtils.randomAlphabetic(20);
		final ExperimentalDesignInput experimentalDesignInput = new ExperimentalDesignInput();
		experimentalDesignInput.setReplicationsCount(0);
		this.designTypeValidator.validateReplicationCountLimit(experimentalDesignInput, errorCode);
		assertThat(Arrays.asList(errors.getAllErrors().get(0).getCodes()),
			hasItem(errorCode));
	}

	@Test
	public void testValidateReplicationCountLimit_LessThanOne() {
		final BindingResult errors = new MapBindingResult(new HashMap<String, String>(), Integer.class.getName());
		this.designTypeValidator.setErrors(errors);

		final String errorCode = RandomStringUtils.randomAlphabetic(20);
		final ExperimentalDesignInput experimentalDesignInput = new ExperimentalDesignInput();
		experimentalDesignInput.setReplicationsCount(0);
		this.designTypeValidator.validateReplicationCountLimit(experimentalDesignInput, errorCode);
		assertThat(Arrays.asList(errors.getAllErrors().get(0).getCodes()),
			hasItem(errorCode));
	}

	@Test
	public void testValidateIfCheckEntriesExistInGermplasmList_WhenCheckEntriesExist() {
		final BindingResult errors = new MapBindingResult(new HashMap<String, String>(), Integer.class.getName());
		this.designTypeValidator.setErrors(errors);

		final List<StudyEntryDto> importedGermplasmList = this.createStudyGermplasmList();
		// Make the first ImportedGermplasm a check entry type.
		importedGermplasmList.get(0).getProperties().get(TermId.ENTRY_TYPE.getId()).setCategoricalValueId(SystemDefinedEntryType.CHECK_ENTRY.getEntryTypeCategoricalId());

		this.designTypeValidator.validateIfCheckEntriesExistInstudyEntryDtoList(importedGermplasmList);
		Assert.assertFalse(errors.hasErrors());
	}

	@Test
	public void testValidateIfCheckEntriesExistInGermplasmList_WhenCheckEntriesDoNotExist() {
		final BindingResult errors = new MapBindingResult(new HashMap<String, String>(), Integer.class.getName());
		this.designTypeValidator.setErrors(errors);
		final List<StudyEntryDto> importedGermplasmList = this.createStudyGermplasmList();

		this.designTypeValidator.validateIfCheckEntriesExistInstudyEntryDtoList(importedGermplasmList);
		assertThat(Arrays.asList(errors.getAllErrors().get(0).getCodes()),
			hasItem("germplasm.list.check.required.augmented.design"));
	}

	@Test
	public void testValidateStartingPlotNoSuccess() {
		final BindingResult errors = new MapBindingResult(new HashMap<String, String>(), Integer.class.getName());
		this.designTypeValidator.setErrors(errors);
		final int treatmentSize = 10;

		final ExperimentalDesignInput experimentalDesignInput = new ExperimentalDesignInput();
		experimentalDesignInput.setStartingPlotNo(1);

		this.designTypeValidator.validateStartingPlotNo(experimentalDesignInput, treatmentSize);
		Assert.assertFalse(errors.hasErrors());
	}

	@Test
	public void testValidateStartingPlotNo_Empty() {
		final BindingResult errors = new MapBindingResult(new HashMap<String, String>(), Integer.class.getName());
		this.designTypeValidator.setErrors(errors);
		final int treatmentSize = 10;

		final ExperimentalDesignInput experimentalDesignInput = new ExperimentalDesignInput();

		this.designTypeValidator.validateStartingPlotNo(experimentalDesignInput, treatmentSize);
		assertThat(Arrays.asList(errors.getAllErrors().get(0).getCodes()),
			hasItem("plot.number.should.be.in.range"));
	}

	@Test
	public void testValidateStartingPlotNo_OutOfRange() {
		final BindingResult errors = new MapBindingResult(new HashMap<String, String>(), Integer.class.getName());
		this.designTypeValidator.setErrors(errors);
		final int treatmentSize = 10;

		final ExperimentalDesignInput experimentalDesignInput = new ExperimentalDesignInput();
		experimentalDesignInput.setStartingPlotNo(100000000);

		this.designTypeValidator.validateStartingPlotNo(experimentalDesignInput, treatmentSize);
		assertThat(Arrays.asList(errors.getAllErrors().get(0).getCodes()),
			hasItem("plot.number.should.be.in.range"));
	}

	@Test
	public void testValidateNoTreatmentFactors_WhenNoTreatmentFactors() {
		final BindingResult errors = new MapBindingResult(new HashMap<String, String>(), Integer.class.getName());
		this.designTypeValidator.setErrors(errors);

		final ExperimentalDesignInput experimentalDesignInput = new ExperimentalDesignInput();
		experimentalDesignInput.setTreatmentFactorsData(new HashMap<>());

		this.designTypeValidator.validateNoTreatmentFactors(experimentalDesignInput);
		Assert.assertFalse(errors.hasErrors());
	}

	@Test
	public void testValidateNoTreatmentFactors_WhenTreatmentFactorsPresent() {
		final BindingResult errors = new MapBindingResult(new HashMap<String, String>(), Integer.class.getName());
		this.designTypeValidator.setErrors(errors);

		final ExperimentalDesignInput experimentalDesignInput = new ExperimentalDesignInput();
		final Map<String, String> treatmentFactorsData = new HashMap<>();
		treatmentFactorsData.put("1", "100");
		experimentalDesignInput.setTreatmentFactorsData(treatmentFactorsData);

		this.designTypeValidator.validateNoTreatmentFactors(experimentalDesignInput);
		assertThat(Arrays.asList(errors.getAllErrors().get(0).getCodes()),
			hasItem("experiment.design.treatment.factors.error"));
	}



	private List<StudyEntryDto> createStudyGermplasmList() {

		final List<StudyEntryDto> importedGermplasmList = new LinkedList<>();

		// Create 10 imported germplasm entries
		for (int i = 1; i <= 10; i++) {
			importedGermplasmList.add(this.createGermplasm(i));
		}

		return importedGermplasmList;

	}

	private StudyEntryDto createGermplasm(final int entryNo) {
		final StudyEntryDto germplasm = new StudyEntryDto();
		germplasm.setEntryNumber(entryNo);
		germplasm.setDesignation("DESIG" + entryNo);

		final Map<Integer, StudyEntryPropertyData> properties = new HashMap<>();
		properties.put(TermId.ENTRY_TYPE.getId(), new StudyEntryPropertyData(null, TermId.ENTRY_TYPE.getId(), null, SystemDefinedEntryType.TEST_ENTRY.getEntryTypeCategoricalId()));
		properties.put(TermId.CROSS.getId(), new StudyEntryPropertyData(null, TermId.CROSS.getId(), "", null));
		properties.put(TermId.ENTRY_CODE.getId(), new StudyEntryPropertyData(null, TermId.ENTRY_CODE.getId(), String.valueOf(entryNo), null));
		germplasm.setProperties(properties);

		return germplasm;
	}
}
