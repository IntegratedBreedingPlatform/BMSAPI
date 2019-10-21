
package org.ibp.api.domain.design;

import javax.xml.bind.annotation.XmlAttribute;
import java.io.Serializable;

public class ExperimentDesignParameterListItem implements Serializable {

	/**
	 *
	 */
	private static final long serialVersionUID = -927084614613247587L;
	private String value;

	public ExperimentDesignParameterListItem(String value) {
		this.value = value;
	}

	@XmlAttribute
	public String getValue() {
		return this.value;
	}

	public void setValue(String value) {
		this.value = value;
	}

}
