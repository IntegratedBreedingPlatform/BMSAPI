package org.ibp.api.rest.labelprinting.domain;

import java.util.List;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.ibp.api.rest.common.FileType;
import org.pojomatic.Pojomatic;
import org.pojomatic.annotations.AutoProperty;

@AutoProperty
public class LabelsGeneratorInput extends LabelsInfoInput {

	public static int LABEL_PER_ROW = 3;

	@JsonIgnore
	private Set<Field> allAvailablefields;

	@JsonIgnore
	private FileType fileType;

	private List<List<String>> fields;

	private boolean barcodeRequired;

	private boolean automaticBarcode;

	private boolean includeHeadings;

	private List<String> barcodeFields;

	private String sizeOfLabelSheet;

	private String numberOfRowsPerPageOfLabel;

	private String fileName;

	public List<List<String>> getFields() {
		return this.fields;
	}

	public void setFields(final List<List<String>> fields) {
		this.fields = fields;
	}

	public boolean isBarcodeRequired() {
		return this.barcodeRequired;
	}

	public void setBarcodeRequired(final boolean barcodeRequired) {
		this.barcodeRequired = barcodeRequired;
	}

	public boolean isAutomaticBarcode() {
		return this.automaticBarcode;
	}

	public void setAutomaticBarcode(final boolean automaticBarcode) {
		this.automaticBarcode = automaticBarcode;
	}

	public boolean isIncludeHeadings() {
		return this.includeHeadings;
	}

	public void setIncludeHeadings(final boolean includeHeadings) {
		this.includeHeadings = includeHeadings;
	}

	public List<String> getBarcodeFields() {
		return this.barcodeFields;
	}

	public void setBarcodeFields(final List<String> barcodeFields) {
		this.barcodeFields = barcodeFields;
	}

	public String getFileName() {
		return this.fileName;
	}

	public void setFileName(final String fileName) {
		this.fileName = fileName;
	}

	public Set<Field> getAllAvailablefields() {
		return this.allAvailablefields;
	}

	public FileType getFileType() {
		return this.fileType;
	}

	public void setFileType(final FileType fileType) {
		this.fileType = fileType;
	}

	public void setAllAvailablefields(final Set<Field> allAvailablefields) {
		this.allAvailablefields = allAvailablefields;
	}

	public String getSizeOfLabelSheet() {
		return this.sizeOfLabelSheet;
	}

	public void setSizeOfLabelSheet(final String sizeOfLabelSheet) {
		this.sizeOfLabelSheet = sizeOfLabelSheet;
	}

	public String getNumberOfRowsPerPageOfLabel() {
		return this.numberOfRowsPerPageOfLabel;
	}

	public void setNumberOfRowsPerPageOfLabel(final String numberOfRowsPerPageOfLabel) {
		this.numberOfRowsPerPageOfLabel = numberOfRowsPerPageOfLabel;
	}

	@Override
	public int hashCode() {
		return Pojomatic.hashCode(this);
	}

	@Override
	public String toString() {
		return Pojomatic.toString(this);
	}

	@Override
	public boolean equals(final Object o) {
		return Pojomatic.equals(this, o);
	}
}
