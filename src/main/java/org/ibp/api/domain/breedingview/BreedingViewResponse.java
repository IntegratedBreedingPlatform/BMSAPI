/*******************************************************************************
 * Copyright (c) 2012, All Rights Reserved.
 *
 * Generation Challenge Programme (GCP)
 *
 *
 * This software is licensed for use under the terms of the GNU General Public License (http://bit.ly/8Ztv8M) and the provisions of Part F
 * of the Generation Challenge Programme Amended Consortium Agreement (http://bit.ly/KQX1nL)
 *
 *******************************************************************************/

package org.ibp.api.domain.breedingview;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlRootElement(name = "Response")
@XmlType(propOrder = {"successful", "message"})
public class BreedingViewResponse {

	private boolean successful;
	private String message;

	public BreedingViewResponse() {
	}

	public BreedingViewResponse(final boolean successful, final String message) {
		this.successful = successful;
		this.message = message;
	}

	@XmlAttribute
	public boolean isSuccessful() {
		return this.successful;
	}

	public void setSuccessful(final boolean successful) {
		this.successful = successful;
	}

	@XmlAttribute
	public String getMessage() {
		return this.message;
	}

	public void setMessage(final String message) {
		this.message = message;
	}

	@Override
	public String toString() {
		return "DataResponse [successful=" + this.successful + ", message=" + this.message + "]";
	}
}
