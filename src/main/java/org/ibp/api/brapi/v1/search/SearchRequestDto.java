package org.ibp.api.brapi.v1.search;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import org.pojomatic.annotations.AutoProperty;

@AutoProperty
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.EXISTING_PROPERTY, property = "requestType", visible = true)
@JsonSubTypes(value = {@JsonSubTypes.Type(value = GermplasmSearchRequestDto.class, name = "GermplasmSearchRequestDto")})
public abstract class SearchRequestDto {

}
