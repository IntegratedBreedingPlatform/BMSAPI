package org.ibp.api.brapi.v2.inventory;

import org.generationcp.middleware.domain.inventory.manager.ExtendedLotDto;
import org.ibp.api.mapper.ApiMapper;
import org.modelmapper.Converter;
import org.modelmapper.ModelMapper;
import org.modelmapper.PropertyMap;
import org.modelmapper.spi.MappingContext;

import java.text.SimpleDateFormat;
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
            final Map<String, String> additionalInfo = new HashMap<>();
            final ExtendedLotDto lotDto = context.getSource();
            additionalInfo.put("username", lotDto.getCreatedByUsername());
            additionalInfo.put("status", lotDto.getStatus());
            additionalInfo.put("germplasmName", lotDto.getDesignation());
            additionalInfo.put("actualBalance", String.valueOf(lotDto.getActualBalance()));
            additionalInfo.put("totalReserved", String.valueOf(lotDto.getReservedTotal()));
            additionalInfo.put("totalWithdrawals", String.valueOf(lotDto.getWithdrawalTotal()));
            additionalInfo.put("pendingDeposits", String.valueOf(lotDto.getPendingDepositsTotal()));
            final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            if (lotDto.getLastDepositDate() != null) {
                additionalInfo.put("lastDepositDate", dateFormat.format(lotDto.getLastDepositDate()));
            }
            if (lotDto.getLastWithdrawalDate() != null) {
                additionalInfo.put("lastWithdrawalDate", dateFormat.format(lotDto.getLastWithdrawalDate()));
            }
            additionalInfo.put("lotInternalId", String.valueOf(lotDto.getLotId()));

            return context.getMappingEngine().map(context.create(additionalInfo, context.getDestinationType()));
        }

    }

    private static void addLotDetailsDataMapping(final ModelMapper mapper) {
        mapper.addMappings(new PropertyMap<ExtendedLotDto, LotDetails>() {

            @Override protected void configure() {
                this.using(new AdditionalInfoConverter()).map(this.source).setAdditionalInfo(null);
                this.map().setAmount(this.source.getAvailableBalance());
                this.map().setCreatedDate(this.source.getCreatedDate());
                this.map().setGermplasmDbId(this.source.getGermplasmUUID());
                this.map().setLocationDbId(String.valueOf(this.source.getLocationId()));
                this.map().setSeedLotDescription(this.source.getNotes());
                this.map().setSeedLotName(this.source.getStockId());
                this.map().setStorageLocation(this.source.getLocationName());
                this.map().setUnits(this.source.getUnitName());
                this.map().setSeedLotDbId(this.source.getLotUUID());
            }
        });
    }
}
