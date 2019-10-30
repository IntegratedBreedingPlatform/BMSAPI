package org.ibp.api.rest.inventory_new;

import com.beust.jcommander.internal.Lists;
import com.jayway.jsonassert.impl.matcher.IsCollectionWithSize;
import org.generationcp.middleware.domain.inventory_new.TransactionDto;
import org.generationcp.middleware.domain.inventory_new.TransactionsSearchDto;
import org.generationcp.middleware.manager.api.SearchRequestService;
import org.ibp.ApiUnitTestBase;
import org.ibp.api.java.inventory_new.TransactionService;
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

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.core.Is.is;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

public class TransactionResourceTest extends ApiUnitTestBase {

	@Autowired
	private TransactionService transactionService;

	@Autowired
	private SearchRequestService searchRequestService;

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

		final TransactionsSearchDto transactionsSearchDto = new TransactionsSearchDto();
		transactionsSearchDto.setDesignation("germplasm");
		transactionsSearchDto.setGids(Lists.newArrayList(1));
		transactionsSearchDto.setLotIds(Lists.newArrayList(2));
		transactionsSearchDto.setMaxAmount(10.0);
		transactionsSearchDto.setMinAmount(-10.0);
		transactionsSearchDto.setNotes("Deposit");
		transactionsSearchDto.setScaleIds(Lists.newArrayList(8264));
		transactionsSearchDto.setStockId("ABC-1");
		transactionsSearchDto.setTransactionIds(Lists.newArrayList(100, 200));
		transactionsSearchDto.setTransactionType("Deposit");
		transactionsSearchDto.setCreatedByUsername("admin");

		final int searchResultsDbid = 1;
		final Pageable pageable = Mockito.mock(Pageable.class);
		Mockito.when(pageable.getPageSize()).thenReturn(20);
		Mockito.when(pageable.getPageNumber()).thenReturn(0);
		Mockito.when(pageable.getSort()).thenReturn(null);

		Mockito.doReturn(transactionsSearchDto).when(this.searchRequestService).getSearchRequest(searchResultsDbid,
			TransactionsSearchDto.class);

		final List<TransactionDto> list = new ArrayList<>();
		final TransactionDto transactionDto = new TransactionDto();
		transactionDto.setAmount(10.0);
		transactionDto.getLot().setDesignation("germplasm");
		transactionDto.getLot().setGid(1);
		transactionDto.getLot().setLotId(2);
		transactionDto.setNotes("Deposit");
		transactionDto.getLot().setScaleId(8264);
		transactionDto.getLot().setScaleName("SEED_AMOUNT_g");
		transactionDto.getLot().setStockId("ABC-1");
		transactionDto.setTransactionType("Deposit");
		transactionDto.setUser("admin");
		list.add(transactionDto);

		Mockito.doReturn(list).when(this.transactionService).searchTransactions(Mockito.any(TransactionsSearchDto.class),
			Mockito.any(Pageable.class));

		this.mockMvc
			.perform(MockMvcRequestBuilders
				.get("/crops/{cropName}/transactions/search", this.cropName)
				.param("searchRequestId", "1").contentType(this.contentType))
			.andDo(MockMvcResultHandlers.print())
			.andExpect(MockMvcResultMatchers.status().isOk())
			.andExpect(MockMvcResultMatchers.jsonPath("$", IsCollectionWithSize.hasSize(list.size())))
			.andExpect(jsonPath("$[0].lotId", is(2)))
			.andExpect(jsonPath("$[0].gid", is(1)))
			.andExpect(jsonPath("$[0].designation", is("germplasm")))
			.andExpect(jsonPath("$[0].notes", is("Deposit")))
			.andExpect(jsonPath("$[0].scaleId", is(8264)))
			.andExpect(jsonPath("$[0].scaleName", is("SEED_AMOUNT_g")))
			.andExpect(jsonPath("$[0].stockId", is("ABC-1")))
			.andExpect(jsonPath("$[0].transactionType", is("Deposit")))
			.andExpect(jsonPath("$[0].user", is("admin")))
		;
	}
}
