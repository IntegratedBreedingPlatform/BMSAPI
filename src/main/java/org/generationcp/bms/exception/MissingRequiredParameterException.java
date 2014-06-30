package org.generationcp.bms.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.BAD_REQUEST, reason="Missing required parameter.")
public class MissingRequiredParameterException extends  MissingServletRequestParameterException {

	private static final long serialVersionUID = 1L;
	
	public MissingRequiredParameterException(String parameterName, String parameterType) {
		super(parameterName, parameterType);
	}

}
