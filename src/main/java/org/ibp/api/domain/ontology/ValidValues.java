
package org.ibp.api.domain.ontology;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class ValidValues {

	private Integer min;
	private Integer max;
	private List<VariableCategory> categories;

	public ValidValues() {
	}

	public Integer getMin() {
		return this.min;
	}

	public void setMin(Integer min) {
		this.min = min;
	}

	public Integer getMax() {
		return this.max;
	}

	public void setMax(Integer max) {
		this.max = max;
	}

	public List<VariableCategory> getCategories() {
		return this.categories;
	}

	@JsonIgnore
	public void setCategoriesFromMap(Map<String, String> categories) {
		this.mapCategories(categories);
	}

	public void setCategories(List<VariableCategory> categories) {
		this.categories = categories;
	}

	private void mapCategories(Map<String, String> suppliedCategories) {
		if (suppliedCategories != null) {
			this.categories = new ArrayList<>();
			for (String k : suppliedCategories.keySet()) {
				this.categories.add(new VariableCategory(k, suppliedCategories.get(k)));
			}
		}
	}
}
