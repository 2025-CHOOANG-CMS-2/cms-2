package kr.ac.dhuniv.notice.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class StudentNoticeViewController {

	@GetMapping("/students/notice")
	public String ViewStudentNotice() {
		
		
		return "/student/student-notice";
	}
	
}
