
package org.ibp.api.domain.design;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import java.io.Serializable;
import java.util.List;

public class ExpDesignParameter implements Serializable {

	/**
	 *
	 */
	private static final long serialVersionUID = -926477529786318441L;
	private String name;
	private String value;
	private List<ListItem> listItem; // would only be created in xml if not null

	public ExpDesignParameter() {
		super();
	}

	public ExpDesignParameter(String name, String value) {
		this.name = name;
		this.value = value;
	}

	@XmlAttribute
	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@XmlAttribute
	public String getValue() {
		return this.value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	@XmlElement(name = "ListItem")
	public List<ListItem> getListItem() {
		return this.listItem;
	}

	public void setListItem(List<ListItem> listItem) {
		this.listItem = listItem;
	}

}
