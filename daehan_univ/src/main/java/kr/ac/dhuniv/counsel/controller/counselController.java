package kr.ac.dhuniv.counsel.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class counselController {
	
	@GetMapping("/admins/counselor-management")
	public String admin_counselor_management() { //상담사 등록 페이지
		
		return "admin/counsel/admin-counselor-management";
	}
	
	@GetMapping("/employees/counselor-dashboard") //상담 예약 확인 페이지
	public String counselor_dashboard() {
		 
		return "employee/counsel/counselor-dashboard";
	}
	
	@GetMapping("/employees/counselor-results") //상담사 결과 작성 페이지
	public String counselor_results() {
		 
		return "employee/counsel/counselor-results";
	}
	
	@GetMapping("/students/counsel-book") //상담 예약 페이지
	public String counsel_book() {
		
		return "student/counsel/counsel-book";
	}
	
	@GetMapping("/students/counsel-history") //상담 예약 조회 페이지
	public String counsel_history() {
		
		return "student/counsel/counsel-history";
	}
}
