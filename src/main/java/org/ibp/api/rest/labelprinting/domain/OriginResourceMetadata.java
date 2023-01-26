package org.ibp.api.rest.labelprinting.domain;

import org.pojomatic.Pojomatic;
import org.pojomatic.annotations.AutoProperty;

import java.util.HashMap;
import java.util.Map;

@AutoProperty
public class OriginResourceMetadata {

	private String defaultFileName = "";
	private Map<String, String> metadata = new HashMap<>();

	public OriginResourceMetadata() {
	}

	public OriginResourceMetadata(final String defaultFileName, final Map<String, String> metadata) {
		this.defaultFileName = defaultFileName;
		this.metadata = metadata;
	}

	public String getDefaultFileName() {
		return this.defaultFileName;
	}

	public void setDefaultFileName(final String defaultFileName) {
		this.defaultFileName = defaultFileName;
	}

	public Map<String, String> getMetadata() {
		return this.metadata;
	}

	public void setMetadata(final Map<String, String> metadata) {
		this.metadata = metadata;
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
	public boolean equals(final Object o) {
		return Pojomatic.equals(this, o);
	}

}
