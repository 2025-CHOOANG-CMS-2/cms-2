package kr.ac.dhuniv.mileage.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import kr.ac.dhuniv.mileage.service.MileageService;
import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class MileageController {
	
	private final MileageService mileageService;

	@GetMapping("/student/mileage")
	public String test1() {
		return  "/student/mileage.html";
	}
	
	@GetMapping("/employee/mileage")
	public String test2() {
		return  "/employee/mileage-payment.html";
	}

	//비고과 프로그램 이수자에게 마일리지 지급 데이터베이스 저장
    @PostMapping("/employee/mileage/{prgId}")
    public String payMileage(@PathVariable("prgId") String prgId) {
    	mileageService.payMileageForProgram(prgId);
    	return "/employee/mileage-payment.html";
    }
}
