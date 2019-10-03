
package org.ibp.api.domain.design;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;

@XmlRootElement(name = "Templates")
public class MainDesign implements Serializable {

	/**
	 *
	 */
	private static final long serialVersionUID = 3881877704058005795L;
	private ExpDesign design;

	public MainDesign() {

	}

	public MainDesign(ExpDesign design) {
		super();
		this.design = design;
	}

	@XmlElement(name = "Template")
	public ExpDesign getDesign() {
		return this.design;
	}

	public void setDesign(ExpDesign design) {
		this.design = design;
	}

}
