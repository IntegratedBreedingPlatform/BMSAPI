package org.ibp.api.mapper;

import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;


public class ApiMapper {

	private ApiMapper() {
		
	}

	static {
		applyApplicationWideMapperConfiguration(getInstance());
	}
	
	private static class ApiMapperInstaceHolder {
		
		private ApiMapperInstaceHolder() {
			
		}
		
		private static ModelMapper instance = ApiMapper.applyApplicationWideMapperConfiguration(new ModelMapper());
	}
		
	/**
	 * Mapper initialation
	 * @return ModelMapper Instance
	 */
	public static ModelMapper getInstance() {
		return ApiMapperInstaceHolder.instance;
	}
	
	private static ModelMapper applyApplicationWideMapperConfiguration(final ModelMapper mapper) {
		mapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);
		return mapper;
	}
}
