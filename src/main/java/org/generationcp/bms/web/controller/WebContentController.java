package org.generationcp.bms.web.controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.generationcp.bms.dao.SimpleDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.mangofactory.swagger.annotations.ApiIgnore;

@Controller
@RequestMapping("/")
public class WebContentController {
	
	private Logger LOGGER = LoggerFactory.getLogger(WebContentController.class); 

	@Autowired
	private SimpleDao simpleDao;
	
	/**
	 * The home page.
	 */
    @ApiIgnore
    @RequestMapping("/")
    public String home(Model model) {
        
        model.addAttribute("availableCentralDBs", simpleDao.getAllCentralCropSchemaNames());
        return "index";
    }
    
    @RequestMapping(value = "/selectCrop", method = RequestMethod.POST)
    @ApiIgnore
    public String selectCrop(HttpServletRequest request, HttpSession session, @RequestParam String selectedCropDB) {
    	session.setAttribute("selectedCropDB", selectedCropDB);
    	LOGGER.info("Selected crop DB is: " + selectedCropDB);
    	return "redirect:/";
    }

}
