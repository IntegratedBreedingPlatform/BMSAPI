package org.ibp.api.java.impl.middleware.dataset;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.generationcp.middleware.domain.dms.DatasetDTO;
import org.generationcp.middleware.domain.etl.MeasurementVariable;
import org.generationcp.middleware.domain.genotype.SampleGenotypeDTO;
import org.generationcp.middleware.domain.oms.TermId;
import org.generationcp.middleware.service.impl.study.StudyInstance;
import org.ibp.api.Util;
import org.ibp.api.rest.dataset.ObservationUnitData;
import org.ibp.api.rest.dataset.ObservationUnitRow;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class DatasetTransposedExcelGenerator extends DatasetExcelGenerator {

    @Override
    public File generateSingleInstanceFile(
            final Integer studyId,
            final DatasetDTO dataSetDto, final List<MeasurementVariable> columns,
            final List<ObservationUnitRow> reorderedObservationUnitRows,
            final Map<Integer, List<SampleGenotypeDTO>> genotypeDTORowMap,
            final String fileNamePath, final StudyInstance studyInstance) throws IOException {
        final HSSFWorkbook xlsBook = new HSSFWorkbook();

        final List<MeasurementVariable> orderedColumns = this.orderColumns(columns, TermId.TRIAL_INSTANCE_FACTOR.getId());
        this.writeDescriptionSheet(xlsBook, studyId, dataSetDto, studyInstance, false);
        final Locale locale = LocaleContextHolder.getLocale();
        this.writeObservationSheet(
                orderedColumns, reorderedObservationUnitRows, genotypeDTORowMap, xlsBook,
                this.messageSource.getMessage("export.study.sheet.observation", null, locale));

        final File file = new File(fileNamePath);

        try (final FileOutputStream fos = new FileOutputStream(file)) {
            xlsBook.write(fos);

        }
        return file;
    }

    @Override
    public File generateMultiInstanceFile(final Integer studyId, final DatasetDTO datasetDTO,
                                          final Map<Integer, List<ObservationUnitRow>> observationUnitRowMap,
                                          final Map<Integer, List<SampleGenotypeDTO>> genotypeDTORowMap,
                                          final List<MeasurementVariable> columns,
                                          final String fileNameFullPath) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    void writeObservationSheet(
            final List<MeasurementVariable> columns, final List<ObservationUnitRow> reorderedObservationUnitRows,
            final Map<Integer, List<SampleGenotypeDTO>> genotypeDTORowMap,
            final HSSFWorkbook xlsBook, final String sheetName) {
        final HSSFSheet xlsSheet = xlsBook.createSheet(sheetName);
        final int numberOfSubObsPerPlot = this.getNumberOfSubObsPerPlot(columns, reorderedObservationUnitRows);
        this.writeObservationHeader(xlsBook, xlsSheet, columns, numberOfSubObsPerPlot);
        int currentRowNum = 2;

        // <Plot number, <OBSERVATION_UNIT number, ObservationUnitRow>>
        final Map<String, Map<String, ObservationUnitRow>> observationUnitRowMap = this.createObservationUnitRowMap(columns, reorderedObservationUnitRows);
        final List<MeasurementVariable> factorColumns = columns.stream().filter(var -> var.isFactor()).collect(Collectors.toList());
        final List<MeasurementVariable> nonFactorColumns = columns.stream().filter(var -> !var.isFactor()).collect(Collectors.toList());
        int nonFactorColumnStart = 0;
        final int numberOfNonFactorColumns = nonFactorColumns.size();

        // Create cell styles with different borders and use them whenever necessary
        final CellStyle leftBorderedCellStyle = this.createCellStyle(xlsSheet.getWorkbook().createCellStyle(), true, false, false);
        final CellStyle rightBorderedCellStyle = this.createCellStyle(xlsSheet.getWorkbook().createCellStyle(), false, true, false);
        final CellStyle borderedCellStyle = this.createCellStyle(xlsSheet.getWorkbook().createCellStyle(), true, true, false);

        for(final String plotNumber: observationUnitRowMap.keySet()) {
            final HSSFRow row = xlsSheet.createRow(currentRowNum++);
            // factors are the same for entries with same plot number, so we'll use the first one
            int currentColNum = this.writeFactorsValues(observationUnitRowMap.get(plotNumber).get("1"), row, factorColumns);
            nonFactorColumnStart = currentColNum;
            if (CollectionUtils.isNotEmpty(nonFactorColumns)) {
                for (int i = 1; i <= numberOfSubObsPerPlot; i++) {
                    final ObservationUnitRow dataRow = observationUnitRowMap.get(plotNumber).get(String.valueOf(i));
                    final List<SampleGenotypeDTO> sampleGenotypeDtoList =
                            genotypeDTORowMap.getOrDefault(dataRow.getObservationUnitId(), new ArrayList<>());
                    for (int j = 0; j < numberOfNonFactorColumns; j++){
                        final MeasurementVariable column = nonFactorColumns.get(j);
                        final ObservationUnitData observationUnitData = Util.getObservationUnitData(dataRow.getVariables(), column);
                        currentColNum = this.writeObservationUnitDataCell(row, currentColNum, column, observationUnitData);
                        currentColNum = this.writeSampleGenotypeDataCell(sampleGenotypeDtoList, row, currentColNum, column);
                        if (numberOfNonFactorColumns > 1) {
                            if (j == 0) {
                                row.getCell(currentColNum - 1).setCellStyle(leftBorderedCellStyle);
                            }
                            if (j == (numberOfNonFactorColumns - 1)) {
                                row.getCell(currentColNum - 1).setCellStyle(rightBorderedCellStyle);
                            }
                        } else {
                            // Add borders to both sides if there's only 1 nonfactor column
                            row.getCell(currentColNum - 1).setCellStyle(borderedCellStyle);
                        }
                    }
                }
            }
        }

        this.addLastRowBottomBorder(xlsSheet, numberOfSubObsPerPlot, nonFactorColumnStart, numberOfNonFactorColumns);
    }

    private void addLastRowBottomBorder(final HSSFSheet xlsSheet, final int numberOfSubObsPerPlot, final int nonFactorColumnStart, final int numberOfNonFactorColumns) {
        final HSSFRow lastRow = xlsSheet.getRow(xlsSheet.getLastRowNum());
        final int totalNumberOfNonFactorColumns = numberOfNonFactorColumns * numberOfSubObsPerPlot;

        // Create cell styles with different borders and use them whenever necessary
        final CellStyle leftBorderedCellStyle = this.createCellStyle(xlsSheet.getWorkbook().createCellStyle(), true, false, true);
        final CellStyle rightBorderedCellStyle = this.createCellStyle(xlsSheet.getWorkbook().createCellStyle(), false, true, true);
        final CellStyle bottomBorderedCell = this.createCellStyle(xlsSheet.getWorkbook().createCellStyle(), false, false, true);
        final CellStyle borderedCellStyle = this.createCellStyle(xlsSheet.getWorkbook().createCellStyle(), true, true, true);

        for (int i = 0; i < totalNumberOfNonFactorColumns; i++) {
            final HSSFCell cell = lastRow.getCell(nonFactorColumnStart + i);
            if (cell.getCellStyle().getBorderLeft().equals(BorderStyle.THIN) && cell.getCellStyle().getBorderRight().equals(BorderStyle.THIN)) {
                cell.setCellStyle(borderedCellStyle);
            } else if (cell.getCellStyle().getBorderLeft().equals(BorderStyle.THIN)) {
                cell.setCellStyle(leftBorderedCellStyle);
            } else if (cell.getCellStyle().getBorderRight().equals(BorderStyle.THIN)) {
                cell.setCellStyle(rightBorderedCellStyle);
            } else {
                cell.setCellStyle(bottomBorderedCell);
            }
        }
    }

    private int writeFactorsValues(final ObservationUnitRow dataRow, final HSSFRow row, final List<MeasurementVariable> factorColumns) {
        int currentColNum = 0;
        for (final MeasurementVariable column: factorColumns) {
            if (column.getVariableType().getId() != TermId.OBSERVATION_UNIT.getId()) {
                final ObservationUnitData observationUnitData = Util.getObservationUnitData(dataRow.getVariables(), column);
                currentColNum = this.writeObservationUnitDataCell(row, currentColNum, column, observationUnitData);
            } else {
                // OBSERVATION_UNIT variables(PLANT_NO, DATE_NO, etc.) should be left blank
                final HSSFCell cell = row.createCell(currentColNum++);
                cell.setCellType(CellType.BLANK);
            }
        }
        return currentColNum;
    }

    private Map<String, Map<String, ObservationUnitRow>> createObservationUnitRowMap(final List<MeasurementVariable> columns, final List<ObservationUnitRow> reorderedObservationUnitRows) {
        final Map<String, Map<String, ObservationUnitRow>> observationUnitRowMap = new LinkedHashMap<>();
        if (CollectionUtils.isNotEmpty(reorderedObservationUnitRows)) {
            final Optional<MeasurementVariable> observationUnitVariable = getObservationUnitVariable(columns);
            final Optional<MeasurementVariable> plotNoVariable = columns.stream().filter(variable -> variable.getTermId() == TermId.PLOT_NO.getId())
                    .findFirst();
            if (observationUnitVariable.isPresent() && plotNoVariable.isPresent()) {
                final String obsUnitVariableKey = reorderedObservationUnitRows.get(0).getVariables().containsKey(observationUnitVariable.get().getName()) ?
                        observationUnitVariable.get().getName() : observationUnitVariable.get().getAlias();
                final String plotNoVariableKey = reorderedObservationUnitRows.get(0).getVariables().containsKey(plotNoVariable.get().getName()) ?
                        plotNoVariable.get().getName() : plotNoVariable.get().getAlias();
                for (final ObservationUnitRow dataRow : reorderedObservationUnitRows) {
                    final String plotNo = dataRow.getVariables().get(plotNoVariableKey).getValue();
                    // group Observation Unit Rows by PLOT_NO
                    observationUnitRowMap.putIfAbsent(plotNo, new HashMap<>());
                    observationUnitRowMap.get(plotNo).putIfAbsent(dataRow.getVariables().get(obsUnitVariableKey).getValue(), dataRow);
                }
            }
        }
        return observationUnitRowMap;
    }

    private int getNumberOfSubObsPerPlot(final List<MeasurementVariable> columns, final List<ObservationUnitRow> reorderedObservationUnitRows) {
        int numberOfSubObsPerPlot = 0;
        if (CollectionUtils.isNotEmpty(reorderedObservationUnitRows)) {
            final Optional<MeasurementVariable> observationUnitVariable = getObservationUnitVariable(columns);
            if (observationUnitVariable.isPresent()) {
                final String observationUnitVariableKey = reorderedObservationUnitRows.get(0).getVariables().containsKey(observationUnitVariable.get().getName()) ?
                        observationUnitVariable.get().getName() : observationUnitVariable.get().getAlias();
                // return the highest ObservationUnit Variable value as the number of sub-observations per plot since the value is not saved in the database during creation
                numberOfSubObsPerPlot = reorderedObservationUnitRows.stream()
                        .mapToInt(observationUnitRow -> Integer.valueOf(observationUnitRow.getVariables().get(observationUnitVariableKey).getValue())).max().orElse(0);
            }
        }
        return numberOfSubObsPerPlot;
    }

    private static Optional<MeasurementVariable> getObservationUnitVariable(final List<MeasurementVariable> columns) {
        return columns.stream().filter(variable -> variable.getVariableType().getId() == TermId.OBSERVATION_UNIT.getId())
                .findFirst();
    }

    private void writeObservationHeader(
            final HSSFWorkbook xlsBook, final HSSFSheet xlsSheet,
            final List<MeasurementVariable> variables, final int numberOfSubObsPerPlot) {
        if (variables != null && !variables.isEmpty()) {
            int currentColNum = 0;
            final HSSFRow row = xlsSheet.createRow(0);

            // Merge the first two rows for factor variables
            for (final MeasurementVariable variable : variables) {
                if (variable.isFactor()) {
                    final HSSFCell cell = row.createCell(currentColNum);
                    cell.setCellStyle(this.getObservationHeaderStyle(true, xlsBook));
                    cell.setCellValue(StringUtils.isNotEmpty(variable.getAlias()) ? variable.getAlias() : variable.getName());
                    xlsSheet.addMergedRegion(new CellRangeAddress(0, 1, currentColNum, currentColNum));
                    if (variable.getVariableType().getId() == TermId.OBSERVATION_UNIT.getId()) {
                        cell.getCellStyle().setVerticalAlignment(VerticalAlignment.TOP);
                    } else {
                        cell.getCellStyle().setVerticalAlignment(VerticalAlignment.CENTER);
                    }
                    currentColNum++;
                }
            }

            final List<MeasurementVariable> nonFactorVariables = variables.stream().filter(var -> !var.isFactor()).collect(Collectors.toList());
            if (CollectionUtils.isNotEmpty(nonFactorVariables)) {
                final int numberOfNonFactors = nonFactorVariables.size();
                final HSSFRow nonFactorsRow = xlsSheet.createRow(1);
                for (int i = 1; i<= numberOfSubObsPerPlot; i++) {
                    final HSSFCell cell = row.createCell(currentColNum);
                    cell.setCellStyle(this.getObservationHeaderStyle(true, xlsBook));
                    cell.getCellStyle().setAlignment(HorizontalAlignment.CENTER);
                    cell.setCellValue(String.valueOf(i));
                    //No need to merge cells if there's only 1 nonFactor variable
                    if (numberOfNonFactors > 1) {
                        // Subtract 1 since current col occupies the first cell
                        final int lastCol = currentColNum + numberOfNonFactors - 1;
                        xlsSheet.addMergedRegion(new CellRangeAddress(0, 0, currentColNum, lastCol));
                    }
                    cell.getCellStyle().setBorderLeft(BorderStyle.THIN);
                    cell.getCellStyle().setBorderRight(BorderStyle.THIN);
                    final int numberOfNonFactorVariables = nonFactorVariables.size();
                    for (int j = 0; j < numberOfNonFactorVariables; j++) {
                        final MeasurementVariable variable = nonFactorVariables.get(j);
                        final HSSFCell nonFactorsRowCell = nonFactorsRow.createCell(currentColNum++);
                        nonFactorsRowCell.setCellValue(StringUtils.isNotEmpty(variable.getAlias()) ? variable.getAlias() : variable.getName());
                        final CellStyle cellStyle = this.getObservationHeaderStyle(false, xlsBook);
                        // Set thin border for first and last cells
                        if (j ==  0) {
                            cellStyle.setBorderLeft(BorderStyle.THIN);
                        }
                        if (j == (numberOfNonFactorVariables - 1)) {
                            cellStyle.setBorderRight(BorderStyle.THIN);
                        }
                        nonFactorsRowCell.setCellStyle(cellStyle);
                    }
                }
            }
        }
    }

    private CellStyle createCellStyle(final CellStyle cellStyle, final boolean hasBorderLeft, final boolean hasBorderRight, final boolean hasBorderBottom) {
        if (hasBorderLeft) {
            cellStyle.setBorderLeft(BorderStyle.THIN);
        }
        if (hasBorderRight) {
            cellStyle.setBorderRight(BorderStyle.THIN);
        }
        if (hasBorderBottom) {
            cellStyle.setBorderBottom(BorderStyle.THIN);
        }
        return cellStyle;
    }
}
