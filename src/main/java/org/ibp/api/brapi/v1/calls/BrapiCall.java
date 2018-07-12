
package org.ibp.api.brapi.v1.calls;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@JsonInclude(Include.NON_NULL)
@JsonPropertyOrder({"call", "datatypes", "methods", "versions"})
public class BrapiCall {

	private String call;

	private List<String> datatypes;

	private List<RequestMethod> methods;

	private List<String> versions;

	/**
	 * No args constructor required by serialization libraries.
	 */
	public BrapiCall() {
	}

	public BrapiCall(
		final String call, final List<String> datatypes, final List<RequestMethod> methods, final List<String> versions) {
		this.call = call;
		this.datatypes = datatypes;
		this.methods = methods;
		this.versions = versions;
	}

	public String getCall() {
		return this.call;
	}

	public void setCall(final String call) {
		this.call = call;
	}

	public List<String> getDatatypes() {
		return this.datatypes;
	}

	public void setDatatypes(final List<String> datatypes) {
		this.datatypes = datatypes;
	}

	public List<RequestMethod> getMethods() {
		return this.methods;
	}

	public void setMethods(final List<RequestMethod> methods) {
		this.methods = methods;
	}

	public List<String> getVersions() {
		return this.versions;
	}

	public void setVersions(final List<String> versions) {
		this.versions = versions;
	}

	@Override
	public boolean equals(final Object o) {
		if (this == o)
			return true;
		if (o == null || this.getClass() != o.getClass())
			return false;
		final BrapiCall brapiCall = (BrapiCall) o;
		return Objects.equals(this.call, brapiCall.call) &&
			Objects.equals(this.datatypes, brapiCall.datatypes) &&
			Objects.equals(this.methods, brapiCall.methods) &&
			Objects.equals(this.versions, brapiCall.versions);
	}

	@Override
	public int hashCode() {

		return Objects.hash(this.call, this.datatypes, this.methods, this.versions);
	}

	@Override
	public String toString() {
		return "BrapiCall{" +
			"call='" + this.call + '\'' +
			", datatypes='" + this.datatypes + '\'' +
			", methods='" + this.methods + '\'' +
			", versions='" + this.versions + '\'' +
			'}';
	}

	public void addMethods(final RequestMethod requestMethod) {
		if (this.methods == null) {
			this.methods = new ArrayList<RequestMethod>();
		}
		this.methods.add(requestMethod);
	}
}
