
package org.ibp.api.rest.labelprinting.template;

import org.springframework.stereotype.Component;

@Component
public class LabelPaperFactory {
	public static int SIZE_OF_PAPER_A4 = 1;
	public static int SIZE_OF_PAPER_LETTER = 2;

	public LabelPaper generateLabelPaper(final int labelsPerRow, final int numberOfRowsPerPage, final int pageSize) {
		if (SIZE_OF_PAPER_LETTER == pageSize) {
			if (labelsPerRow == 3) {
				switch (numberOfRowsPerPage) {
					case 7:
						return LabelPaper.PAPER_3_BY_7_LETTER;
					case 8:
						return LabelPaper.PAPER_3_BY_8_LETTER;
					case 10:
						return LabelPaper.PAPER_3_BY_10_LETTER;
				}
			}
		} else if (SIZE_OF_PAPER_A4 == pageSize) {
			if (labelsPerRow == 3) {
				switch (numberOfRowsPerPage) {
					case 7:
						return LabelPaper.PAPER_3_BY_7_A4;
					case 8:
						return LabelPaper.PAPER_3_BY_8_A4;
					case 10:
						return LabelPaper.PAPER_3_BY_10_A4;
				}
			}
		}
		return LabelPaper.PAPER_3_BY_7_A4;
	}
}
