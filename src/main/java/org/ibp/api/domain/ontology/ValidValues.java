
package org.ibp.api.domain.ontology;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class ValidValues {

	private String min;
	private String max;
	private final List<Category> categories = new ArrayList<>();

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


	public void setCategories(List<Category> categories){
		this.categories.clear();
		for (Category category : categories) {
			this.categories.add(category);
		}
	}

	public List<Category> getCategories() {
		return this.categories;
	}

}
