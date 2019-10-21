
package org.ibp.api.domain.design;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class ExperimentDesign implements Serializable {

	/**
	 *
	 */
	private static final long serialVersionUID = 7602199355069487753L;
	private String name;
	private List<ExperimentDesignParameter> parameters;

	public ExperimentDesign(final String name, final List<ExperimentDesignParameter> parameters) {
		this.name = name;
		this.parameters = parameters;
	}

	@XmlAttribute
	public String getName() {
		return this.name;
	}

	public void setName(final String name) {
		this.name = name;
	}

	@XmlElement(name = "Parameter")
	public List<ExperimentDesignParameter> getParameters() {
		return this.parameters;
	}

	public void setParameters(final List<ExperimentDesignParameter> parameters) {
		this.parameters = parameters;
	}

	public void setParameterValue(final String name, final String value) {
		boolean isFound = false;
		if (this.parameters != null) {
			for (final ExperimentDesignParameter param : this.parameters) {
				if (name != null && param.getName() != null && param.getName().equalsIgnoreCase(name)) {
					param.setValue(value);
					isFound = true;
					break;
				}
			}
		}
		if (!isFound) {
			if (this.parameters == null) {
				this.parameters = new ArrayList<>();
			}
			this.parameters.add(new ExperimentDesignParameter(name, value));
		}
	}

	public String getParameterValue(final String name) {
		if (this.parameters != null) {
			for (final ExperimentDesignParameter param : this.parameters) {
				if (name != null && param.getName() != null && param.getName().equalsIgnoreCase(name)) {
					return param.getValue();
				}
			}
		}
		return "";
	}

	public List<ExperimentDesignParameterListItem> getParameterList(final String name) {
		if (this.parameters != null) {
			for (final ExperimentDesignParameter param : this.parameters) {
				if (name != null && param.getName() != null && param.getName().equalsIgnoreCase(name)) {
					return param.getListItem();
				}
			}
		}
		return new ArrayList<>();
	}
}
