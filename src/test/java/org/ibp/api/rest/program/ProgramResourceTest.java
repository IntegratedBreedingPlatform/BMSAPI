
package org.ibp.api.rest.program;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.generationcp.middleware.api.program.ProgramDTO;
import org.generationcp.middleware.pojos.workbench.CropType;
import org.generationcp.middleware.pojos.workbench.PermissionsEnum;
import org.generationcp.middleware.pojos.workbench.WorkbenchUser;
import org.generationcp.middleware.service.api.program.ProgramSearchRequest;
import org.generationcp.middleware.service.api.user.UserService;
import org.hamcrest.Matchers;
import org.hamcrest.collection.IsCollectionWithSize;
import org.ibp.ApiUnitTestBase;
import org.ibp.api.java.impl.middleware.security.SecurityServiceImpl;
import org.ibp.api.java.program.ProgramService;
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
import java.util.UUID;

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

    @Autowired
    private ProgramService programService;

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

        final List<ProgramDTO> programList = new ArrayList<>();

        final ProgramDTO program1 = new ProgramDTO("1", "fb0783d2-dc82-4db6-a36e-7554d3740092", "Program I Created", cropType.getCropName());
        program1.setMembers(Sets.newHashSet(this.me.getName()));
        program1.setCreatedBy(this.me.getName());
        program1.setStartDate("2015-11-11");

        final ProgramDTO program2 = new ProgramDTO("2", UUID.randomUUID().toString(), "Program I am member of", cropType.getCropName());
        program2.setMembers(Sets.newHashSet(this.myBreedingBuddy.getName()));
        program2.setCreatedBy(this.myBreedingBuddy.getName());
        program2.setStartDate("2015-12-12");

        programList.add(program1);
        programList.add(program2);

        Mockito.doReturn(programList).when(this.programService).listProgramsByCropName(Mockito.eq(ProgramResourceTest.CROP_NAME));
        Mockito.doReturn(this.me).when(this.userService).getUserById(Integer.valueOf(program1.getId()));
        Mockito.doReturn(this.myBreedingBuddy).when(this.userService).getUserById(Integer.valueOf(program2.getId()));

        Mockito.when(this.request.isUserInRole(PermissionsEnum.ADMIN.name())).thenReturn(true);
        Mockito.when(this.userService.getUsersByProjectId(Long.valueOf(program1.getId())))
                .thenReturn(Lists.newArrayList(this.me));

        Mockito.when(this.userService.getUsersByProjectId(Long.valueOf(program2.getId()))).thenReturn(
                Lists.newArrayList(this.me, this.myBreedingBuddy));
        verifyReturnValues(program1, program2);

        final ProgramSearchRequest programSearchRequest = new ProgramSearchRequest();
        programSearchRequest.setLoggedInUserId(me.getUserid());
        programSearchRequest.setCommonCropName(CROP_NAME);
        Mockito.doReturn(programList).when(this.programService).listProgramsByCropNameAndUser(Mockito.eq(programSearchRequest));
        Mockito.when(this.request.isUserInRole(ArgumentMatchers.anyString())).thenReturn(false);
        verifyReturnValues(program1, program2);
    }

    void verifyReturnValues(final ProgramDTO program1, final ProgramDTO program2)
        throws Exception {
        this.mockMvc.perform(MockMvcRequestBuilders.get("/crops/{cropname}/programs", ProgramResourceTest.CROP_NAME)
            .contentType(this.contentType))
                .andDo(MockMvcResultHandlers.print()).andExpect(status().isOk())
                .andExpect(jsonPath("$", IsCollectionWithSize.hasSize(2))) //
                .andExpect(jsonPath("$[0].id", Matchers.is(String.valueOf(program1.getId()))))
                .andExpect(jsonPath("$[0].uniqueID", Matchers.is(program1.getUniqueID())))
                .andExpect(jsonPath("$[0].name", Matchers.is(program1.getName())))
                .andExpect(jsonPath("$[0].members", Matchers.contains(this.me.getName())))
                .andExpect(jsonPath("$[0].crop", Matchers.is(program1.getCrop())))
                .andExpect(jsonPath("$[0].startDate", Matchers.is(program1.getStartDate())))
                .andExpect(jsonPath("$[0].createdBy", Matchers.is(this.me.getName())))

                .andExpect(jsonPath("$[1].id", Matchers.is(String.valueOf(program2.getId()))))
                .andExpect(jsonPath("$[1].uniqueID", Matchers.is(program2.getUniqueID())))
                .andExpect(jsonPath("$[1].name", Matchers.is(program2.getName())))
                .andExpect(jsonPath("$[1].members", Matchers.contains(this.myBreedingBuddy.getName())))
                .andExpect(jsonPath("$[1].crop", Matchers.is(program2.getCrop())))
                .andExpect(jsonPath("$[1].startDate", Matchers.is(program2.getStartDate())))
                .andExpect(jsonPath("$[1].createdBy", Matchers.is(this.myBreedingBuddy.getName())));
    }
}
