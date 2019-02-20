package org.ibp.api.rest.preset;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonView;
import org.pojomatic.Pojomatic;
import org.pojomatic.annotations.AutoProperty;

import java.util.List;

@AutoProperty
public class LabelPrintingPresetDTO extends PresetDTO {

	@AutoProperty
	private static class BarcodeSetting {

		@JsonView(PresetDTO.View.Configuration.class)
		private boolean barcodeNeeded;

		@JsonView(PresetDTO.View.Configuration.class)
		private boolean automaticBarcode;

		@JsonView(PresetDTO.View.Configuration.class)
		private List<Integer> barcodeFields;

		public boolean isBarcodeNeeded() {
			return barcodeNeeded;
		}

		public void setBarcodeNeeded(final boolean barcodeNeeded) {
			this.barcodeNeeded = barcodeNeeded;
		}

		public boolean isAutomaticBarcode() {
			return automaticBarcode;
		}

		public void setAutomaticBarcode(final boolean automaticBarcode) {
			this.automaticBarcode = automaticBarcode;
		}

		public List<Integer> getBarcodeFields() {
			return barcodeFields;
		}

		public void setBarcodeFields(final List<Integer> barcodeFields) {
			this.barcodeFields = barcodeFields;
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


	@JsonView(PresetDTO.View.Configuration.class)
	private String outputType;

	@JsonView(PresetDTO.View.Configuration.class)
	private List<List<Integer>> selectedFields;

	@JsonView(PresetDTO.View.Configuration.class)
	private BarcodeSetting barcodeSetting;

	public String getOutputType() {
		return outputType;
	}

	public void setOutputType(final String outputType) {
		this.outputType = outputType;
	}

	public List<List<Integer>> getSelectedFields() {
		return selectedFields;
	}

	public void setSelectedFields(final List<List<Integer>> selectedFields) {
		this.selectedFields = selectedFields;
	}

	public BarcodeSetting getBarcodeSetting() {
		return barcodeSetting;
	}

	public void setBarcodeSetting(final BarcodeSetting barcodeSetting) {
		this.barcodeSetting = barcodeSetting;
	}

}
