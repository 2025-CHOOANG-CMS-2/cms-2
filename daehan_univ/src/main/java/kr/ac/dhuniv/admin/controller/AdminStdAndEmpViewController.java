package kr.ac.dhuniv.admin.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AdminStdAndEmpViewController {
	
	@GetMapping("/admin_index.do")
	public String AdminIndexController() {
		return "/admin/admin-index.html";
	}
	
	@GetMapping("/employee_management.do")
	public String EmployeeManagementController() {
		return "/admin/management/employee-management.html";
	}
	
	@GetMapping("/student_management.do")
	public String StudentManagementController() {
		return "/admin/management/student-management.html";
	}
}