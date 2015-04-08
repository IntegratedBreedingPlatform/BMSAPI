package org.ibp.api.domain.ontology;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class ValidValues {

	private String min;
	private String max;
	private List<NameDescription> categories;

	public ValidValues() {
	}

	public String getMin() {
		return this.min;
	}

	public void setMin(String min) {
		this.min = min;
	}

	public String getMax() {
		return this.max;
	}

	public void setMax(String max) {
		this.max = max;
	}

	public List<NameDescription> getCategories() {
		return this.categories;
	}

	@JsonIgnore
	public void setCategoriesFromMap(Map<String, String> categories) {
		this.mapCategories(categories);
	}

	public void setCategories(List<NameDescription> categories) {
		this.categories = categories;
	}

	private void mapCategories(Map<String, String> suppliedCategories) {
		if (suppliedCategories != null) {
			this.categories = new ArrayList<>();
			for (String k : suppliedCategories.keySet()) {
				this.categories.add(new NameDescription(k, suppliedCategories.get(k)));
			}
		}
	}
}
