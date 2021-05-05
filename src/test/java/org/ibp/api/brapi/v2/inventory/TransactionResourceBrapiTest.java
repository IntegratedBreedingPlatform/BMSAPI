package org.ibp.api.brapi.v2.inventory;

import org.generationcp.middleware.domain.inventory.manager.TransactionsSearchDto;
import org.generationcp.middleware.pojos.ims.TransactionStatus;
import org.generationcp.middleware.pojos.ims.TransactionType;
import org.hamcrest.CoreMatchers;
import org.ibp.ApiUnitTestBase;
import org.ibp.api.java.inventory.manager.TransactionService;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.domain.Pageable;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

public class TransactionResourceBrapiTest extends ApiUnitTestBase {

	@Autowired
	private TransactionService transactionService;

	@Configuration
	public static class TestConfiguration {
		@Bean
		@Primary
		public TransactionService transactionService() {
			return Mockito.mock(TransactionService.class);
		}
	}

	@Before
	public void setup() throws Exception {
		super.setUp();
	}

	@Test
	public void testSearchTransactions() throws Exception {


		final Pageable pageable = Mockito.mock(Pageable.class);
		Mockito.when(pageable.getPageSize()).thenReturn(20);
		Mockito.when(pageable.getPageNumber()).thenReturn(0);
		Mockito.when(pageable.getSort()).thenReturn(null);


		final List<TransactionDto> list = new ArrayList<>();
		final TransactionDto transactionDto = new TransactionDto();
		transactionDto.setAmount(10.0);
		transactionDto.setTransactionTimestamp(new Date());
		transactionDto.setTransactionDbId("1");
		transactionDto.setTransactionDescription("comments");
		transactionDto.setUnits("SEED_AMOUNT_g");
		final Map<String, Object> additionalInfo = new HashMap<>();
		additionalInfo.put("createdByUsername", "admin");
		additionalInfo.put("transactionType", TransactionType.DEPOSIT.getValue());
		additionalInfo.put("transactionStatus", TransactionStatus.CONFIRMED.getValue());
		additionalInfo.put("seedLotID", "1de85e1c-b947-4ee2-9d19-31eee6da9ad5");
		additionalInfo.put("germplasmDbId", 1);
		additionalInfo.put("locationId", 0);
		additionalInfo.put("locationName", "UNKNOWN");
		additionalInfo.put("locationAbbr", "UNKNOWN");
		transactionDto.setAdditionalInfo(additionalInfo);
		list.add(transactionDto);

		Mockito.doReturn(list).when(this.transactionService).getTransactions(Mockito.any(TransactionsSearchDto.class),
			Mockito.any(Pageable.class));
		final SimpleDateFormat timestampFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");

		this.mockMvc
			.perform(MockMvcRequestBuilders
				.get("/{cropName}/brapi/v2/seedlots/transactions", this.cropName).contentType(this.contentType))
			.andDo(MockMvcResultHandlers.print())
			.andExpect(MockMvcResultMatchers.status().isOk())
			.andExpect(jsonPath("$.result.data[0].amount", CoreMatchers.is(transactionDto.getAmount())))
			.andExpect(jsonPath("$.result.data[0].transactionTimestamp", CoreMatchers.is(timestampFormat.format(transactionDto.getTransactionTimestamp()))))
			.andExpect(jsonPath("$.result.data[0].transactionDescription", CoreMatchers.is(transactionDto.getTransactionDescription())))
			.andExpect(jsonPath("$.result.data[0].units", CoreMatchers.is(transactionDto.getUnits())))
			.andExpect(jsonPath("$.result.data[0].transactionDbId", CoreMatchers.is(transactionDto.getTransactionDbId())))
			.andExpect(jsonPath("$.result.data[0].additionalInfo.createdByUsername", CoreMatchers.is(additionalInfo.get("createdByUsername"))))
			.andExpect(jsonPath("$.result.data[0].additionalInfo.transactionType", CoreMatchers.is(additionalInfo.get("transactionType"))))
			.andExpect(jsonPath("$.result.data[0].additionalInfo.transactionStatus", CoreMatchers.is(additionalInfo.get("transactionStatus"))))
			.andExpect(jsonPath("$.result.data[0].additionalInfo.seedLotID", CoreMatchers.is(additionalInfo.get("seedLotID"))))
			.andExpect(jsonPath("$.result.data[0].additionalInfo.germplasmDbId", CoreMatchers.is(additionalInfo.get("germplasmDbId"))))
			.andExpect(jsonPath("$.result.data[0].additionalInfo.locationId", CoreMatchers.is(additionalInfo.get("locationId"))))
			.andExpect(jsonPath("$.result.data[0].additionalInfo.locationName", CoreMatchers.is(additionalInfo.get("locationName"))))
			.andExpect(jsonPath("$.result.data[0].additionalInfo.locationAbbr", CoreMatchers.is(additionalInfo.get("locationAbbr"))));
	}

}
