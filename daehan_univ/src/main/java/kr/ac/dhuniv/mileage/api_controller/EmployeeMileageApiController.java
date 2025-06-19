package kr.ac.dhuniv.mileage.api_controller;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import kr.ac.dhuniv.mileage.dto.CompletedStudentDto;
import kr.ac.dhuniv.mileage.dto.MileagePaymentRequest;
import kr.ac.dhuniv.mileage.service.EmployeeMileageService;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/employee/mileage")
@RequiredArgsConstructor
public class EmployeeMileageApiController {

	private final EmployeeMileageService mileageService;
    
	// 프로그램 이수자 중 마일리지 미지급자 목록 조회(전체목록 또는 검색목록)
    @GetMapping("/completed-students")
    public List<CompletedStudentDto> getCompletedStudents(
            @RequestParam(name="startDate", required = false) LocalDate startDate,
            @RequestParam(name="endDate", required = false) LocalDate endDate,
            @RequestParam(name="coreCompetency", required = false) Long coreCompetency,
            @RequestParam(name="programId", required = false) Long programId) {
    	
    	// LocalDateTime으로 변환
        LocalDateTime startDateTime = startDate != null ? startDate.atStartOfDay() : null;  //00:00:00
        LocalDateTime endDateTime = endDate != null ? endDate.atTime(LocalTime.MAX) : null;  //23:59:59.999999999
        System.out.println(mileageService.getCompletedStudents(startDateTime, endDateTime, coreCompetency, programId));
        return mileageService.getCompletedStudents(startDateTime, endDateTime, coreCompetency, programId);
    }
    
    @PostMapping("/payment")
    public ResponseEntity<Void> payMileage(@RequestBody List<MileagePaymentRequest> requests) {
    	System.out.println("테스트: " + requests);
        mileageService.saveMileagePayments(requests);
        return ResponseEntity.ok().build();
    }
    
}