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
		Assert.assertEquals(Paper3by7Letter.class, labelPaper.getClass());

		labelPaper = this.labelPaperFactory.generateLabelPaper(3, 8, LabelPaperFactory.SIZE_OF_PAPER_LETTER);
		Assert.assertEquals(Paper3by8Letter.class, labelPaper.getClass());

		labelPaper = this.labelPaperFactory.generateLabelPaper(3, 10, LabelPaperFactory.SIZE_OF_PAPER_LETTER);
		Assert.assertEquals(Paper3by10Letter.class, labelPaper.getClass());

		labelPaper = this.labelPaperFactory.generateLabelPaper(3, 7, LabelPaperFactory.SIZE_OF_PAPER_A4);
		Assert.assertEquals(Paper3by7A4.class, labelPaper.getClass());

		labelPaper = this.labelPaperFactory.generateLabelPaper(3, 8, LabelPaperFactory.SIZE_OF_PAPER_A4);
		Assert.assertEquals(Paper3by8A4.class, labelPaper.getClass());

		labelPaper = this.labelPaperFactory.generateLabelPaper(3, 10, LabelPaperFactory.SIZE_OF_PAPER_A4);
		Assert.assertEquals(Paper3by10A4.class, labelPaper.getClass());
	}

}
