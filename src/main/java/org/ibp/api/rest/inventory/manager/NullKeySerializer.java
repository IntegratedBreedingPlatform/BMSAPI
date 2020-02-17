package org.ibp.api.rest.inventory.manager;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import javax.validation.constraints.Null;
import java.io.IOException;
import java.io.StringWriter;

public class NullKeySerializer extends StdSerializer<Object> {


	private static final long serialVersionUID = -4478531309177369056L;

	public NullKeySerializer() {
		this(null);
	}

	public NullKeySerializer(final Class<Object> t) {
		super(t);
	}

	@Override
	public void serialize(final Object value, final JsonGenerator jgen, final SerializerProvider provider) throws IOException, JsonProcessingException {
		jgen.writeFieldName("NULL_VALUES");
	}
}


