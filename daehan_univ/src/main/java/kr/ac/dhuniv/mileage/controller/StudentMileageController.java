package kr.ac.dhuniv.mileage.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import jakarta.servlet.http.HttpSession;
import kr.ac.dhuniv.mileage.service.StudentMileageService;
import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class StudentMileageController {

	private final StudentMileageService cmpInfoService;
	
	
    
    @GetMapping("/student/mileage")
    public String showStudentMileage(HttpSession session) {
    	String userId = "2025004001";
    	session.setAttribute("userId", userId);
    	return "/student/mileage/mileage.html";
    }
    
    @GetMapping("/student")
    public String student() {
    	return "/student/header.html";
    }
}
