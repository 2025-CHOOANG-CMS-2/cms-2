package kr.ac.dhuniv.mileage.controller;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import kr.ac.dhuniv.mileage.dto.CoreCptForMlgDTO;
import kr.ac.dhuniv.mileage.dto.employee.NcsPrgForMlgDTO;
import kr.ac.dhuniv.mileage.service.EmployeeMileageService;
import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class EmployeeMileageController {
	
	private final EmployeeMileageService mileageService;

	// 마일리지 지급 화면 (핵심역량목록, 비교과프로그램목록은 검색 옵션에 사용)
	@GetMapping("/employee/mileage/payments")
	public String processMileagePayment(Model m) {
		List<CoreCptForMlgDTO> coreCompetencies = mileageService.getAllCoreCompetencies();
		List<NcsPrgForMlgDTO> ncsPrograms = mileageService.getAllNcsPrograms();
		m.addAttribute("coreCompetencies", coreCompetencies);  // 핵심역량 목록
		m.addAttribute("ncsPrograms", ncsPrograms);            // 비교과 프로그램 목록
		return  "/employee/mileage/mileage-payment.html";
	}

}
