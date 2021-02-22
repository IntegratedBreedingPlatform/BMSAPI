
package org.ibp.api.rest.program;

import com.google.common.collect.Lists;
import org.generationcp.middleware.pojos.workbench.CropType;
import org.generationcp.middleware.pojos.workbench.PermissionsEnum;
import org.generationcp.middleware.pojos.workbench.Project;
import org.generationcp.middleware.pojos.workbench.WorkbenchUser;
import org.generationcp.middleware.service.api.program.ProgramSearchRequest;
import org.generationcp.middleware.service.api.user.UserService;
import org.hamcrest.Matchers;
import org.hamcrest.collection.IsCollectionWithSize;
import org.ibp.ApiUnitTestBase;
import org.ibp.api.java.impl.middleware.program.ProgramServiceImpl;
import org.ibp.api.java.impl.middleware.security.SecurityServiceImpl;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class ProgramResourceTest extends ApiUnitTestBase {

    private WorkbenchUser me;
    private WorkbenchUser myBreedingBuddy;

    private static String CROP_NAME ="MAIZE";
    @Autowired
    private SecurityServiceImpl securityService;

    @Autowired
    private UserService userService;

    @Autowired
    private HttpServletRequest request;

    @Before
    public void beforeEachTest() {

        this.me = new WorkbenchUser();
        this.me.setName("Mr. Breeder");
        this.me.setUserid(1);
        this.me.setPassword("password");

        this.myBreedingBuddy = new WorkbenchUser();
        this.myBreedingBuddy.setName("My Breeding Buddy");
        this.myBreedingBuddy.setUserid(2);
        this.myBreedingBuddy.setPassword("password");

        Mockito.when(this.userService.getUserById(this.me.getUserid())).thenReturn(this.me);
        Mockito.when(this.userService.getUserById(this.myBreedingBuddy.getUserid())).thenReturn(this.myBreedingBuddy);

        Mockito.when(this.userService.getUserByUsername(this.me.getName())).thenReturn(this.me);
        Mockito.when(this.userService.getUserByUsername(this.myBreedingBuddy.getName())).thenReturn(this.myBreedingBuddy);
        Mockito.when(this.securityService.getCurrentlyLoggedInUser()).thenReturn(this.me);

    }

    @Test
    public void listProgramsByCropName() throws Exception {
        final CropType cropType = new CropType();
        cropType.setCropName(ProgramResourceTest.CROP_NAME);

        final List<Project> programList = new ArrayList<>();
        final Project program1 =
                new Project(1L, "fb0783d2-dc82-4db6-a36e-7554d3740092", "Program I Created", null,
                        this.me.getUserid(), cropType, null);

        final String program1Date = "2015-11-11";
        program1.setStartDate(ProgramServiceImpl.DATE_FORMAT.parse(program1Date));

        final Project program2 =
                new Project(2L, "57b8f271-56db-448e-ad8d-528ac4d80f04", "Program I am member of", null, this.myBreedingBuddy.getUserid(),
                        cropType, null);

        final String program2Date = "2015-12-12";
        program2.setStartDate(ProgramServiceImpl.DATE_FORMAT.parse(program2Date));

        programList.add(program1);
        programList.add(program2);

        Mockito.doReturn(programList).when(this.workbenchDataManager).getProjectsByCropName(Mockito.eq(ProgramResourceTest.CROP_NAME));
        Mockito.doReturn(this.me).when(this.userService).getUserById(program1.getProjectId().intValue());
        Mockito.doReturn(this.myBreedingBuddy).when(this.userService).getUserById(program2.getProjectId().intValue());

        Mockito.when(this.request.isUserInRole(PermissionsEnum.ADMIN.name())).thenReturn(true);
        Mockito.when(this.userService.getUsersByProjectId(program1.getProjectId()))
                .thenReturn(Lists.newArrayList(this.me));

        Mockito.when(this.userService.getUsersByProjectId(program2.getProjectId())).thenReturn(
                Lists.newArrayList(this.me, this.myBreedingBuddy));
        verifyReturnValues(program1, program1Date, program2, program2Date);

        final ProgramSearchRequest programSearchRequest = new ProgramSearchRequest();
        programSearchRequest.setLoggedInUserId(me.getUserid());
        programSearchRequest.setCommonCropName(CROP_NAME);
        Mockito.doReturn(programList).when(this.workbenchDataManager).getProjects(null, Mockito.eq(programSearchRequest));
        Mockito.when(this.request.isUserInRole(ArgumentMatchers.anyString())).thenReturn(false);
        verifyReturnValues(program1, program1Date, program2, program2Date);
    }

    void verifyReturnValues(final Project program1, final String program1Date, final Project program2, final String program2Date)
        throws Exception {
        this.mockMvc.perform(MockMvcRequestBuilders.get("/crops/{cropname}/programs", ProgramResourceTest.CROP_NAME)
            .contentType(this.contentType))
                .andDo(MockMvcResultHandlers.print()).andExpect(status().isOk())
                .andExpect(jsonPath("$", IsCollectionWithSize.hasSize(2))) //
                .andExpect(jsonPath("$[0].id", Matchers.is(String.valueOf(program1.getProjectId()))))
                .andExpect(jsonPath("$[0].uniqueID", Matchers.is(program1.getUniqueID())))
                .andExpect(jsonPath("$[0].name", Matchers.is(program1.getProjectName())))
                .andExpect(jsonPath("$[0].members", Matchers.contains(this.me.getName())))
                .andExpect(jsonPath("$[0].crop", Matchers.is(program1.getCropType().getCropName())))
                .andExpect(jsonPath("$[0].startDate", Matchers.is(program1Date)))
                .andExpect(jsonPath("$[0].createdBy", Matchers.is(this.me.getName())))

                .andExpect(jsonPath("$[1].id", Matchers.is(String.valueOf(program2.getProjectId()))))
                .andExpect(jsonPath("$[1].uniqueID", Matchers.is(program2.getUniqueID())))
                .andExpect(jsonPath("$[1].name", Matchers.is(program2.getProjectName())))
                .andExpect(jsonPath("$[1].members", Matchers.contains(this.me.getName(), this.myBreedingBuddy.getName())))
                .andExpect(jsonPath("$[1].crop", Matchers.is(program2.getCropType().getCropName())))
                .andExpect(jsonPath("$[1].startDate", Matchers.is(program2Date)))
                .andExpect(jsonPath("$[1].createdBy", Matchers.is(this.myBreedingBuddy.getName())));
    }
}
