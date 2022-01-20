package org.ibp.api.domain.germplasmlist;

import org.generationcp.middleware.api.germplasmlist.GermplasmListDto;
import org.generationcp.middleware.pojos.GermplasmList;
import org.ibp.api.mapper.ApiMapper;
import org.modelmapper.ModelMapper;
import org.modelmapper.PropertyMap;

public class GermplasmListMapper {

	private static ModelMapper applicationWideModelMapper = ApiMapper.getInstance();

	private GermplasmListMapper() {

	}

	static {
		GermplasmListMapper
			.addLocationDataMapping(GermplasmListMapper.applicationWideModelMapper);
	}

	public static ModelMapper getInstance() {
		return GermplasmListMapper.applicationWideModelMapper;
	}

	private static void addLocationDataMapping(final ModelMapper mapper) {

		mapper.addMappings(new PropertyMap<GermplasmList, GermplasmListDto>() {

			@Override
			protected void configure() {

				this.map().setListId(this.source.getId());
				this.map().setListName(this.source.getName());
				this.map().setCreationDate(this.source.parseDate());
				this.map().setDescription(this.source.getDescription());
				this.map().setProgramUUID(this.source.getProgramUUID());
				this.map().setLocked(this.source.isLockedList());
				this.map().setOwnerId(this.source.getUserId());
				this.map().setNotes(this.source.getNotes());
				this.map().setListType(this.source.getType());
				this.map().setGenerationLevel(this.source.getGenerationLevel());
			}

		});
	}
}
