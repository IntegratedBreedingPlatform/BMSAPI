
package org.ibp.api.java.impl.middleware.search;

import com.beust.jcommander.internal.Lists;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.generationcp.middleware.dao.SearchRequestDAO;
import org.generationcp.middleware.domain.search_request.brapi.v2.GermplasmSearchRequest;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.hibernate.HibernateSessionProvider;
import org.generationcp.middleware.manager.DaoFactory;
import org.generationcp.middleware.manager.SearchRequestServiceImpl;
import org.generationcp.middleware.pojos.search.SearchRequest;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

public class SearchRequestServiceImplTest {

	@InjectMocks
	private SearchRequestServiceImpl searchRequestServiceImpl;

	@Mock
	private HibernateSessionProvider sessionProvider;

	@Mock
	private DaoFactory daoFactory;

	@Mock
	private ObjectMapper jacksonMapper;

	@Mock
	private SearchRequestDAO searchRequestDAO;

	@Before
	public void before() {
		MockitoAnnotations.initMocks(this);
		this.jacksonMapper = new ObjectMapper();
		this.searchRequestServiceImpl = new SearchRequestServiceImpl(this.sessionProvider);
		this.searchRequestServiceImpl.setDaoFactory(this.daoFactory);
		this.searchRequestServiceImpl.setJacksonMapper(this.jacksonMapper);
		Mockito.when(this.daoFactory.getSearchRequestDAO()).thenReturn(this.searchRequestDAO);
	}

	@Test
	public void testSaveSearchRequest() throws MiddlewareQueryException {
		final SearchRequest searchRequest = new SearchRequest();
		final GermplasmSearchRequest germplasmSearchRequest = new GermplasmSearchRequest();
		germplasmSearchRequest.setGermplasmNames(Lists.newArrayList("ABC", "DEF"));
		searchRequest.setParameters("{\"germplasmNames\":[\"ABC\",\"DEF\"]}");
		Mockito.when(this.daoFactory.getSearchRequestDAO().save(Mockito.any())).thenReturn(searchRequest);

		final Integer searchRequestResult =
			this.searchRequestServiceImpl.saveSearchRequest(germplasmSearchRequest, GermplasmSearchRequest.class);

		Mockito.when(this.daoFactory.getSearchRequestDAO().save(searchRequest).getRequestId()).thenReturn(1);
		Mockito.when(this.daoFactory.getSearchRequestDAO().getById(searchRequestResult)).thenReturn(searchRequest);

		final GermplasmSearchRequest dto =
			(GermplasmSearchRequest) this.searchRequestServiceImpl
				.getSearchRequest(searchRequestResult, GermplasmSearchRequest.class);
		Assert.assertEquals(dto.getGermplasmNames(), germplasmSearchRequest.getGermplasmNames());
	}

	@Test
	public void testGetSavedSearchRequest() throws MiddlewareQueryException {
		final SearchRequest searchRequest = new SearchRequest();
		searchRequest.setParameters("{\"germplasmNames\":[\"ABC\",\"DEF\"]}");
		Mockito.when(this.daoFactory.getSearchRequestDAO().getById(Mockito.anyInt())).thenReturn(searchRequest);

		final Integer searchResulstDbid = 1;
		final GermplasmSearchRequest germplasmSearchRequestDTO =
			(GermplasmSearchRequest) this.searchRequestServiceImpl.getSearchRequest(searchResulstDbid, GermplasmSearchRequest.class);

		Assert.assertTrue(germplasmSearchRequestDTO.getGermplasmNames().contains("ABC"));
		Assert.assertTrue(germplasmSearchRequestDTO.getGermplasmNames().contains("DEF"));
	}

}
