package org.ibp.api.rest.samplesubmission.domain;

import org.pojomatic.Pojomatic;
import org.pojomatic.annotations.AutoProperty;

import java.util.Map;

/**
 * Created by clarysabel on 12/5/19.
 */
@AutoProperty
public class GOBiiGermplasm {

	private String germplasmName;
	private String externalCode;
	private String speciesName;
	private String typeName;
	private Map<String, String> properties;

	public String getGermplasmName() {
		return germplasmName;
	}

	public void setGermplasmName(final String germplasmName) {
		this.germplasmName = germplasmName;
	}

	public String getExternalCode() {
		return externalCode;
	}

	public void setExternalCode(final String externalCode) {
		this.externalCode = externalCode;
	}

	public String getSpeciesName() {
		return speciesName;
	}

	public void setSpeciesName(final String speciesName) {
		this.speciesName = speciesName;
	}

	public String getTypeName() {
		return typeName;
	}

	public void setTypeName(final String typeName) {
		this.typeName = typeName;
	}

	public Map<String, String> getProperties() {
		return properties;
	}

	public void setProperties(final Map<String, String> properties) {
		this.properties = properties;
	}

	@Override
	public int hashCode() {
		return Pojomatic.hashCode(this);
	}

	@Override
	public String toString() {
		return Pojomatic.toString(this);
	}

	@Override
	public boolean equals(Object o) {
		return Pojomatic.equals(this, o);
	}

}
