package org.ibp.api.rest.common;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.pojomatic.Pojomatic;
import org.pojomatic.annotations.AutoProperty;

import java.util.ArrayList;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
@AutoProperty
public class UserTreeState {

	private String userId;
	private List<String> programFolders = new ArrayList<>();
	private List<String> cropFolders = new ArrayList<>();

	public UserTreeState() {

	}

	public String getUserId() {
		return this.userId;
	}

	public void setUserId(final String userId) {
		this.userId = userId;
	}

	public List<String> getProgramFolders() {
		return this.programFolders;
	}

	public void setProgramFolders(final List<String> programFolders) {
		this.programFolders = programFolders;
	}

	public void setCropFolders(final List<String> cropFolders) {
		this.cropFolders = cropFolders;
	}

	public List<String> getCropFolders() {
		return this.cropFolders;
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
