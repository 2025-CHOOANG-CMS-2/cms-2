package kr.ac.dhuniv.core_cpt_info;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class EmployeeCompetenciesViewController {
	
	@GetMapping("/employees/diagnosis")
	public String getEmpComtenciesPage() {
		return "/employee/admin-diagnosis-management";
	}
	@GetMapping("/employees/competency")
	public String getEmpCompetencyPage() {
		return "/employee/admin-competency-management";
	}
}
