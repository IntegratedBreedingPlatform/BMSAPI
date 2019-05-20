
package org.ibp.api.java.impl.middleware.search;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.generationcp.middleware.dao.SearchRequestDAO;
import org.generationcp.middleware.domain.search_request.GermplasmSearchRequestDto;
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
		final GermplasmSearchRequestDto germplasmSearchRequestDto = new GermplasmSearchRequestDto();
		germplasmSearchRequestDto.setPreferredName("ABC");
		searchRequest.setParameters("{\"preferredName\":\"ABC\"}");
		Mockito.when(this.daoFactory.getSearchRequestDAO().save(Mockito.any())).thenReturn(searchRequest);

		final SearchRequest searchRequestResult =
			this.searchRequestServiceImpl.saveSearchRequest(germplasmSearchRequestDto, GermplasmSearchRequestDto.class);
		Assert.assertTrue(searchRequestResult.getParameters().contains(germplasmSearchRequestDto.getPreferredName()));
	}

	@Test
	public void testGetSavedSearchRequest() throws MiddlewareQueryException {
		final SearchRequest searchRequest = new SearchRequest();
		searchRequest.setParameters("{\"preferredName\":\"ABC\"}");
		Mockito.when(this.daoFactory.getSearchRequestDAO().getById(Mockito.anyInt())).thenReturn(searchRequest);

		final Integer searchResulstDbid = 1;
		final GermplasmSearchRequestDto germplasmSearchRequestDTO =
			(GermplasmSearchRequestDto) this.searchRequestServiceImpl.getSearchRequest(searchResulstDbid, GermplasmSearchRequestDto.class);

		Assert.assertTrue(germplasmSearchRequestDTO.getPreferredName().equalsIgnoreCase("ABC"));
	}

}
