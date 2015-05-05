package org.ibp.api.domain.common;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ValidationErrors {

	private final List<ErrorLine> errors = new ArrayList<>();

	public void addError(String template){
		this.errors.add(new ErrorLine(template, new ArrayList<String>(), new ArrayList<String>(), new ArrayList<String>()));
	}

	public void addError(String template, String fieldName, String labelName){
		this.errors.add(new ErrorLine(template, Collections.singletonList(fieldName), Collections.singletonList(labelName), new ArrayList<String>()));
	}

	public void addError(String template, String fieldName, String labelName, String value){
		this.errors.add(new ErrorLine(template, Collections.singletonList(fieldName), Collections.singletonList(labelName), Collections.singletonList(value)));
	}

	public List<ErrorLine> getErrors() {
		return errors;
	}

	public boolean isValid() {
		return this.errors.isEmpty();
	}

	public static class ErrorLine {

		private final List<String> fieldNames = new ArrayList<>();
		private final List<String> termNames = new ArrayList<>();
		private final List<String> values = new ArrayList<>();
		private final String template; //msg key

		/**
		 *
		 * @param template		Template of message.
		 * @param fieldNames	Filed names of submitted form
		 * @param termNames		Form label names
		 * @param values		Direct values from form
		 */
		public ErrorLine(String template, List<String> fieldNames, List<String> termNames, List<String> values) {
			this.template = template;
			this.fieldNames.addAll(fieldNames);
			this.termNames.addAll(termNames);
			this.values.addAll(values);
		}

		public String getTemplate() {
			return this.template;
		}

		public List<String> getFieldNames() {
			return this.fieldNames;
		}

		public List<String> getTermNames() {
			return this.termNames;
		}

		public List<String> getValues() {
			return this.values;
		}
	}
}
