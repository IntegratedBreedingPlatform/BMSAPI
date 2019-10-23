
package org.ibp.api.domain.design;

import javax.xml.bind.annotation.XmlAttribute;
import java.io.Serializable;

public class ListItem implements Serializable {

	/**
	 *
	 */
	private static final long serialVersionUID = -927084614613247587L;
	private String value;

	public ListItem(String value) {
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
