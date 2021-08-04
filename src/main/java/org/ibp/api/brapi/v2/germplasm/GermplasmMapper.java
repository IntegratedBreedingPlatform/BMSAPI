package org.ibp.api.brapi.v2.germplasm;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;
import org.generationcp.middleware.api.brapi.v1.germplasm.GermplasmDTO;
import org.ibp.api.brapi.v1.germplasm.Germplasm;
import org.ibp.api.brapi.v1.germplasm.GermplasmOrigin;
import org.ibp.api.mapper.ApiMapper;
import org.modelmapper.Converter;
import org.modelmapper.ModelMapper;
import org.modelmapper.PropertyMap;
import org.modelmapper.spi.MappingContext;
import org.springframework.util.CollectionUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GermplasmMapper {

	private static final ModelMapper applicationWideModelMapper = ApiMapper.getInstance();

	private GermplasmMapper() {

	}

	static {
		GermplasmMapper.addGermplasmMapper(GermplasmMapper.applicationWideModelMapper);
	}

	public static ModelMapper getInstance() {
		return GermplasmMapper.applicationWideModelMapper;
	}

	private static class GermplasmOriginConverter implements Converter<String, GermplasmOrigin> {

		@Override
		public GermplasmOrigin convert(final MappingContext<String, GermplasmOrigin> context) {
			if (!StringUtils.isEmpty(context.getSource())) {
				try {
					final GermplasmOrigin germplasmOrigin = new GermplasmOrigin();
					final Map<String, Object> geoCoordinatesMap = new ObjectMapper().readValue(context.getSource(), HashMap.class);
					final Map<String, Object> geojson = (Map<String, Object>) geoCoordinatesMap.get("geoCoordinates");
					germplasmOrigin.setCoordinateUncertainty("");
					germplasmOrigin.setCoordinates(geojson);
					return context.getMappingEngine().map(context.create(germplasmOrigin, context.getDestinationType()));
				} catch (final IOException e) {
					return null;
				}
			} else {
				return null;
			}

		}

	}

	private static void addGermplasmMapper(final ModelMapper mapper) {

		mapper.addMappings(new PropertyMap<GermplasmDTO, Germplasm>() {

			@Override
			protected void configure() {
				this.map(this.source.getGermplasmDbId(), this.destination.getGermplasmDbId());
				this.map(this.source.getDefaultDisplayName(), this.destination.getDefaultDisplayName());
				this.map(this.source.getAccessionNumber(), this.destination.getAccessionNumber());
				this.map(this.source.getGermplasmName(), this.destination.getGermplasmName());
				this.map(this.source.getGermplasmPUI(), this.destination.getGermplasmPUI());
				this.map(this.source.getPedigree(), this.destination.getPedigree());
				this.map(this.source.getGermplasmSeedSource(), this.destination.getGermplasmSeedSource());
				this.map(this.source.getSynonyms(), this.destination.getSynonyms());
				this.map(this.source.getCommonCropName(), this.destination.getCommonCropName());
				this.map(this.source.getInstituteCode(), this.destination.getInstituteCode());
				this.map(this.source.getInstituteName(), this.destination.getInstituteName());
				this.map(this.source.getBiologicalStatusOfAccessionCode(), this.destination.getBiologicalStatusOfAccessionCode());
				this.map(this.source.getCountryOfOriginCode(), this.destination.getCountryOfOriginCode());
				this.map(this.source.getGenus(), this.destination.getGenus());
				this.map(this.source.getSpecies(), this.destination.getSpecies());
				this.map(this.source.getSpeciesAuthority(), this.destination.getSpeciesAuthority());
				this.map(this.source.getSubtaxa(), this.destination.getSubtaxa());
				this.map(this.source.getSubtaxaAuthority(), this.destination.getSubtaxaAuthority());
				this.map(this.source.getAcquisitionDate(), this.destination.getAcquisitionDate());
				this.map(this.source.getBreedingMethodDbId(), this.destination.getBreedingMethodDbId());
				this.map(this.source.getGermplasmGenus(), this.destination.getGermplasmGenus());
				this.map(this.source.getGermplasmSpecies(), this.destination.getGermplasmSpecies());
				this.map(this.source.getSeedSource(), this.destination.getSeedSource());
				this.map(this.source.getDocumentationURL(), this.destination.getDocumentationURL());
				this.map(this.source.getEntryNumber(), this.destination.getEntryNumber());
				this.map(this.source.getAdditionalInfo(), this.destination.getAdditionalInfo());
				this.map(this.source.getExternalReferences(), this.destination.getExternalReferences());
				// Convert GeoJson string to GermplasmOrigin object
				this.using(new GermplasmMapper.GermplasmOriginConverter()).map(this.source.getGermplasmOrigin()).setGermplasmOrigin(null);
			}

		});
	}

	public static List<Germplasm> mapGermplasm(final List<GermplasmDTO> germplasmDTOList) {
		final List<Germplasm> germplasmList = new ArrayList<>();
		final ModelMapper modelMapper = GermplasmMapper.getInstance();
		if (!CollectionUtils.isEmpty(germplasmDTOList)) {
			for (final GermplasmDTO germplasmDTO : germplasmDTOList) {
				final Germplasm germplasm = modelMapper.map(germplasmDTO, Germplasm.class);
				germplasmList.add(germplasm);
			}
		}
		return germplasmList;
	}

}
