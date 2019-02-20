package org.ibp.api.rest.preset.domain;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonView;
import org.pojomatic.Pojomatic;
import org.pojomatic.annotations.AutoProperty;

@AutoProperty
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.EXISTING_PROPERTY, property = "outputType", visible = true)
@JsonSubTypes(value = {@JsonSubTypes.Type(value = PDFFilePresetConfigurationDTO.class, name = "PDF"),
	@JsonSubTypes.Type(value = CSVXLSFilePresetConfigurationDTO.class, name = "CSV"),
	@JsonSubTypes.Type(value = CSVXLSFilePresetConfigurationDTO.class, name = "XLS")})
public class FilePresetConfigurationDTO {

	@JsonView(PresetDTO.View.Configuration.class)
	private String outputType;

	public String getOutputType() {
		return outputType;
	}

	public void setOutputType(final String outputType) {
		this.outputType = outputType;
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
