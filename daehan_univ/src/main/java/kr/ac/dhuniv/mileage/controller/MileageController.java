package kr.ac.dhuniv.mileage.controller;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import kr.ac.dhuniv.mileage.dto.CompletedStudentDto;
import kr.ac.dhuniv.mileage.service.MileageService;
//import kr.ac.dhuniv.mileage.service.NcsCmpInfoService;
import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class MileageController {

//    private final NcsCmpInfoService ncsCmpInfoService;
	
	private final MileageService mileageService;

	@GetMapping("/student/mileage")
	public String test1() {
		return  "/student/mileage/mileage.html";
	}
	
	@GetMapping("/employee/header")
	public String test20() {
		return  "/employee/header.html";
	}
	
	@GetMapping("/employee/mileage1")
	public String test21() {
		return  "/employee/mileage/mileage-payment.html";
	}
	
	@GetMapping("/employee/mileage2")
	public String test22() {
		return  "/employee/mileage/mileage-payment2.html";
	}
	
	@GetMapping("/employee/competency1")
	public String test3() {
		return  "/employee/competency/admin-competency-management.html";
	}

	@GetMapping("/employee/competency2")
	public String test4() {
		return  "/employee/competency/admin-diagnosis-management.html";
	}
	
	@GetMapping("/employee/competency3")
	public String test5() {
		return  "/employee/competency/admin-diagnosis-result.html";
	}
	
	@GetMapping("/employee/program1")
	public String test6() {
		return  "/employee/program/application.html";
	}
	
	@GetMapping("/employee/program2")
	public String test7() {
		return  "/employee/program/completion.html";
	}
	
	@GetMapping("/employee/program3")
	public String test8() {
		return  "/employee/program/index.html";
	}
	
	//비고과 프로그램 이수자에게 마일리지 지급 데이터베이스 저장
    @PostMapping("/employee/mileage/{prgId}")
    public String payMileage(@PathVariable("prgId") String prgId) {
    	mileageService.payMileageForProgram(prgId);
    	return "/employee/mileage/mileage-payment.html";
    }
    
    @GetMapping("/completed-students")
    public String getCompletedStudents() {
    	System.out.println(mileageService.getAllCompletedStudents());
        return "/employee/mileage/mileage-payment.html";
    }
    
//    @GetMapping("/completed-students")
//    public List<CompletedStudentDto> getCompletedStudents(
//            @RequestParam(required = false) String programName,
//            @RequestParam(required = false) String studentName
//    ) {
//        return mileageService.searchCompletedStudents(programName, studentName);
//    }
}
