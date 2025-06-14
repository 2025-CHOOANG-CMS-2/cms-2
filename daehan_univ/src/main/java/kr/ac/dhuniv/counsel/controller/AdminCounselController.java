package kr.ac.dhuniv.counsel.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AdminCounselController {
	
	@GetMapping("/admin_counselor_management")
	public String admin_counselor_management() {
		
		return "/admin/admin-counselor-management";
	}
}
