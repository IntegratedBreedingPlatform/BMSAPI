package org.ibp.api.brapi.v2.study;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.pojomatic.annotations.AutoProperty;

@AutoProperty
@JsonInclude(JsonInclude.Include.NON_NULL) @JsonPropertyOrder({"parameterName", "description", "unit", "unitPUI", "value", "valuePUI"})
public class EnvironmentParameter {

	private String description;

	private String parameterName;

	private String parameterPUI;

	private String unit;

	private Integer unitPUI;

	private String value;

	private String valuePUI;

	public EnvironmentParameter() {
	}

	public EnvironmentParameter(final String description, final String parameterName, final String parameterPUI, final String unit,
		final Integer unitPUI, final String value, final String valuePUI) {
		this.description = description;
		this.parameterName = parameterName;
		this.parameterPUI = parameterPUI;
		this.unit = unit;
		this.unitPUI = unitPUI;
		this.value = value;
		this.valuePUI = valuePUI;
	}

	public String getDescription() {
		return description;
	}


	public EnvironmentParameter setDescription(final String description) {
		this.description = description;
		return this;
	}

	public String getParameterName() {
		return parameterName;
	}

	public EnvironmentParameter setParameterName(final String parameterName) {
		this.parameterName = parameterName;
		return this;
	}

	public String getParameterPUI() {
		return parameterPUI;
	}

	public EnvironmentParameter setParameterPUI(final String parameterPUI) {
		this.parameterPUI = parameterPUI;
		return this;
	}

	public String getUnit() {
		return unit;
	}

	public EnvironmentParameter setUnit(final String unit) {
		this.unit = unit;
		return this;
	}

	public Integer getUnitPUI() {
		return unitPUI;
	}

	public EnvironmentParameter setUnitPUI(final Integer unitPUI) {
		this.unitPUI = unitPUI;
		return this;
	}

	public String getValue() {
		return value;
	}

	public EnvironmentParameter setValue(final String value) {
		this.value = value;
		return this;
	}

	public String getValuePUI() {
		return valuePUI;
	}

	public EnvironmentParameter setValuePUI(final String valuePUI) {
		this.valuePUI = valuePUI;
		return this;
	}

}
