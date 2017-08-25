package org.ibp.api.rest.sample;

import org.generationcp.middleware.domain.sample.SampleDTO;
import org.ibp.api.mapper.ApiMapper;
import org.modelmapper.ModelMapper;
import org.modelmapper.PropertyMap;

public class SampleMapper {

	private static ModelMapper applicationWideModelMapper = ApiMapper.getInstance();

	static {
		SampleMapper.addSampleMapper(SampleMapper.applicationWideModelMapper);
	}

	private SampleMapper() { }

	public static ModelMapper getInstance() {
		return SampleMapper.applicationWideModelMapper;
	}

	private static void addSampleMapper(ModelMapper mapper) {
		mapper.addMappings(new PropertyMap<SampleDTO, org.ibp.api.rest.sample.SampleDTO>() {

			@Override
			protected void configure() {
				this.map().setSampleName(this.source.getSampleName());
				this.map().setSampleBusinessKey(this.source.getSampleBusinessKey());
				this.map().setTakenBy(this.source.getTakenBy());
				this.map().setSamplingDate(this.source.getSamplingDate());
				this.map().setSampleList(this.source.getSampleList());
				this.map().setPlantNumber(this.source.getPlantNumber());
				this.map().setPlantBusinessKey(this.source.getPlantBusinessKey());
			}
		});
	}
}
