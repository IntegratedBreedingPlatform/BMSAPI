package org.ibp;

import com.mangofactory.swagger.annotations.ApiIgnore;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpSession;

@Controller
@RequestMapping("/")
public class WebContentController {
	
    @ApiIgnore
    @RequestMapping("/")
    public String home(Model model, HttpSession session) throws MiddlewareQueryException {
        return "index";
    }
}
