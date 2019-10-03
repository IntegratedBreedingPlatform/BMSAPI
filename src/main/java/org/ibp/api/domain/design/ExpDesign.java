
package org.ibp.api.domain.design;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class ExpDesign implements Serializable {

	/**
	 *
	 */
	private static final long serialVersionUID = 7602199355069487753L;
	private String name;
	private List<ExpDesignParameter> parameters;

	public ExpDesign() {
		super();
	}

	public ExpDesign(String name, List<ExpDesignParameter> parameters) {
		super();
		this.name = name;
		this.parameters = parameters;
	}

	@XmlAttribute
	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@XmlElement(name = "Parameter")
	public List<ExpDesignParameter> getParameters() {
		return this.parameters;
	}

	public void setParameters(List<ExpDesignParameter> parameters) {
		this.parameters = parameters;
	}

	public void setParameterValue(String name, String value) {
		boolean isFound = false;
		if (this.parameters != null) {
			for (ExpDesignParameter param : this.parameters) {
				if (name != null && param.getName() != null && param.getName().equalsIgnoreCase(name)) {
					param.setValue(value);
					isFound = true;
					break;
				}
			}
		}
		if (!isFound) {
			if (this.parameters == null) {
				this.parameters = new ArrayList<ExpDesignParameter>();
			}
			this.parameters.add(new ExpDesignParameter(name, value));
		}
	}

	public String getParameterValue(String name) {
		if (this.parameters != null) {
			for (ExpDesignParameter param : this.parameters) {
				if (name != null && param.getName() != null && param.getName().equalsIgnoreCase(name)) {
					return param.getValue();
				}
			}
		}
		return "";
	}

	public List<ListItem> getParameterList(String name) {
		if (this.parameters != null) {
			for (ExpDesignParameter param : this.parameters) {
				if (name != null && param.getName() != null && param.getName().equalsIgnoreCase(name)) {
					return param.getListItem();
				}
			}
		}
		return new ArrayList<ListItem>();
	}
}
