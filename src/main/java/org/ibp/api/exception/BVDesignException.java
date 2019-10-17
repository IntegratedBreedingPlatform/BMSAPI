
package org.ibp.api.exception;

public class BVDesignException extends RuntimeException {

	/**
	 *
	 */
	private static final long serialVersionUID = 24993039887181720L;
	private String bvErrorCode;

	public BVDesignException(final String bvErrorCode) {
		super();
		this.bvErrorCode = bvErrorCode;
	}

	public String getBvErrorCode() {
		return this.bvErrorCode;
	}

	public void setBvErrorCode(String bvErrorCode) {
		this.bvErrorCode = bvErrorCode;
	}

}
