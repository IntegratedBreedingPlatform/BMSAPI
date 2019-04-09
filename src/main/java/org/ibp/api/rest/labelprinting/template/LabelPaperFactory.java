
package org.ibp.api.rest.labelprinting.template;

import org.springframework.stereotype.Component;

@Component
public class LabelPaperFactory {
	public static int SIZE_OF_PAPER_A4 = 1;
	public static int SIZE_OF_PAPER_LETTER = 2;

	public LabelPaper generateLabelPaper(int labelsPerRow, int numberOfRowsPerPage, int pageSize) {
		LabelPaper paper = new Paper3by7A4();
		if (SIZE_OF_PAPER_LETTER == pageSize) {
			if (labelsPerRow == 3) {
				switch (numberOfRowsPerPage) {
					case 7:
						paper = new Paper3by7Letter();
						break;
					case 8:
						paper = new Paper3by8Letter();
						break;
					case 10:
						paper = new Paper3by10Letter();
						break;
				}
			}
		} else if (SIZE_OF_PAPER_A4 == pageSize) {
			if (labelsPerRow == 3) {
				switch (numberOfRowsPerPage) {
					case 7:
						paper = new Paper3by7A4();
						break;
					case 8:
						paper = new Paper3by8A4();
						break;
					case 10:
						paper = new Paper3by10A4();
						break;
				}
			}
		}
		return paper;
	}
}
