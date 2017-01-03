package org.ibp.api.brapi.v1.study;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.pojomatic.Pojomatic;
import org.pojomatic.annotations.AutoProperty;

@AutoProperty @JsonInclude(JsonInclude.Include.NON_NULL) @JsonPropertyOrder({"contactDbId", "name", "email", "type", "orcid"})
public class Contact {

	private Integer contactDbId;

	private String name;

	private String email;

	private String type;

	private String orcid;

	/**
	 * Empty constructor
	 */
	public Contact() {
	}

	/**
	 * Full constructor
	 *
	 * @param contactDbId
	 * @param email
	 * @param name
	 * @param type
	 * @param orcid
	 */
	public Contact(final Integer contactDbId, final String email, final String name, final String type, final String orcid) {
		this.contactDbId = contactDbId;
		this.email = email;
		this.name = name;
		this.type = type;
		this.orcid = orcid;
	}

	/**
	 * @return the contactDBId
	 */
	public Integer getContactDbId() {
		return contactDbId;
	}

	/**
	 * @param contactDbId
	 * @return Contact
	 */
	public Contact setContactDbId(final Integer contactDbId) {
		this.contactDbId = contactDbId;
		return this;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name
	 * @return Contact
	 */
	public Contact setName(final String name) {
		this.name = name;
		return this;
	}

	/**
	 * @return the email
	 */
	public String getEmail() {
		return email;
	}

	/**
	 * @param email
	 * @return Contact
	 */
	public Contact setEmail(final String email) {
		this.email = email;
		return this;
	}

	/**
	 * @return the type
	 */
	public String getType() {
		return type;
	}

	/**
	 * @param type
	 * @return Contact
	 */
	public Contact setType(final String type) {
		this.type = type;
		return this;
	}

	/**
	 * @return the orcid
	 */
	public String getOrcid() {
		return orcid;
	}

	/**
	 * @param orcid
	 * @return Contact
	 */
	public Contact setOrcid(final String orcid) {
		this.orcid = orcid;
		return this;
	}

	@Override public int hashCode() {
		return Pojomatic.hashCode(this);
	}

	@Override public String toString() {
		return Pojomatic.toString(this);
	}

	@Override public boolean equals(Object o) {
		return Pojomatic.equals(this, o);
	}

}
