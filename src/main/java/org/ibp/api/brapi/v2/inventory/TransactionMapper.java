package org.ibp.api.brapi.v2.inventory;

import org.generationcp.middleware.domain.inventory.manager.TransactionDto;
import org.ibp.api.mapper.ApiMapper;
import org.modelmapper.Converter;
import org.modelmapper.ModelMapper;
import org.modelmapper.PropertyMap;
import org.modelmapper.spi.MappingContext;

import java.util.HashMap;
import java.util.Map;

public class TransactionMapper {

	private static final ModelMapper applicationWideModelMapper = ApiMapper.getInstance();

	private TransactionMapper() {

	}

	static {
		TransactionMapper.addTransactionDtoDataMapping(TransactionMapper.applicationWideModelMapper);
	}

	public static ModelMapper getInstance() {
		return TransactionMapper.applicationWideModelMapper;
	}

	private static class AdditionalInfoConverter implements Converter<TransactionDto, Map<String, Object>> {

		@Override
		public Map<String, Object> convert(final MappingContext<TransactionDto, Map<String, Object>> context) {
			final Map<String, Object> additionalInfo = new HashMap<>();
			final TransactionDto transactionDto = context.getSource();
			additionalInfo.put("createdByUsername", transactionDto.getCreatedByUsername());
			additionalInfo.put("transactionType", transactionDto.getTransactionType());
			additionalInfo.put("transactionStatus", transactionDto.getTransactionStatus());
			additionalInfo.put("seedLotID", transactionDto.getLot().getLotUUID());
			additionalInfo.put("germplasmDbId", transactionDto.getLot().getGermplasmUUID());
			additionalInfo.put("designation", transactionDto.getLot().getDesignation());
			additionalInfo.put("locationId", String.valueOf(transactionDto.getLot().getLocationId()));
			additionalInfo.put("locationName", transactionDto.getLot().getLocationName());
			additionalInfo.put("locationAbbr", transactionDto.getLot().getLocationAbbr());
			additionalInfo.put("unitId", String.valueOf(transactionDto.getLot().getUnitId()));
			additionalInfo.put("stockId", transactionDto.getLot().getStockId());
			additionalInfo.put("lotId", String.valueOf(transactionDto.getLot().getLotId()));
			additionalInfo.put("lotStatus", transactionDto.getLot().getStatus());
			additionalInfo.put("lotNotes", transactionDto.getLot().getNotes());

			return context.getMappingEngine().map(context.create(additionalInfo, context.getDestinationType()));
		}

	}

	private static void addTransactionDtoDataMapping(final ModelMapper mapper) {
		mapper.addMappings(new PropertyMap<TransactionDto, org.ibp.api.brapi.v2.inventory.TransactionDto>() {

			@Override
			protected void configure() {
				this.using(new AdditionalInfoConverter()).map(this.source).setAdditionalInfo(null);
				this.map().setAmount(this.source.getAmount());
				this.map().setUnits(this.source.getLot().getUnitName());
				this.map().setTransactionDescription(this.source.getNotes());
				this.map().setTransactionTimestamp(this.source.getCreatedDate());
				this.map().setTransactionDbId(this.source.getTransactionId().toString());
			}
		});
	}

}
