package org.ibp.api.brapi.v2.germplasm;

import org.generationcp.middleware.api.breedingmethod.BreedingMethodDTO;
import org.ibp.api.mapper.ApiMapper;
import org.modelmapper.Converter;
import org.modelmapper.ModelMapper;
import org.modelmapper.PropertyMap;
import org.modelmapper.spi.MappingContext;

import java.util.HashMap;
import java.util.Map;

public class BreedingMethodMapper {
	private static final ModelMapper applicationWideModelMapper = ApiMapper.getInstance();

	private BreedingMethodMapper() {

	}

	static {
		BreedingMethodMapper.addBreedingMethodDtoDataMapping(BreedingMethodMapper.applicationWideModelMapper);
	}

	public static ModelMapper getInstance() {
		return BreedingMethodMapper.applicationWideModelMapper;
	}

	private static void addBreedingMethodDtoDataMapping(final ModelMapper mapper) {
		mapper.addMappings(new PropertyMap<BreedingMethodDTO, BreedingMethod>() {

			@Override
			protected void configure() {
				this.map().setAbbreviation(this.source.getCode());
				this.map().setBreedingMethodDbId(String.valueOf(this.source.getMid()));
				this.map().setBreedingMethodName(this.source.getName());
				this.map().setDescription(this.source.getDescription());
			}
		});
	}
}
