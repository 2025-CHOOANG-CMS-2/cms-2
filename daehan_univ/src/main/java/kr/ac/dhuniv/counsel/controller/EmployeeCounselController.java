package kr.ac.dhuniv.counsel.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class EmployeeCounselController {

	@GetMapping("/counselor_dashboard")
	public String counselor_dashboard() {
		 
		return "employee/counselor-dashboard";
	}
	
	@GetMapping("/counselor_results")
	public String counselor_results() {
		 
		return "employee/counselor-results";
	}
	
}
