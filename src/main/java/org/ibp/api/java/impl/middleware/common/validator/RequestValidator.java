package org.ibp.api.java.impl.middleware.common.validator;

import org.ibp.api.domain.common.ValidationErrors;

public interface RequestValidator<T> {

    void validate(T target, ValidationErrors response);

}
