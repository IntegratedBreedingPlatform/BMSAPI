
package org.ibp.api.domain.design;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import java.io.Serializable;
import java.util.List;

public class ExperimentDesignParameter implements Serializable {

	/**
	 *
	 */
	private static final long serialVersionUID = -926477529786318441L;
	private String name;
	private String value;
	private List<ListItem> listItem; // would only be created in xml if not null

	public ExperimentDesignParameter(final String name, final String value) {
		this.name = name;
		this.value = value;
	}

	public ExperimentDesignParameter(final String name, final String value, final List<ListItem> items) {
		this.name = name;
		this.value = value;
		if (items != null && !items.isEmpty()) {
			this.setListItem(items);
		}
	}

	@XmlAttribute
	public String getName() {
		return this.name;
	}

	public void setName(final String name) {
		this.name = name;
	}

	@XmlAttribute
	public String getValue() {
		return this.value;
	}

	public void setValue(final String value) {
		this.value = value;
	}

	@XmlElement(name = "ListItem")
	public List<ListItem> getListItem() {
		return this.listItem;
	}

	public void setListItem(final List<ListItem> listItem) {
		this.listItem = listItem;
	}

}
