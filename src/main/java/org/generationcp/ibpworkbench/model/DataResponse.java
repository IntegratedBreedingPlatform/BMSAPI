package org.generationcp.ibpworkbench.model;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

/*******************************************************************************
 * Copyright (c) 2012, All Rights Reserved.
 *
 * Generation Challenge Programme (GCP)
 *
 *
 * This software is licensed for use under the terms of the GNU General Public
 * License (http://bit.ly/8Ztv8M) and the provisions of Part F of the Generation
 * Challenge Programme Amended Consortium Agreement (http://bit.ly/KQX1nL)
 *
 *******************************************************************************/

@XmlRootElement(name = "Response")
@XmlType(propOrder = {"successful", "message"})
public class DataResponse {
    private boolean successful;
    private String message;

    public DataResponse() {
    }

    public DataResponse(boolean successful, String message) {
        this.successful = successful;
        this.message = message;
    }

    @XmlAttribute
    public boolean isSuccessful() {
        return successful;
    }

    public void setSuccessful(boolean successful) {
        this.successful = successful;
    }

    @XmlAttribute
    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public String toString() {
        return "DataResponse [successful=" + successful + ", message=" + message + "]";
    }
}
