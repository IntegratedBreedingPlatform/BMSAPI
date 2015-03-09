package org.generationcp.bms.web.controller;

import com.mangofactory.swagger.annotations.ApiIgnore;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.generationcp.bms.Constants;
import org.generationcp.bms.dao.SimpleDao;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.api.WorkbenchDataManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/")
public class WebContentController {
	
	private final Logger LOGGER = LoggerFactory.getLogger(WebContentController.class); 

	@Autowired
	private SimpleDao simpleDao;
	
	@Autowired
	private WorkbenchDataManager workbenchDataManager;
	
    @ApiIgnore
    @RequestMapping("/")
    public String home(Model model, HttpSession session) throws MiddlewareQueryException {
        return "index";
    }

    @ApiIgnore
    @RequestMapping("/browse")
    public String browse(Model model, HttpServletRequest request) {       
        model.addAttribute("availableCentralDBs", simpleDao.getAllCentralCropSchemaNames());
        return "browse";
    }
    
    @RequestMapping(value = "/selectCentralCropDB", method = RequestMethod.POST)
    @ApiIgnore
    public String selectCentralCropDB(HttpServletRequest request, HttpSession session, @RequestParam String selectedCentralCropDB, @RequestParam String redirectURL) {
    	session.setAttribute(Constants.PARAM_SELECTED_CENTRAL_CROP_DB, selectedCentralCropDB);
    	LOGGER.debug("Selected central crop DB is: " + selectedCentralCropDB);
    	return "redirect:" + redirectURL;
    }
}
