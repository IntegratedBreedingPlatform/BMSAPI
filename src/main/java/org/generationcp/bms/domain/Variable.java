package org.generationcp.bms.domain;

import org.generationcp.middleware.domain.dms.StandardVariable;
import org.generationcp.middleware.domain.oms.TermSummary;

public class Variable {
	
	//NOTE: Do not expose the entire standardVariable via any public method.
	private final StandardVariable standardVariable;
	
	private String localName;
	private String localDescription;
	
	private String value;
	
	public Variable(StandardVariable standardVariable) {
		if(standardVariable == null) {
			throw new IllegalArgumentException("standardVariable is ");
		}
		this.standardVariable = standardVariable;
	}
	
	public TermSummary getProperty() {
		return new TermSummary(standardVariable.getProperty().getId(), 
				standardVariable.getProperty().getName(), 
				standardVariable.getProperty().getDefinition());
	}
	
	public TermSummary getMethod() {
		return new TermSummary(standardVariable.getMethod().getId(), 
				standardVariable.getMethod().getName(), 
				standardVariable.getMethod().getDefinition());
	}
	
	public TermSummary getScale() {
		return new TermSummary(standardVariable.getScale().getId(), 
				standardVariable.getScale().getName(), 
				standardVariable.getScale().getDefinition());
	}

	public String getLocalName() {
		return localName;
	}

	public void setLocalName(String localName) {
		this.localName = localName;
	}

	public String getLocalDescription() {
		return localDescription;
	}

	public void setLocalDescription(String localDescription) {
		this.localDescription = localDescription;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime
				* result
				+ ((standardVariable == null) ? 0 : standardVariable.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Variable other = (Variable) obj;
		if (standardVariable == null) {
			if (other.standardVariable != null)
				return false;
		} else if (!standardVariable.equals(other.standardVariable))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "Variable [standardVariable=" + standardVariable
				+ ", localName=" + localName + ", localDescription="
				+ localDescription + ", value=" + value + "]";
	}
}
