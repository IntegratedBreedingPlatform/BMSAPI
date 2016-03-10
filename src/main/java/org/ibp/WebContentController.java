
package org.ibp;

import javax.servlet.http.HttpSession;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.mangofactory.swagger.annotations.ApiIgnore;

@Controller
public class WebContentController {

	@ApiIgnore
	@RequestMapping(value = "/", method = RequestMethod.GET)
	public String home(Model model, HttpSession session) {
		return "index";
	}

	@ApiIgnore
	@RequestMapping(value = "/cooltools", method = RequestMethod.GET)
	public String coolToolsHome(Model model, HttpSession session) {
		return "cooltools/cooltools";
	}

	@ApiIgnore
	@RequestMapping(value = "/cooltools/ancestorTree/{crop}/{gid}", method = RequestMethod.GET)
	public String ancestorTree(@PathVariable String crop, @PathVariable String gid, Model model, HttpSession session) {
		model.addAttribute("crop", crop);
		model.addAttribute("gid", gid);
		return "cooltools/ancestorTree";
	}

	@ApiIgnore
	@RequestMapping(value = "/cooltools/descendantTree/{crop}/{gid}", method = RequestMethod.GET)
	public String descendantTree(@PathVariable String crop, @PathVariable String gid, Model model, HttpSession session) {
		model.addAttribute("crop", crop);
		model.addAttribute("gid", gid);
		return "cooltools/descendantTree";
	}

	@ApiIgnore
	@RequestMapping(value = "/cooltools/ancestorTreeSimple", method = RequestMethod.GET)
	public String ancestorTreeSimple(Model model, HttpSession session) {
		return "cooltools/ancestorTreeSimple";
	}

	@ApiIgnore
	@RequestMapping(value = "/cooltools/descendantTreeSimple", method = RequestMethod.GET)
	public String descendantTreeSimple(Model model, HttpSession session) {
		return "cooltools/descendantTreeSimple";
	}
}
