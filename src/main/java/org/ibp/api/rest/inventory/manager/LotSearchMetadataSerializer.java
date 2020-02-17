package org.ibp.api.rest.inventory.manager;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import java.io.IOException;
import java.math.BigInteger;
import java.util.Map;

public class LotSearchMetadataSerializer extends StdSerializer<Map<String, BigInteger>> {
	private final ObjectMapper mapper = new ObjectMapper();


	public LotSearchMetadataSerializer(){
		this(null);
	}

	public LotSearchMetadataSerializer(Class<Map<String, BigInteger>> t){
		super(t);
	}

	@Override
	public void serialize(final Map<String, BigInteger> s, final JsonGenerator jsonGenerator, final SerializerProvider serializerProvider)
		throws IOException, JsonProcessingException {
		mapper.configure(SerializationFeature.WRITE_NULL_MAP_VALUES, false);
		mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
		mapper.getSerializerProvider()
			.setNullKeySerializer(new NullKeySerializer());
		mapper.writeValueAsString(s);
	}

}
