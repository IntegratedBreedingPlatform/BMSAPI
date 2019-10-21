
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
	private ExperimentDesign design;

	public MainDesign(final ExperimentDesign design) {
		this.design = design;
	}

	@XmlElement(name = "Template")
	public ExperimentDesign getDesign() {
		return this.design;
	}

	public void setDesign(final ExperimentDesign design) {
		this.design = design;
	}

}
