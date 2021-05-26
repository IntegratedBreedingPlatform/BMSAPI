package org.ibp.api.brapi.v1.study;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.pojomatic.Pojomatic;
import org.pojomatic.annotations.AutoProperty;

@AutoProperty
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({"contactDbId", "email", "instituteName", "name", "type", "orcid"})
public class Contact {

	private String contactDbId;

	private String name;

	private String email;

	private String type;

	private String orcid;

	private String instituteName;

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
	public Contact(final String contactDbId, final String email, final String name, final String type, final String orcid) {
		this.contactDbId = contactDbId;
		this.email = email;
		this.name = name;
		this.type = type;
		this.orcid = orcid;
	}

	public Contact(final String contactDbId, final String email, final String name, final String type, final String orcid,
		final String instituteName) {
		this(contactDbId, email, name, type, orcid);
		this.instituteName = instituteName;
	}

	/**
	 * @return the contactDBId
	 */
	public String getContactDbId() {
		return this.contactDbId;
	}

	/**
	 * @param contactDbId
	 * @return Contact
	 */
	public Contact setContactDbId(final String contactDbId) {
		this.contactDbId = contactDbId;
		return this;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return this.name;
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
		return this.email;
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
		return this.type;
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
		return this.orcid;
	}

	/**
	 * @param orcid
	 * @return Contact
	 */
	public Contact setOrcid(final String orcid) {
		this.orcid = orcid;
		return this;
	}

	public String getInstituteName() {
		return this.instituteName;
	}

	public void setInstituteName(final String instituteName) {
		this.instituteName = instituteName;
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
