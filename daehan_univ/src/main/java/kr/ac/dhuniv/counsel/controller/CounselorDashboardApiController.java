package kr.ac.dhuniv.counsel.controller;

import kr.ac.dhuniv.counsel.dto.CounselingHistoryDto;
import kr.ac.dhuniv.counsel.dto.CounselingResultItemDto;
import kr.ac.dhuniv.counsel.dto.MonthlyReservationStatusDto;
import kr.ac.dhuniv.counsel.dto.PageDto;
import kr.ac.dhuniv.counsel.dto.ReservationDetailDto;
import kr.ac.dhuniv.counsel.dto.WriteResultRequestDto;
import kr.ac.dhuniv.counsel.service.CounselorDashboardService;
import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import kr.ac.dhuniv.counsel.service.CounselStudentService;

import jakarta.validation.Valid;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/counselor/dashboard")
@RequiredArgsConstructor
public class CounselorDashboardApiController {

    private final CounselorDashboardService dashboardService;
    private final CounselStudentService counselStudentService;

    @GetMapping("/reservations")
    public ResponseEntity<List<ReservationDetailDto>> getDailyReservations(
            @RequestParam("date") @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date) {
        String counselorEmplNo = "2025110002"; // 테스트용 ID ('최교수')
        List<ReservationDetailDto> reservations = dashboardService.getDailyReservations(counselorEmplNo, date);
        return ResponseEntity.ok(reservations);
    }
    
    @PatchMapping("/reservations/{applyId}/approve")
    public ResponseEntity<Void> approveReservation(@PathVariable("applyId") Long applyId) { // [수정] 이름을 명시
        String counselorEmplNo = "2025110002"; // 테스트용 ID
        dashboardService.approveReservation(applyId, counselorEmplNo);
        return ResponseEntity.ok().build();
    }
    
    @PatchMapping("/reservations/{applyId}/reject")
    public ResponseEntity<Void> rejectReservation(@PathVariable("applyId") Long applyId) { // [수정] 이름을 명시
        String counselorEmplNo = "2025110002"; // 테스트용 ID
        dashboardService.rejectReservation(applyId, counselorEmplNo);
        return ResponseEntity.ok().build();
    }
    
    // [추가] 월별 예약 현황 요약 API
    @GetMapping("/reservations/monthly-overview")
    public ResponseEntity<List<MonthlyReservationStatusDto>> getMonthlyOverview(
            @RequestParam("year") int year,
            @RequestParam("month") int month) {
        String counselorEmplNo = "2025110002"; // TODO: 실제 로그인한 상담사 ID로 교체
        List<MonthlyReservationStatusDto> statusList = dashboardService.getMonthlyStatus(counselorEmplNo, year, month);
        return ResponseEntity.ok(statusList);
    }
    
    @PostMapping("/results")
    public ResponseEntity<Void> writeCounselingResult(@Valid @RequestBody WriteResultRequestDto requestDto) {
        // 1. @Valid 어노테이션이 DTO의 유효성 검사를 자동으로 실행합니다.
        // 2. @RequestBody 어노테이션이 JSON 데이터를 DTO 객체로 변환해줍니다.
        
        // TODO: 로그인 기능 완성 후, 실제 로그인한 상담사의 ID로 교체해야 합니다.
        String counselorEmplNo = "2025110002"; 

        // 3. 서비스 계층의 로직을 호출합니다.
        dashboardService.writeCounselingResult(counselorEmplNo, requestDto);
        
        // 4. 성공 응답을 반환합니다.
        return ResponseEntity.ok().build();
    }
    
    @GetMapping("/results/{resultId}")
    public ResponseEntity<CounselingHistoryDto> getCounselingResultDetail(@PathVariable Long resultId) {
        CounselingHistoryDto resultDetail = counselStudentService.getCounselingResultDetail(resultId);
        return ResponseEntity.ok(resultDetail);
    }
    
}