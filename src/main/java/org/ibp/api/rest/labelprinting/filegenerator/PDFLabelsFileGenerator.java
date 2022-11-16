package org.ibp.api.rest.labelprinting.filegenerator;

import com.google.common.collect.Maps;
import com.google.common.io.Files;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.oned.Code128Writer;
import com.lowagie.text.BadElementException;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.Image;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import org.apache.commons.lang3.StringUtils;
import org.generationcp.commons.util.FileUtils;
import org.ibp.api.rest.common.FileType;
import org.ibp.api.rest.labelprinting.domain.Field;
import org.ibp.api.rest.labelprinting.domain.LabelsData;
import org.ibp.api.rest.labelprinting.domain.LabelsGeneratorInput;
import org.ibp.api.rest.labelprinting.template.LabelPaper;
import org.ibp.api.rest.labelprinting.template.LabelPaperFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.awt.*;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class PDFLabelsFileGenerator implements LabelsFileGenerator {

	private static final Logger LOG = LoggerFactory.getLogger(PDFLabelsFileGenerator.class);
	private static final String UNSUPPORTED_CHARSET_IMG = "unsupported-char-set.png";
	static final String ARIAL_UNI = "arialuni.ttf";
	static final float COLUMN_WIDTH_SIZE = 265f;
	private static final int WIDTH = 600;
	private static final int HEIGHT = 75;
	public static final String FIELDNAME_VALUE_SEPARATOR = " : ";
	public static final String BARCODE_SEPARATOR = " | ";
	public static final int BARCODE_LABEL_LIMIT = 79;
	public static final String UNDERSCORE = "_";

	@Autowired
	private LabelPaperFactory labelPaperFactory;

	@Override
	public File generate(final LabelsGeneratorInput labelsGeneratorInput, final LabelsData labelsData) throws IOException {
		final File temporaryFolder = Files.createTempDir();
		final String sanitizedFileName = FileUtils.sanitizeFileName(String.format("%s." + FileType.PDF.getExtension(), labelsGeneratorInput.getFileName()));

		final String fileNameFullPath = temporaryFolder.getAbsolutePath() + File.separator + sanitizedFileName;
		final Map<String, Field> keyFieldMap = Maps.uniqueIndex(labelsGeneratorInput.getAllAvailablefields(), field -> field.getFieldType().getName() + CSVLabelsFileGenerator.UNDERSCORE + field.getId());

		final int pageSizeId = Integer.parseInt(labelsGeneratorInput.getSizeOfLabelSheet());
		final int numberOfLabelPerRow = LabelsGeneratorInput.LABEL_PER_ROW;
		final int numberOfRowsPerPageOfLabel = Integer.parseInt(labelsGeneratorInput.getNumberOfRowsPerPageOfLabel());
		final int totalPerPage = numberOfLabelPerRow * numberOfRowsPerPageOfLabel;
		final FileOutputStream fileOutputStream = new FileOutputStream(fileNameFullPath);

		final File file = new File(fileNameFullPath);
		try {
			final LabelPaper paper = this.labelPaperFactory.generateLabelPaper(numberOfLabelPerRow, numberOfRowsPerPageOfLabel, pageSizeId);
			final Document document = this.getDocument(fileOutputStream, paper, pageSizeId);

			int i = 0;
			final int fixTableRowSize = numberOfLabelPerRow;
			final float[] widthColumns = this.getWidthColumns(fixTableRowSize);

			PdfPTable table = new PdfPTable(fixTableRowSize);
			table.setWidths(widthColumns);
			table.setWidthPercentage(100);

			final List<File> filesToBeDeleted = new ArrayList<>();
			final float cellHeight = paper.getCellHeight();
			final List<Map<String, String>> data = labelsData.getData();
			for(final Map<String, String> labels: data) {
				i++;
				String barcodeLabelForCode = "";
				String barcodeLabel = "";

				if (labelsGeneratorInput.isBarcodeRequired()) {
					if (!labelsGeneratorInput.isAutomaticBarcode()) {
						barcodeLabel = this.getBarcodeLabel(labels, labelsGeneratorInput.getBarcodeFields(), keyFieldMap, false);
						barcodeLabelForCode = this.getBarcodeLabel(labels, labelsGeneratorInput.getBarcodeFields(), keyFieldMap, true);
					} else {
						barcodeLabel = labels.get(labelsData.getDefaultBarcodeKey());
						barcodeLabelForCode = barcodeLabel;
					}
				}
				barcodeLabelForCode = this.truncateBarcodeLabelForCode(barcodeLabelForCode);

				final PdfPCell cell = new PdfPCell();
				cell.setFixedHeight(cellHeight);
				cell.setNoWrap(false);
				cell.setPadding(5f);
				cell.setPaddingBottom(1f);

				final PdfPTable innerImageTableInfo = new PdfPTable(1);
				innerImageTableInfo.setWidths(new float[] {1});
				innerImageTableInfo.setWidthPercentage(82);
				final PdfPCell cellImage = new PdfPCell();
				if (labelsGeneratorInput.isBarcodeRequired()) {
					final Image mainImage = this.getBarcodeImage(filesToBeDeleted, barcodeLabelForCode);
					cellImage.addElement(mainImage);
				} else {
					cellImage.addElement(new Paragraph(" "));
				}
				cellImage.setBorder(Rectangle.NO_BORDER);
				cellImage.setBackgroundColor(Color.white);
				cellImage.setPadding(1.5f);

				innerImageTableInfo.addCell(cellImage);

				final float fontSize = paper.getFontSize();

				final BaseFont unicode = BaseFont.createFont(ARIAL_UNI, BaseFont.IDENTITY_H, BaseFont
					.EMBEDDED);
				final com.lowagie.text.Font fontNormal = new com.lowagie.text.Font(unicode, fontSize);
				fontNormal.setStyle(com.lowagie.text.Font.NORMAL);

				cell.addElement(innerImageTableInfo);
				cell.addElement(new Paragraph());
				for (int row = 0; row < 5; row++) {
					if (row == 0) {
						final PdfPTable innerDataTableInfo = new PdfPTable(1);
						innerDataTableInfo.setWidths(new float[] {1});
						innerDataTableInfo.setWidthPercentage(85);

						final com.lowagie.text.Font fontNormalData = new com.lowagie.text.Font(unicode, 5.0f);
						fontNormal.setStyle(Font.NORMAL);

						final PdfPCell cellInnerData = new PdfPCell(new Phrase(barcodeLabel, fontNormalData));

						cellInnerData.setBorder(Rectangle.NO_BORDER);
						cellInnerData.setBackgroundColor(Color.white);
						cellInnerData.setPaddingBottom(0.2f);
						cellInnerData.setPaddingTop(0.2f);
						cellInnerData.setHorizontalAlignment(Element.ALIGN_MIDDLE);

						innerDataTableInfo.addCell(cellInnerData);
						innerDataTableInfo.setHorizontalAlignment(Element.ALIGN_MIDDLE);
						cell.addElement(innerDataTableInfo);
					}
					final PdfPTable innerTableInfo = new PdfPTable(2);
					innerTableInfo.setWidths(new float[] {1, 1});
					innerTableInfo.setWidthPercentage(85);
					final List<String> leftSelectedFieldIDs = labelsGeneratorInput.getFields().get(0);
					final String leftText = this.generateLabelText(labels, leftSelectedFieldIDs,
						keyFieldMap, row);
					final PdfPCell cellInnerLeft = new PdfPCell(new Paragraph(leftText, fontNormal));

					cellInnerLeft.setBorder(Rectangle.NO_BORDER);
					cellInnerLeft.setBackgroundColor(Color.white);
					cellInnerLeft.setPaddingBottom(0.5f);
					cellInnerLeft.setPaddingTop(0.5f);

					innerTableInfo.addCell(cellInnerLeft);

					final List<String> rightSelectedFieldIDs = labelsGeneratorInput.getFields().get(1);
					final String rightText = this.generateLabelText(labels, rightSelectedFieldIDs,
						keyFieldMap, row);
					final PdfPCell cellInnerRight = new PdfPCell(new Paragraph(rightText, fontNormal));

					cellInnerRight.setBorder(Rectangle.NO_BORDER);
					cellInnerRight.setBackgroundColor(Color.white);
					cellInnerRight.setPaddingBottom(0.5f);
					cellInnerRight.setPaddingTop(0.5f);

					innerTableInfo.addCell(cellInnerRight);

					cell.addElement(innerTableInfo);
				}
				cell.setBorder(Rectangle.NO_BORDER);
				cell.setBackgroundColor(Color.white);

				table.addCell(cell);

				if (i % numberOfLabelPerRow == 0) {
					// we go the next line
					final int needed = fixTableRowSize - numberOfLabelPerRow;

					for (int neededCount = 0; neededCount < needed; neededCount++) {
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
				if (i % totalPerPage == 0) {
					// we go the next page
					document.newPage();
				}
			}
			// we need to add the last row
			this.addLastRow(numberOfLabelPerRow, numberOfRowsPerPageOfLabel, paper, document, i, fixTableRowSize,
				table, widthColumns);
			document.close();
			for (final File fileTobeDeleted : filesToBeDeleted) {
				fileTobeDeleted.delete();
			}
			fileOutputStream.close();
		} catch (final Exception e) {
			LOG.error(e.getMessage());
		}

		return file;
	}

	String getBarcodeLabel(final Map<String, String> labels, final List<String> barcodeFields, final Map<String, Field> keyFieldMap, final boolean includeLabel) {
		final StringBuilder barcode = new StringBuilder();
		for (final String barcodeField : barcodeFields) {
			if (!StringUtils.isEmpty(barcode.toString())) {
				barcode.append(BARCODE_SEPARATOR);
			}

			if(includeLabel) {
				barcode.append(keyFieldMap.get(barcodeField).getName() + FIELDNAME_VALUE_SEPARATOR);
			}
			barcode.append(labels.get(barcodeField));
		}
		return barcode.toString();
	}

	protected String generateLabelText(final Map<String, String> labels, final List<String> selectedFields, final Map<String, Field> keyFieldMap, final int row) {
		if(row < selectedFields.size()) {
			final String value = labels.get(selectedFields.get(row)) != null ? labels.get(selectedFields.get(row)) : StringUtils.EMPTY;
			return keyFieldMap.get(selectedFields.get(row)).getName() + FIELDNAME_VALUE_SEPARATOR + value;
		}
		return "";
	}

	/**
	 * Truncate the barcode label for code instead of throwing an error in pdf
	 * @param barcodeLabelForCode barcode label to truncate
	 * @return truncated barcode label
	 */
	String truncateBarcodeLabelForCode(final String barcodeLabelForCode) {
		if (barcodeLabelForCode != null && barcodeLabelForCode.length() > BARCODE_LABEL_LIMIT) {
			return barcodeLabelForCode.substring(0, BARCODE_LABEL_LIMIT);
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

	float[] getWidthColumns(final int fixTableRowSize) {
		final float[] widthColumns = new float[fixTableRowSize];

		for (int counter = 0; counter < widthColumns.length; counter++) {
			widthColumns[counter] = COLUMN_WIDTH_SIZE;
		}
		return widthColumns;
	}

	com.lowagie.text.Image getBarcodeImage(final java.util.List<File> filesToBeDeleted, final String barcodeLabelForCode)
		throws BadElementException, IOException {
		FileOutputStream fout = null;

		com.lowagie.text.Image mainImage = com.lowagie.text.Image.getInstance(PDFLabelsFileGenerator.class.getClassLoader().getResource(UNSUPPORTED_CHARSET_IMG));

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
