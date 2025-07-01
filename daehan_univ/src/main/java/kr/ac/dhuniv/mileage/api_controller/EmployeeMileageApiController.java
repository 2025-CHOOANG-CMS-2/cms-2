package kr.ac.dhuniv.mileage.api_controller;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.format.annotation.DateTimeFormat.ISO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import kr.ac.dhuniv.mileage.dto.employee.CompletedProgramDTO;
import kr.ac.dhuniv.mileage.dto.employee.CompletedStudentDTO;
import kr.ac.dhuniv.mileage.dto.employee.DashboardStatsDTO;
import kr.ac.dhuniv.mileage.dto.employee.MileagePaymentReqDTO;
import kr.ac.dhuniv.mileage.dto.employee.PaymentHistoryDTO;
import kr.ac.dhuniv.mileage.dto.employee.ProgramTopDTO;
import kr.ac.dhuniv.mileage.dto.employee.RecentActivityDTO;
import kr.ac.dhuniv.mileage.service.EmployeeMileageService;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/employee/mileage")
@RequiredArgsConstructor
public class EmployeeMileageApiController {

	private final EmployeeMileageService mileageService;
	
	// *** 대시보드 ***
	
	// 금학기 총 지급 마일리지(지급취소 제외), 마일리지 보유 학생수(재학생 기준, 현재 보유기준), 금학기 활성 프로그램수, 학생당 평균 마일리지(재학생 기준, 현재 보유기준)
    @GetMapping("/stats")
    public DashboardStatsDTO stats() {
        return mileageService.getStats();
    }

	// 금학기 상위 5개 프로그램 조회 (지급된 마일리지합계 기준 내림차순, 총지급 마일리지에서 지급취소는 제외)
    @GetMapping("/top-programs")
    public List<ProgramTopDTO> topPrograms() {
        Pageable top5 = PageRequest.of(0, 5);
        return mileageService.getTop5Programs(top5);
    }

    // 최근 마일리지 지급 활동 내역 조회 (최근 10건, 지급한 총 마일리지에서 지급취소는 제외)
    @GetMapping("/recent-activities")
    public List<RecentActivityDTO> recentActivities() {
    	Pageable latest10 = PageRequest.of(0, 10); 
        return mileageService.getRecentActivities(latest10);
    }
    
    // *** 프로그램 연동 마일리지 지급 ***
    
    // 이수완료 되었지만 마일리지가 지급되지 않은 프로그램 목록 조회
    @GetMapping("/unpaid-programs")
    public List<CompletedProgramDTO> getUnpaidPrograms() {
        return mileageService.findCompletedButUnpaidPrograms();
    }
    
    // 해당 프로그램 이수자 목록 조회
    @GetMapping("unpaid-students")
    public List<CompletedStudentDTO> getStudentsByCompletedProgram(
    		@RequestParam(name="programId") Long programId,
    		@RequestParam(name="completeDate") LocalDateTime completeDate){
    	return mileageService.findCompletedStudents(programId, completeDate);
    }
    
    // 마일리지 지급 (학생마일리지 총점 갱신, 학생마일리지 점수 이력 추가)
    @PostMapping("/payment")
    public ResponseEntity<Void> payMileage(@RequestBody List<MileagePaymentReqDTO> requests) {
        mileageService.saveMileagePayments(requests);
        return ResponseEntity.ok().build();
    }
    
    // *** 프로그램 연동 마일리지 지급(화면 구성이 다름) ***
    
	// 프로그램 이수자 중 마일리지 미지급자 목록 조회(전체목록 또는 검색목록)
    @GetMapping("/completed-students")
    public List<CompletedStudentDTO> getCompletedStudents(
            @RequestParam(name="startDate", required = false) LocalDate startDate,
            @RequestParam(name="endDate", required = false) LocalDate endDate,
            @RequestParam(name="competencyId", required = false) Long competencyId,
            @RequestParam(name="programId", required = false) Long programId) {
    	
    	// LocalDateTime으로 변환
        LocalDateTime startDateTime = startDate != null ? startDate.atStartOfDay() : null;  //00:00:00
        LocalDateTime endDateTime = endDate != null ? endDate.atTime(LocalTime.MAX) : null;  //23:59:59.999999999
        System.out.println(mileageService.getCompletedStudents(startDateTime, endDateTime, competencyId, programId));
        return mileageService.getCompletedStudents(startDateTime, endDateTime, competencyId, programId);
    }
    
    // *** 마일리지 지급내역 조회 및 지급 취소 ***
    
    // 미일리지 지급 내역 출력 (검색 및 페이징 적용)
    @GetMapping("/payment-history")
    public Page<PaymentHistoryDTO> getPaymentHistory(
        @RequestParam(name="page", defaultValue = "0") int page,
        @RequestParam(name="size", defaultValue = "1") int size,
        @RequestParam(name="startDate", required = false) LocalDate startDate,
        @RequestParam(name="endDate", required = false) LocalDate endDate,
        @RequestParam(name="competencyId", required = false) Long competencyId,
        @RequestParam(name="programId", required = false) Long programId,
        @RequestParam(name="mlgStatCode", required = false) String mlgStatCode
    ) {
    	System.out.println(page);
    	System.out.println(size);
    	System.out.println(startDate);
    	System.out.println(endDate);
    	System.out.println(competencyId);
    	System.out.println(programId);
    	System.out.println(mlgStatCode);
        // LocalDateTime으로 변환
        LocalDateTime startDateTime = startDate != null ? startDate.atStartOfDay() : null;  //00:00:00
        LocalDateTime endDateTime = endDate != null ? endDate.atTime(LocalTime.MAX) : null;  //23:59:59.999999999
        Pageable pageable = PageRequest.of(page, size);
        return mileageService.getHistory(startDateTime, endDateTime, competencyId, programId, mlgStatCode, pageable);
    }
    
    // 마일리지 단건 취소
    @PostMapping("/cancel-payment/{id}")
    public ResponseEntity<Void> cancel(@PathVariable(name="id") Long id) {
    	mileageService.cancelPayment(id);
        return ResponseEntity.ok().build();
    }
    
    // 마일리지 복수 취소
    @PostMapping("/cancel-payment")
    public ResponseEntity<Void> bulkCancel(@RequestBody List<Long> ids) {
    	mileageService.bulkCancel(ids);
        return ResponseEntity.ok().build();
    }
    
}