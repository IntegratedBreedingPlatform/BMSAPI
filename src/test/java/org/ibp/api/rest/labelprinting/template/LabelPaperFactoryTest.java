package org.ibp.api.rest.labelprinting.template;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class LabelPaperFactoryTest {
	private LabelPaperFactory labelPaperFactory = new LabelPaperFactory();


	@Test
	public void testGenerateLabelPaper() {
		LabelPaper labelPaper = this.labelPaperFactory.generateLabelPaper(3, 7, LabelPaperFactory.SIZE_OF_PAPER_LETTER);
		Assert.assertEquals(LabelPaper.PAPER_3_BY_7_LETTER, labelPaper);

		labelPaper = this.labelPaperFactory.generateLabelPaper(3, 8, LabelPaperFactory.SIZE_OF_PAPER_LETTER);
		Assert.assertEquals(LabelPaper.PAPER_3_BY_8_LETTER, labelPaper);

		labelPaper = this.labelPaperFactory.generateLabelPaper(3, 10, LabelPaperFactory.SIZE_OF_PAPER_LETTER);
		Assert.assertEquals(LabelPaper.PAPER_3_BY_10_LETTER, labelPaper);

		labelPaper = this.labelPaperFactory.generateLabelPaper(3, 7, LabelPaperFactory.SIZE_OF_PAPER_A4);
		Assert.assertEquals(LabelPaper.PAPER_3_BY_7_A4, labelPaper);

		labelPaper = this.labelPaperFactory.generateLabelPaper(3, 8, LabelPaperFactory.SIZE_OF_PAPER_A4);
		Assert.assertEquals(LabelPaper.PAPER_3_BY_8_A4, labelPaper);

		labelPaper = this.labelPaperFactory.generateLabelPaper(3, 10, LabelPaperFactory.SIZE_OF_PAPER_A4);
		Assert.assertEquals(LabelPaper.PAPER_3_BY_10_A4, labelPaper);
	}

}
