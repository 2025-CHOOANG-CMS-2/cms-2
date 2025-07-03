package kr.ac.dhuniv.admin.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AdminStdAndEmpViewController {
	
	/*
	 * 
	@GetMapping("/admin_index")
	public String AdminIndexController() {
		return "/admin/admin-index.html";
	}
	 */
	
	@GetMapping("/employees/employee-management")
	public String EmployeeManagementController() {
		return "/admin/management/employee-management.html";
	}
	
	@GetMapping("/students/student-management")
	public String StudentManagementController() {
		return "/admin/management/student-management.html";
	}
}