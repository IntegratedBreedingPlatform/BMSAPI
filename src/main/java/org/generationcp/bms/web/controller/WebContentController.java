package org.generationcp.bms.web.controller;

import javax.servlet.http.HttpSession;

import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import com.mangofactory.swagger.annotations.ApiIgnore;

@Controller
@RequestMapping("/")
public class WebContentController {
	
    @ApiIgnore
    @RequestMapping("/")
    public String home(Model model, HttpSession session) throws MiddlewareQueryException {
        return "index";
    }
}
