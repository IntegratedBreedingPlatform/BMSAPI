
package org.ibp;

import javax.servlet.http.HttpSession;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import com.mangofactory.swagger.annotations.ApiIgnore;

@Controller
@RequestMapping("/")
public class WebContentController {

	/**
	 * @param model
	 * @param session
	 */
	@ApiIgnore
	@RequestMapping("/")
	public String home(Model model, HttpSession session) {
		return "index";
	}
}
