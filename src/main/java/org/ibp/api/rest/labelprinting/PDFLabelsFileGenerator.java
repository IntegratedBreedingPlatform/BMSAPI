package org.ibp.api.rest.labelprinting;

import com.google.common.collect.Maps;
import com.google.common.io.Files;
import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.Image;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import liquibase.util.StringUtils;
import org.generationcp.commons.util.FileUtils;
import org.ibp.api.rest.labelprinting.domain.Field;
import org.ibp.api.rest.labelprinting.domain.LabelsData;
import org.ibp.api.rest.labelprinting.domain.LabelsGeneratorInput;
import org.ibp.api.rest.labelprinting.template.LabelPaper;
import org.ibp.api.rest.labelprinting.template.LabelPaperFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.awt.*;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
public class PDFLabelsFileGenerator implements LabelsFileGenerator  {
	private static final Logger LOG = LoggerFactory.getLogger(PDFLabelsFileGenerator.class);

	@Resource
	private LabelPrintingPDFUtil labelPrintingPDFUtil;

	@Override
	public File generate(final LabelsGeneratorInput labelsGeneratorInput, final LabelsData labelsData) throws IOException {
		final File temporaryFolder = Files.createTempDir();
		final String sanitizedFileName = FileUtils.sanitizeFileName(String.format("%s." + "pdf", labelsGeneratorInput.getFileName()));

		final String fileNameFullPath = temporaryFolder.getAbsolutePath() + File.separator + sanitizedFileName;
		final Map<Integer, Field> keyFieldMap = Maps.uniqueIndex(labelsGeneratorInput.getAllAvailablefields(), Field::getId);

		final int pageSizeId = Integer.parseInt(labelsGeneratorInput.getSizeOfLabelSheet());
		final int numberOfLabelPerRow = LabelsGeneratorInput.LABEL_PER_ROW;
		final int numberOfRowsPerPageOfLabel = Integer.parseInt(labelsGeneratorInput.getNumberOfRowsPerPageOfLabel());
		final int totalPerPage = numberOfLabelPerRow * numberOfRowsPerPageOfLabel;
		final FileOutputStream fileOutputStream = new FileOutputStream(fileNameFullPath);

		File file = new File(fileNameFullPath);
		try {
			final LabelPaper paper = LabelPaperFactory.generateLabelPaper(numberOfLabelPerRow, numberOfRowsPerPageOfLabel, pageSizeId);
			final Document document = this.labelPrintingPDFUtil.getDocument(fileOutputStream, paper, pageSizeId);

			int i = 0;
			final int fixTableRowSize = numberOfLabelPerRow;
			final float[] widthColumns = this.labelPrintingPDFUtil.getWidthColumns(fixTableRowSize, LabelPrintingPDFUtil.COLUMN_WIDTH_SIZE);

			PdfPTable table = new PdfPTable(fixTableRowSize);
			table.setWidths(widthColumns);
			table.setWidthPercentage(100);

			final List<File> filesToBeDeleted = new ArrayList<>();
			final float cellHeight = paper.getCellHeight();
			final List<Map<Integer, String>> data = labelsData.getData();
			for(final Map<Integer, String> labels: data) {
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
				barcodeLabelForCode = this.labelPrintingPDFUtil.truncateBarcodeLabelForCode(barcodeLabelForCode);

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
					final Image mainImage = this.labelPrintingPDFUtil.getBarcodeImage(filesToBeDeleted, barcodeLabelForCode);
					cellImage.addElement(mainImage);
				} else {
					cellImage.addElement(new Paragraph(" "));
				}
				cellImage.setBorder(Rectangle.NO_BORDER);
				cellImage.setBackgroundColor(Color.white);
				cellImage.setPadding(1.5f);

				innerImageTableInfo.addCell(cellImage);

				final float fontSize = paper.getFontSize();

				final BaseFont unicode = BaseFont.createFont(LabelPrintingPDFUtil.ARIAL_UNI, BaseFont.IDENTITY_H, BaseFont
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
					final List<Integer> leftSelectedFieldIDs = labelsGeneratorInput.getFields().get(0);
					final String leftText = this.generateLabelText(labels, leftSelectedFieldIDs,
						keyFieldMap, row);
					final PdfPCell cellInnerLeft = new PdfPCell(new Paragraph(leftText, fontNormal));

					cellInnerLeft.setBorder(Rectangle.NO_BORDER);
					cellInnerLeft.setBackgroundColor(Color.white);
					cellInnerLeft.setPaddingBottom(0.5f);
					cellInnerLeft.setPaddingTop(0.5f);

					innerTableInfo.addCell(cellInnerLeft);

					final List<Integer> rightSelectedFieldIDs = labelsGeneratorInput.getFields().get(1);
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
			this.labelPrintingPDFUtil.addLastRow(numberOfLabelPerRow, numberOfRowsPerPageOfLabel, paper, document, i, fixTableRowSize,
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

	protected String getBarcodeLabel(final Map<Integer, String> labels, final List<Integer> barcodeFields, final Map<Integer, Field> keyFieldMap, final boolean includeLabel) {
		StringBuffer barcode = new StringBuffer();
		for (Integer barcodeField : barcodeFields) {
			if (StringUtils.isEmpty(barcode.toString())) {
				if(includeLabel) {
					barcode.append(keyFieldMap.get(barcodeField).getName() + " : ");
				}
				barcode.append(labels.get(barcodeField));
				continue;
			}
			barcode.append(" | ");
			if(includeLabel) {
				barcode.append(keyFieldMap.get(barcodeField).getName() + " : ");
			}
			barcode.append(labels.get(barcodeField));
		}
		return barcode.toString();
	}

	protected String generateLabelText(final Map<Integer, String> labels, final List<Integer> selectedFields, final Map<Integer, Field> keyFieldMap, final int row) {
		if(row < selectedFields.size()) {
			return keyFieldMap.get(selectedFields.get(row)).getName() + " : " + labels.get(selectedFields.get(row));
		}
		return "";
	}
}
