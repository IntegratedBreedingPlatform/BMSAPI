
package org.ibp.api.java.impl.middleware.study.conversion;

import org.generationcp.middleware.domain.dms.PhenotypicType;
import org.generationcp.middleware.domain.etl.MeasurementVariable;
import org.generationcp.middleware.domain.etl.Workbook;
import org.generationcp.middleware.domain.oms.StudyType;
import org.generationcp.middleware.domain.oms.TermId;
import org.ibp.api.domain.germplasm.GermplasmListEntrySummary;
import org.ibp.api.domain.study.MeasurementImportDTO;
import org.ibp.api.domain.study.ObservationImportDTO;
import org.ibp.api.domain.study.StudyGermplasm;
import org.ibp.api.domain.study.StudyImportDTO;
import org.ibp.api.domain.study.Trait;
import org.junit.Assert;
import org.junit.Test;

import com.google.common.collect.Lists;

public class WorkbookConverterTest {

	@Test
	public void testConvertNursery() {

		final StudyImportDTO inputDTO = new StudyImportDTO();
		inputDTO.setStudyType(StudyType.N.getName());
		inputDTO.setName("Maize Nursery");
		inputDTO.setObjective("Grow more seeds.");
		inputDTO.setStartDate("20150101");
		inputDTO.setEndDate("20151201");
		inputDTO.setUserId(1);
		inputDTO.setFolderId(1L);
		inputDTO.setSiteName("Mexico");
		inputDTO.setStudyInstitute("CIMMYT");
		inputDTO.setDescription("Maize Nursery title.");

		final Trait trait1 = new Trait(1, "Plant Height");
		final Trait trait2 = new Trait(2, "Grain Yield");
		inputDTO.setTraits(Lists.newArrayList(trait1, trait2));

		final StudyGermplasm g1 = new StudyGermplasm();
		g1.setEntryNumber(1);
		final GermplasmListEntrySummary g1Summary = new GermplasmListEntrySummary();
		g1Summary.setGid(1);
		g1Summary.setEntryCode("Entry Code 1");
		g1Summary.setSeedSource("Seed Source 1");
		g1Summary.setDesignation("Designation 1");
		g1Summary.setCross("Cross 1");
		g1.setGermplasmListEntrySummary(g1Summary);

		final StudyGermplasm g2 = new StudyGermplasm();
		g2.setEntryNumber(2);
		final GermplasmListEntrySummary g2Summary = new GermplasmListEntrySummary();
		g2Summary.setGid(2);
		g2Summary.setEntryCode("Entry Code 2");
		g2Summary.setSeedSource("Seed Source 2");
		g2Summary.setDesignation("Designation 2");
		g2Summary.setCross("Cross 2");
		g2.setGermplasmListEntrySummary(g2Summary);

		inputDTO.setGermplasm(Lists.newArrayList(g1, g2));

		final ObservationImportDTO observationUnit1 = new ObservationImportDTO();
		observationUnit1.setGid(g1.getGermplasmListEntrySummary().getGid());

		final MeasurementImportDTO measurement11 = new MeasurementImportDTO();
		measurement11.setTraitId(trait1.getTraitId());
		measurement11.setTraitValue("11");

		final MeasurementImportDTO measurement12 = new MeasurementImportDTO();
		measurement12.setTraitId(trait2.getTraitId());
		measurement12.setTraitValue("12");

		observationUnit1.setMeasurements(Lists.newArrayList(measurement11, measurement12));

		final ObservationImportDTO observationUnit2 = new ObservationImportDTO();
		observationUnit2.setGid(g2.getGermplasmListEntrySummary().getGid());

		final MeasurementImportDTO measurement21 = new MeasurementImportDTO();
		measurement21.setTraitId(trait1.getTraitId());
		measurement21.setTraitValue("21");

		final MeasurementImportDTO measurement22 = new MeasurementImportDTO();
		measurement22.setTraitId(trait2.getTraitId());
		measurement22.setTraitValue("22");

		observationUnit2.setMeasurements(Lists.newArrayList(measurement21, measurement22));

		inputDTO.setObservations(Lists.newArrayList(observationUnit1, observationUnit2));

		final WorkbookConverter converter = new WorkbookConverter();
		// Better to use actual component for MeasurementVariableConverter rather than mocking it as it is a simple collaborator.
		converter.setMeasurementVariableConverter(new MeasurementVariableConverter());

		final Workbook outputWorkbook = converter.convert(inputDTO);

		// StudyDetail mapping
		Assert.assertEquals(StudyType.N, outputWorkbook.getStudyDetails().getStudyType());
		Assert.assertEquals(inputDTO.getName(), outputWorkbook.getStudyDetails().getStudyName());
		Assert.assertEquals(inputDTO.getObjective(), outputWorkbook.getStudyDetails().getObjective());
		Assert.assertEquals(inputDTO.getDescription(), outputWorkbook.getStudyDetails().getDescription());
		Assert.assertEquals(inputDTO.getStartDate(), outputWorkbook.getStudyDetails().getStartDate());
		Assert.assertEquals(inputDTO.getEndDate(), outputWorkbook.getStudyDetails().getEndDate());
		Assert.assertEquals(inputDTO.getSiteName(), outputWorkbook.getStudyDetails().getSiteName());
		Assert.assertEquals(inputDTO.getFolderId(), new Long(outputWorkbook.getStudyDetails().getParentFolderId()));

		// Basic details as MeasurementVariables
		Assert.assertEquals(6, outputWorkbook.getConditions().size());
		Assert.assertEquals(inputDTO.getName(), outputWorkbook.getStudyName());

		final MeasurementVariable mvStudyInstitute = outputWorkbook.getConditions().get(5);
		Assert.assertEquals(inputDTO.getStudyInstitute(), mvStudyInstitute.getValue());
		Assert.assertEquals(PhenotypicType.STUDY, mvStudyInstitute.getRole());
		Assert.assertTrue(mvStudyInstitute.isFactor());
		Assert.assertEquals(TermId.STUDY_INSTITUTE.getId(), mvStudyInstitute.getTermId());

		// Constants - none for Nurseries.
		Assert.assertNotNull(outputWorkbook.getConstants());
		Assert.assertEquals(0, outputWorkbook.getConstants().size());

		// Factors
		Assert.assertEquals(5, outputWorkbook.getFactors().size());
		final MeasurementVariable mvEntryNumber = outputWorkbook.getFactors().get(0);
		Assert.assertEquals(PhenotypicType.GERMPLASM, mvEntryNumber.getRole());
		Assert.assertTrue(mvEntryNumber.isFactor());
		Assert.assertEquals(TermId.ENTRY_NO.getId(), mvEntryNumber.getTermId());

		final MeasurementVariable mvDesignation = outputWorkbook.getFactors().get(1);
		Assert.assertEquals(PhenotypicType.GERMPLASM, mvDesignation.getRole());
		Assert.assertTrue(mvDesignation.isFactor());
		Assert.assertEquals(TermId.DESIG.getId(), mvDesignation.getTermId());

		final MeasurementVariable mvCross = outputWorkbook.getFactors().get(2);
		Assert.assertEquals(PhenotypicType.GERMPLASM, mvCross.getRole());
		Assert.assertTrue(mvCross.isFactor());
		Assert.assertEquals(TermId.CROSS.getId(), mvCross.getTermId());

		final MeasurementVariable mvGid = outputWorkbook.getFactors().get(3);
		Assert.assertEquals(PhenotypicType.GERMPLASM, mvGid.getRole());
		Assert.assertTrue(mvGid.isFactor());
		Assert.assertEquals(TermId.GID.getId(), mvGid.getTermId());

		final MeasurementVariable mvPlotNo = outputWorkbook.getFactors().get(4);
		Assert.assertEquals(PhenotypicType.TRIAL_DESIGN, mvPlotNo.getRole());
		Assert.assertTrue(mvPlotNo.isFactor());
		Assert.assertEquals(TermId.PLOT_NO.getId(), mvPlotNo.getTermId());

		// Variates
		Assert.assertEquals(inputDTO.getTraits().size(), outputWorkbook.getVariates().size());

		// Observations
		Assert.assertEquals(inputDTO.getGermplasm().size(), outputWorkbook.getObservations().size());
	}
}
