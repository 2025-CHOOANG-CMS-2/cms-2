package kr.ac.dhuniv.counsel.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class counselController {
	
	@GetMapping("/admin_counselor_management")
	public String admin_counselor_management() { //상담사 등록 페이지
		
		return "admin/counsel/admin-counselor-management";
	}
	
	@GetMapping("/counselor_dashboard") //상담 예약 확인 페이지
	public String counselor_dashboard() {
		 
		return "employee/counsel/counselor-dashboard";
	}
	
	@GetMapping("/counselor_results") //상담사 결과 작성 페이지
	public String counselor_results() {
		 
		return "employee/counsel/counselor-results";
	}
	
	@GetMapping("/counsel_book") //상담 예약 페이지
	public String counsel_book() {
		
		return "student/counsel/counsel-book";
	}
	
	@GetMapping("/counsel_history") //상담 예약 조회 페이지
	public String counsel_history() {
		
		return "student/counsel/counsel-history";
	}
}
