package org.ibp.api.rest.labelprinting.domain;

import com.sun.tools.javac.util.List;
import org.pojomatic.Pojomatic;
import org.pojomatic.annotations.AutoProperty;

@AutoProperty
public class LabelsGeneratorInput extends LabelsInfoInput {

	private List<List<String>> fields;

	private boolean barcodeRequired;

	private boolean automaticBarcode;

	private List<String> barcodeFields;

	private String fileName;

	public List<List<String>> getFields() {
		return fields;
	}

	public void setFields(final List<List<String>> fields) {
		this.fields = fields;
	}

	public boolean isBarcodeRequired() {
		return barcodeRequired;
	}

	public void setBarcodeRequired(final boolean barcodeRequired) {
		this.barcodeRequired = barcodeRequired;
	}

	public boolean isAutomaticBarcode() {
		return automaticBarcode;
	}

	public void setAutomaticBarcode(final boolean automaticBarcode) {
		this.automaticBarcode = automaticBarcode;
	}

	public List<String> getBarcodeFields() {
		return barcodeFields;
	}

	public void setBarcodeFields(final List<String> barcodeFields) {
		this.barcodeFields = barcodeFields;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(final String fileName) {
		this.fileName = fileName;
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
