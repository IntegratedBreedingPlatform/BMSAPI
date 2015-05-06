package org.ibp.api.domain.ontology;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import org.ibp.api.java.impl.middleware.common.CommonUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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

	public void setMin(String min) {
		if(min != null){
			this.min = CommonUtil.tryParseSafe(min);
		}
	}

	public Integer getMax() {
		return this.max;
	}

	public void setMax(String max) {
		if(max != null){
			this.max = CommonUtil.tryParseSafe(max);
		}
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
