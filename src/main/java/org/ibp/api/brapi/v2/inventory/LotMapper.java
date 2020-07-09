package org.ibp.api.brapi.v2.inventory;

import org.generationcp.middleware.domain.inventory.manager.ExtendedLotDto;
import org.ibp.api.mapper.ApiMapper;
import org.modelmapper.Converter;
import org.modelmapper.ModelMapper;
import org.modelmapper.PropertyMap;
import org.modelmapper.spi.MappingContext;

import java.util.HashMap;
import java.util.Map;

public class LotMapper {

    private static final ModelMapper applicationWideModelMapper = ApiMapper.getInstance();

    static {
        LotMapper.addLotDetailsDataMapping(LotMapper.applicationWideModelMapper);
    }

    public static ModelMapper getInstance() {
        return LotMapper.applicationWideModelMapper;
    }

    private static class AdditionalInfoConverter implements Converter<ExtendedLotDto, Map<String, Object>> {

        @Override
        public Map<String, Object> convert(final MappingContext<ExtendedLotDto, Map<String, Object>> context) {
            final Map<String, Object> additionalInfo = new HashMap<>();
            final ExtendedLotDto lotDto = context.getSource();
            additionalInfo.put("User", lotDto.getCreatedByUsername());
            additionalInfo.put("Status", lotDto.getStatus());
            additionalInfo.put("Notes", lotDto.getNotes());
            additionalInfo.put("GermplasmName", lotDto.getDesignation());
            additionalInfo.put("StockID", lotDto.getStockId());

            return context.getMappingEngine().map(context.create(additionalInfo, context.getDestinationType()));
        }

    }

    private static void addLotDetailsDataMapping(final ModelMapper mapper) {
        mapper.addMappings(new PropertyMap<ExtendedLotDto, LotDetails>() {

            @Override protected void configure() {
                this.using(new AdditionalInfoConverter()).map(this.source).setAdditionalInfo(null);
                this.map().setAmount(this.source.getActualBalance());
                this.map().setCreatedDate(this.source.getCreatedDate());
                this.map().setGermplasmDbId(this.source.getGid());
                this.map().setLocationDbId(this.source.getLocationId());
                this.map().setStorageLocation(this.source.getLocationName());
                this.map().setUnits(this.source.getUnitName());
                this.map().setSeedLotDbId(this.source.getLotUUID());
            }
        });
    }
}
