package kr.ac.dhuniv.std_info.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class StudentViewController {
	
	@GetMapping("/admin_index.do")
	public String AdminIndexController() {
		return "/admin/admin-index.html";
	}
	
	@GetMapping("/employee_management.do")
	public String EmployeeManagementController() {
		return "/admin/employee-management.html";
	}
	
	@GetMapping("/student_management.do")
	public String StudentManagementController() {
		return "/admin/student-management.html";
	}
}