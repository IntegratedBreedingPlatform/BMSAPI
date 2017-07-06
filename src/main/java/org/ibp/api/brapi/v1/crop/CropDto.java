package org.ibp.api.brapi.v1.crop;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.ibp.api.brapi.v1.common.Metadata;
import org.ibp.api.brapi.v1.common.Result;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({"metadata", "result"})
public class CropDto {

	private Metadata metadata;

	private Result<String> result;

	public void setMetadata(final Metadata metadata) {
		this.metadata = metadata;
	}

	public void setResult(final Result<String> result) {
		this.result = result;
	}

	public Metadata getMetadata() {
		return metadata;
	}

	public Result<String> getResult() {
		return result;
	}
}
