
package org.ibp.api.java.impl.middleware.study.conversion;

import org.generationcp.middleware.domain.dms.PhenotypicType;
import org.generationcp.middleware.domain.etl.MeasurementVariable;
import org.generationcp.middleware.domain.etl.Workbook;
import org.generationcp.middleware.domain.oms.StudyType;
import org.generationcp.middleware.domain.oms.TermId;
import org.ibp.api.domain.germplasm.GermplasmListEntrySummary;
import org.ibp.api.domain.study.StudyGermplasm;
import org.ibp.api.domain.study.StudyImportDTO;
import org.ibp.api.domain.study.Trait;
import org.junit.Assert;
import org.junit.Test;

import com.google.common.collect.Lists;

public class WorkbookConverterTest {

	@Test
	public void testConvert() {

		final StudyImportDTO inputDTO = new StudyImportDTO();
		inputDTO.setStudyType("N");
		inputDTO.setName("Maize Nursery");
		inputDTO.setObjective("Grow more seeds.");
		inputDTO.setTitle("Maize Nursery title.");
		inputDTO.setStartDate("20150101");
		inputDTO.setEndDate("20151201");
		inputDTO.setUserId(1);
		inputDTO.setFolderId(1L);
		inputDTO.setSiteName("CIMMYT");

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

		final WorkbookConverter converter = new WorkbookConverter();
		// Better to use actual component for MeasurementVariableConverter rather than mocking it as it is a simple collaborator.
		converter.setMeasurementVariableConverter(new MeasurementVariableConverter());

		final Workbook outputWorkbook = converter.convert(inputDTO);

		// StudyDetail mapping
		Assert.assertEquals(StudyType.N, outputWorkbook.getStudyDetails().getStudyType());
		Assert.assertEquals(inputDTO.getName(), outputWorkbook.getStudyDetails().getStudyName());
		Assert.assertEquals(inputDTO.getObjective(), outputWorkbook.getStudyDetails().getObjective());
		Assert.assertEquals(inputDTO.getTitle(), outputWorkbook.getStudyDetails().getTitle());
		Assert.assertEquals(inputDTO.getStartDate(), outputWorkbook.getStudyDetails().getStartDate());
		Assert.assertEquals(inputDTO.getEndDate(), outputWorkbook.getStudyDetails().getEndDate());
		Assert.assertEquals(inputDTO.getSiteName(), outputWorkbook.getStudyDetails().getSiteName());
		Assert.assertEquals(inputDTO.getFolderId(), new Long(outputWorkbook.getStudyDetails().getParentFolderId()));

		// Basic details as MeasurementVariables
		Assert.assertEquals(5, outputWorkbook.getConditions().size());

		final MeasurementVariable mvName = outputWorkbook.getConditions().get(0);
		Assert.assertEquals(inputDTO.getName(), mvName.getValue());
		Assert.assertEquals(PhenotypicType.STUDY, mvName.getRole());
		Assert.assertTrue(mvName.isFactor());
		Assert.assertEquals(TermId.STUDY_NAME.getId(), mvName.getTermId());

		final MeasurementVariable mvTitle = outputWorkbook.getConditions().get(1);
		Assert.assertEquals(inputDTO.getTitle(), mvTitle.getValue());
		Assert.assertEquals(PhenotypicType.STUDY, mvTitle.getRole());
		Assert.assertTrue(mvTitle.isFactor());
		Assert.assertEquals(TermId.STUDY_TITLE.getId(), mvTitle.getTermId());

		final MeasurementVariable mvStartDate = outputWorkbook.getConditions().get(2);
		Assert.assertEquals(inputDTO.getStartDate(), mvStartDate.getValue());
		Assert.assertEquals(PhenotypicType.STUDY, mvStartDate.getRole());
		Assert.assertTrue(mvStartDate.isFactor());
		Assert.assertEquals(TermId.START_DATE.getId(), mvStartDate.getTermId());

		final MeasurementVariable mvEndDate = outputWorkbook.getConditions().get(3);
		Assert.assertEquals(inputDTO.getEndDate(), mvEndDate.getValue());
		Assert.assertEquals(PhenotypicType.STUDY, mvEndDate.getRole());
		Assert.assertTrue(mvEndDate.isFactor());
		Assert.assertEquals(TermId.END_DATE.getId(), mvEndDate.getTermId());

		final MeasurementVariable mvObjective = outputWorkbook.getConditions().get(4);
		Assert.assertEquals(inputDTO.getObjective(), mvObjective.getValue());
		Assert.assertEquals(PhenotypicType.STUDY, mvObjective.getRole());
		Assert.assertTrue(mvObjective.isFactor());
		Assert.assertEquals(TermId.STUDY_OBJECTIVE.getId(), mvObjective.getTermId());

		// Trial instance as factor
		Assert.assertEquals(1, outputWorkbook.getConstants().size());
		final MeasurementVariable mvTrialInstance = outputWorkbook.getConstants().get(0);
		Assert.assertEquals(PhenotypicType.TRIAL_ENVIRONMENT, mvTrialInstance.getRole());
		Assert.assertTrue(mvTrialInstance.isFactor());
		Assert.assertEquals(TermId.TRIAL_INSTANCE_FACTOR.getId(), mvTrialInstance.getTermId());

		// Germplasm Factors
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
