package org.ibp.api.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.NOT_FOUND, reason="Requested resource was not found.")
public class NotFoundException extends RuntimeException {

	private static final long serialVersionUID = 1L;

}
