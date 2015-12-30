package org.ibp.api.domain.common;

import org.springframework.validation.Errors;

public interface Command {
	void execute(Errors errors);
}
