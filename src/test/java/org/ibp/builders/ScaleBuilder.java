package org.ibp.builders;

import java.util.Map;

import org.generationcp.middleware.domain.oms.DataType;
import org.generationcp.middleware.domain.oms.Scale;
import org.generationcp.middleware.domain.oms.Term;

public class ScaleBuilder {

	private final Term term;

	public ScaleBuilder() {
		this.term = new Term();
	}

	public Scale build(int id, String name, String description, DataType dataType, String minValue,
			String maxValue, Map<String, String> categories) {
		this.term.setId(id);
		this.term.setName(name);
		this.term.setDefinition(description);
		Scale scale = new Scale(this.term);
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
