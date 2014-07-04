package org.generationcp.bms.web.controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.generationcp.bms.dao.SimpleDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/web")
public class WebContentController {

	@Autowired
	private SimpleDao simpleDao;
	
    @RequestMapping("/")
    public String home(Model model) {
        
        model.addAttribute("availableCentralDBs", simpleDao.getAllCentralCropSchemaNames());
        return "index";
    }
    
    @RequestMapping(value = "/selectCrop", method = RequestMethod.POST)
    public String selectCrop(HttpServletRequest request, HttpSession session, @RequestParam String selectedCropDB) {
    	session.setAttribute("selectedCropDB", selectedCropDB);
    	return "redirect:/web/";
    }

}
