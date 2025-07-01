package kr.ac.dhuniv.mileage.controller;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import jakarta.servlet.http.HttpSession;
import kr.ac.dhuniv.mileage.dto.CoreCptForMlgDTO;
import kr.ac.dhuniv.mileage.service.CommonMileageService;
import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class StudentMileageController {

	private final CommonMileageService commonService;
	    
    @GetMapping("/student/mileage")
    public String showStudentMileage(HttpSession session, Model m) {
    	String userId = "2025004001";
    	session.setAttribute("userId", userId);
		List<CoreCptForMlgDTO> coreCompetencies = commonService.getAllCoreCompetencies();
		m.addAttribute("coreCompetencies", coreCompetencies);  // 핵심역량 목록
		
    	return "/student/mileage/mileage.html";
    }
    
    @GetMapping("/student")
    public String student() {
    	return "/student/header.html";
    }
    
    @GetMapping("/student/mypage")
    public String showMypage() {
    	return "/mypage/student-mypage.html";
    }
}
