package org.ibp.api.rest.labelprinting;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.oned.Code128Writer;
import com.lowagie.text.BadElementException;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.PageSize;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import org.ibp.api.rest.labelprinting.template.LabelPaper;
import org.ibp.api.rest.labelprinting.template.LabelPaperFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.awt.*;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

@Component
class LabelPrintingPDFUtil {

	private static final String UNSUPPORTED_CHARSET_IMG = "unsupported-char-set.png";
	static final String ARIAL_UNI = "arialuni.ttf";
	static final float COLUMN_WIDTH_SIZE = 265f;
	private static final int WIDTH = 600;
	private static final int HEIGHT = 75;

	private static final Logger LOG = LoggerFactory.getLogger(LabelPrintingPDFUtil.class);

	/**
	 * Truncate the barcode label for code instead of throwing an error in pdf
	 * @param barcodeLabelForCode barcode label to truncate
	 * @return truncated barcode label
	 */
	String truncateBarcodeLabelForCode(String barcodeLabelForCode) {
		if (barcodeLabelForCode != null && barcodeLabelForCode.length() > 79) {
			barcodeLabelForCode = barcodeLabelForCode.substring(0, 79);
		}
		return barcodeLabelForCode;
	}

	/**
	 * Encode barcode label for pdf pages
	 * @param barcodeLabelForCode barcode label to encode
	 * @return barcode image
	 */
	BitMatrix encodeBarcode(final String barcodeLabelForCode) {
		BitMatrix bitMatrix = null;
		try {
			bitMatrix = new Code128Writer().encode(barcodeLabelForCode, BarcodeFormat.CODE_128, WIDTH, HEIGHT, null);
		} catch (final WriterException | IllegalArgumentException e) {
			LOG.debug(e.getMessage(), e);
		}
		return bitMatrix;
	}

	void addLastRow(final int numberOfLabelPerRow, final int numberOfRowsPerPageOfLabel, final LabelPaper paper, final Document document, final int i,
			final int fixTableRowSize, PdfPTable table, final float[] widthColumns) throws DocumentException {
		if (i % numberOfLabelPerRow != 0) {
			// we go the next line

			final int remaining = numberOfLabelPerRow - i % numberOfLabelPerRow;
			for (int neededCount = 0; neededCount < remaining; neededCount++) {
				final PdfPCell cellNeeded = new PdfPCell();

				cellNeeded.setBorder(Rectangle.NO_BORDER);
				cellNeeded.setBackgroundColor(Color.white);

				table.addCell(cellNeeded);
			}

			table.completeRow();
			if (numberOfRowsPerPageOfLabel == 10) {

				table.setSpacingAfter(paper.getSpacingAfter());
			}

			document.add(table);

			table = new PdfPTable(fixTableRowSize);
			table.setWidths(widthColumns);
			table.setWidthPercentage(100);

		}
	}

	Document getDocument(final FileOutputStream fileOutputStream, final LabelPaper paper, final int pageSizeId) throws
			DocumentException {

		Rectangle pageSize = PageSize.LETTER;

		if (pageSizeId == LabelPaperFactory.SIZE_OF_PAPER_A4) {
			pageSize = PageSize.A4;
		}

		final Document document = new Document(pageSize);

		// float marginLeft, float marginRight, float marginTop, float marginBottom
		document.setMargins(paper.getMarginLeft(), paper.getMarginRight(), paper.getMarginTop(), paper.getMarginBottom());

		PdfWriter.getInstance(document, fileOutputStream);

		// step 3
		document.open();
		return document;
	}

	float[] getWidthColumns(final int fixTableRowSize, final float columnWidthSize) {
		final float[] widthColumns = new float[fixTableRowSize];

		for (int counter = 0; counter < widthColumns.length; counter++) {
			widthColumns[counter] = columnWidthSize;
		}
		return widthColumns;
	}

	com.lowagie.text.Image getBarcodeImage(final java.util.List<File> filesToBeDeleted, final String barcodeLabelForCode)
			throws BadElementException, IOException {
		FileOutputStream fout = null;

		com.lowagie.text.Image mainImage = com.lowagie.text.Image.getInstance(PDFLabelsFileGenerator.class.getClassLoader().getResource(LabelPrintingPDFUtil.UNSUPPORTED_CHARSET_IMG));

		final BitMatrix bitMatrix = this.encodeBarcode(barcodeLabelForCode);
		if (bitMatrix != null) {
			final String imageLocation = System.getProperty("user.home") + "/" + Math.random() + ".png";
			final File imageFile = new File(imageLocation);
			fout = new FileOutputStream(imageFile);
			MatrixToImageWriter.writeToStream(bitMatrix, "png", fout);
			filesToBeDeleted.add(imageFile);

			mainImage = com.lowagie.text.Image.getInstance(imageLocation);
		}

		if (fout != null) {
			fout.flush();
			fout.close();
		}
		return mainImage;
	}

}
