package kr.ac.dhuniv.core_cpt.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class CoreCptViewController {

	@GetMapping("/employees/core-empl-cpt")
	public String getEmpCorePage() {
		return "employee/competency/admin-competency-management";
			
	}
	@GetMapping("/employees/core-diagnosis-cpt")
	public String getEmpDiagnosisPage() {
		return "employee/competency/admin-diagnosis-management";
			
	}
	@GetMapping("/employees/core-diagnosis-result")
	public String getEmpCoreResultPage() {
		return "employee/competency/admin-diagnosis-result";
	}
	
	@GetMapping("/students/competency-diagnosis")
	public String getStudentCompetencyPage() {
		return "student/competency/competency-diagnosis";
	}
	@GetMapping("/students/history")
	public String getStudentCompetencyHistoryPage() {
		return "student/competency/diagnosis-history";
	}
}
