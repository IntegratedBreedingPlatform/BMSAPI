package org.ibp.api.brapi.v1.study;

import org.generationcp.middleware.service.api.user.UserDto;
import org.modelmapper.Converter;
import org.modelmapper.spi.MappingContext;

import java.util.ArrayList;
import java.util.List;

public class ContactConverter implements Converter<List<UserDto>, List<Contact>> {

	@Override
	public List<Contact> convert(final MappingContext<List<UserDto>, List<Contact>> context) {
		final List<Contact> contacts = new ArrayList<>();
		for (final UserDto userDto : context.getSource()) {
			contacts.add(new Contact(userDto.getUserId(), userDto.getEmail(), userDto.getFirstName() + " " + userDto.getLastName(),
				"", ""));
		}
		return context.getMappingEngine().map(context.create(contacts, context.getDestinationType()));
	}

}
