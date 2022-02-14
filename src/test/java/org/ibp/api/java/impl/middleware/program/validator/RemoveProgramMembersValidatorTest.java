package org.ibp.api.java.impl.middleware.program.validator;

import com.google.common.collect.Sets;
import org.apache.commons.lang.math.RandomUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.generationcp.middleware.pojos.workbench.WorkbenchUser;
import org.generationcp.middleware.service.api.user.UserService;
import org.ibp.api.exception.ApiRequestValidationException;
import org.ibp.api.java.impl.middleware.security.SecurityService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static org.hamcrest.CoreMatchers.hasItem;
import static org.junit.Assert.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class RemoveProgramMembersValidatorTest {

	private final String programUUID = RandomStringUtils.randomAlphabetic(16);
	private final Integer userId = RandomUtils.nextInt();

	@Mock
	private UserService userService;

	@Mock
	private SecurityService securityService;

	@InjectMocks
	private RemoveProgramMembersValidator removeProgramMembersValidator;

	@Test
	public void testValidate_ThrowsException_WhenUserIdsIsNull() {
		try {
			removeProgramMembersValidator.validate(programUUID, null);
		} catch (final ApiRequestValidationException e) {
			assertThat(Arrays.asList(e.getErrors().get(0).getCodes()), hasItem("param.null"));
		}
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testValidate_ThrowsException_WhenUserIdsIsEmpty() {
		try {
			removeProgramMembersValidator.validate(programUUID, Collections.EMPTY_SET);
		} catch (final ApiRequestValidationException e) {
			assertThat(Arrays.asList(e.getErrors().get(0).getCodes()), hasItem("program.member.user.not.specified"));
		}
	}

	@Test
	public void testValidate_ThrowsException_WhenUserIdsContainsNullValues() {
		try {
			final Set<Integer> set = new HashSet<>();
			set.add(null);
			removeProgramMembersValidator.validate(programUUID, set);
		} catch (final ApiRequestValidationException e) {
			assertThat(Arrays.asList(e.getErrors().get(0).getCodes()), hasItem("program.member.null.user.id"));
		}
	}

	@Test
	public void testValidate_ThrowsException_WhenAnyUserDoesNotExist() {
		try {
			final Set<Integer> set = Sets.newHashSet(userId);
			Mockito.when(this.userService.getUsersByIds(Mockito.anyList())).thenReturn(Collections.emptyList());
			removeProgramMembersValidator.validate(programUUID, set);
		} catch (final ApiRequestValidationException e) {
			assertThat(Arrays.asList(e.getErrors().get(0).getCodes()), hasItem("program.member.user.invalid"));
		}
	}

	@Test
	public void testValidate_ThrowsException_WhenAnyUserDoesNotHaveProgramAccess() {
		try {
			final Set<Integer> set = Sets.newHashSet(userId);
			final WorkbenchUser user = new WorkbenchUser(userId);
			Mockito.when(this.userService.getUsersByIds(Mockito.anyList())).thenReturn(Collections.singletonList(user));
			Mockito.when(this.userService.getProgramMembers(this.programUUID, null, null)).thenReturn(Collections.emptyList());
			removeProgramMembersValidator.validate(programUUID, set);
		} catch (final ApiRequestValidationException e) {
			assertThat(Arrays.asList(e.getErrors().get(0).getCodes()), hasItem("program.member.not.removable.user.ids"));
		}
	}

}
