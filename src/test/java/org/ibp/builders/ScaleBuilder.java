package org.ibp.builders;

import org.generationcp.middleware.domain.oms.DataType;
import org.generationcp.middleware.domain.oms.OntologyScale;
import org.generationcp.middleware.domain.oms.Term;

import java.util.Map;

public class ScaleBuilder {

	private final Term term;

	public ScaleBuilder() {
		this.term = new Term();
	}

	public OntologyScale build(int id, String name, String description, DataType dataType, String minValue,
			String maxValue, Map<String, String> categories) {
		this.term.setId(id);
		this.term.setName(name);
		this.term.setDefinition(description);
		OntologyScale scale = new OntologyScale(this.term);
		scale.setDataType(dataType);
		scale.setMinValue(minValue);
		scale.setMaxValue(maxValue);
		if (categories != null) {
			for (String k : categories.keySet()) {
				scale.addCategory(k, categories.get(k));
			}
		}
		return scale;
	}
}
