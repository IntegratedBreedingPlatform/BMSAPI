package org.ibp.api.brapi.v2.germplasm;

import org.generationcp.middleware.domain.search_request.brapi.v1.GermplasmSearchRequestDto;
import org.generationcp.middleware.domain.search_request.brapi.v2.GermplasmSearchRequest;
import org.ibp.api.mapper.ApiMapper;
import org.modelmapper.ModelMapper;
import org.modelmapper.PropertyMap;

public class GermplasmSearchRequestMapper {

	private static final ModelMapper applicationWideModelMapper = ApiMapper.getInstance();

	private GermplasmSearchRequestMapper() {

	}

	static {
		GermplasmSearchRequestMapper.addGermplasmSearchRequestMapper(GermplasmSearchRequestMapper.applicationWideModelMapper);
	}

	public static ModelMapper getInstance() {
		return GermplasmSearchRequestMapper.applicationWideModelMapper;
	}

	private static void addGermplasmSearchRequestMapper(final ModelMapper mapper) {
		mapper.addMappings(new PropertyMap<GermplasmSearchRequestDto, GermplasmSearchRequest>() {

			@Override
			protected void configure() {
				this.map(this.source.getCommonCropNames(), this.destination.getCommonCropNames());
				this.map(this.source.getAccessionNumbers(), this.destination.getAccessionNumbers());
				this.map(this.source.getGermplasmGenus(), this.destination.getGenus());
				this.map(this.source.getGermplasmSpecies(), this.destination.getSpecies());
				this.map(this.source.getGermplasmNames(), this.destination.getGermplasmNames());
				this.map(this.source.getGermplasmDbIds(), this.destination.getGermplasmDbIds());
				this.map(this.source.getGermplasmPUIs(), this.destination.getGermplasmPUIs());
				this.map(this.source.getPage(), this.destination.getPage());
				this.map(this.source.getPageSize(), this.destination.getPageSize());
			}
		});
	}

}
