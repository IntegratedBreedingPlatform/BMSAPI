
package org.ibp.api.java.impl.middleware.user;

import java.util.List;

import org.generationcp.middleware.manager.api.WorkbenchDataManager;
import org.generationcp.middleware.service.api.user.UserDto;
import org.ibp.ApiUnitTestBase;
import org.ibp.api.java.user.UserData;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import com.google.common.collect.Lists;

public class UserServiceImplTest extends ApiUnitTestBase {

	@Test
	public void testGetAllUsers() {
		WorkbenchDataManager workbenchDataManager = Mockito.mock(WorkbenchDataManager.class);
		UserServiceImpl userService = new UserServiceImpl();
		userService.setWorkbenchDataManager(this.workbenchDataManager);

		List<UserDto> usersList = Lists.newArrayList(new UserDto("username0"));
		Mockito.when(workbenchDataManager.getAllUserDtosSorted()).thenReturn(usersList);

		List<UserData> userDataList = userService.getAllUserDtosSorted();

		Assert.assertEquals(usersList.size(), userDataList.size());
		UserData user0 = userDataList.get(0);

		Assert.assertNotNull(user0);

		Assert.assertEquals(user0.getUsername(), "username0");

	}

}
