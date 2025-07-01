package kr.ac.dhuniv.admin.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AdminNoticeViewController {

	@GetMapping("/admin_notice.do")
	public String AdminIndexController() {
		return "/admin/management/admin-notice-management.html";
	}
	
}
