package kr.ac.dhuniv.counsel.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class StudentCounselController {
	
	@GetMapping("/counsel_history2")
	public String counsel_history2() {
		
		return "/student/counsel-history2";
	}
	
	@GetMapping("/counsel_book2")
	public String counsel_book2() {
		
		return "/student/counsel-book2";
	}
	
	@GetMapping("/counsel_book")
	public String counsel_book() {
		
		return "/student/counsel-book";
	}
	
	@GetMapping("/counsel_history")
	public String counsel_history() {
		
		return "/student/counsel-history";
	}
	
}
