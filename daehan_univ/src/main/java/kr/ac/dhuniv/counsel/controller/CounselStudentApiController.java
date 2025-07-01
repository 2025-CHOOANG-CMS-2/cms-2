package kr.ac.dhuniv.counsel.controller;

import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import kr.ac.dhuniv.counsel.dto.AvailableSlotsDto;
import kr.ac.dhuniv.counsel.dto.CounselingHistoryDto;
import kr.ac.dhuniv.counsel.dto.CounselingResultItemDto;
import kr.ac.dhuniv.counsel.dto.CounselorSimpleDto;
import kr.ac.dhuniv.counsel.dto.CreateReservationRequestDto;
import kr.ac.dhuniv.counsel.dto.PageDto;
import kr.ac.dhuniv.counsel.dto.SatisfactionRequestDto;
import kr.ac.dhuniv.counsel.dto.UpdateReservationStatusDto;
import kr.ac.dhuniv.counsel.service.CounselStudentService;
import kr.ac.dhuniv.counsel.service.CounselorService;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/counseling")  //학생용 API 경로
@RequiredArgsConstructor
public class CounselStudentApiController {

    private final CounselStudentService counselStudentService;
    
     //상담 유형으로 상담사 목록 조회 API
    @GetMapping("/counselors")
    public ResponseEntity<List<CounselorSimpleDto>> getCounselorsByType(@RequestParam("type") String counselingType) {
        List<CounselorSimpleDto> counselors = counselStudentService.findCounselorsByType(counselingType);
        return ResponseEntity.ok(counselors);
    }
    
     //월간 예약 가능 시간 조회 API (이 버전만 남겨둡니다)
    @GetMapping("/available-slots")
    public ResponseEntity<Map<String, List<AvailableSlotsDto>>> getAvailableSlotsForMonth(
            @RequestParam("year") Integer year,
            @RequestParam("month") Integer month,
            @RequestParam(value = "type", required = false, defaultValue = "all") String counselingType,
            @RequestParam(value = "counselorId", required = false, defaultValue = "all") String counselorId) {
        
        Map<String, List<AvailableSlotsDto>> availableSlots = counselStudentService.getAvailableSlotsForMonth(year, month, counselingType, counselorId);
        return ResponseEntity.ok(availableSlots);
    }

    @PostMapping("/reservations")
    public ResponseEntity<Void> createReservation(@Valid @RequestBody CreateReservationRequestDto requestDto, Authentication authentication) {
       //String loggedInStudentId = authentication.getName();
    	String loggedInStudentId = "2025004001";
        requestDto.setStdNo(loggedInStudentId); // DTO에 현재 로그인한 학생 ID 설정
        counselStudentService.createReservation(requestDto);
        return ResponseEntity.ok().build();
    }
    
    @GetMapping("/history")
    public ResponseEntity<PageDto<CounselingHistoryDto>> getMyCounselingHistory(
            @RequestParam(value = "period", defaultValue = "all") String period,
            @RequestParam(value = "status", defaultValue = "all") String status,
            @RequestParam(value = "type", defaultValue = "all") String type,
            // Pageable 파라미터를 추가하여 페이징 정보를 받습니다.
            @PageableDefault(size = 10, sort = "applyDateTime", direction = Sort.Direction.DESC) Pageable pageable,
            Authentication authentication) {
        
        // TODO: 로그인 기능 완성 후, 실제 로그인한 학생의 학번으로 교체 필요
        //String studentId = authentication.getName();
    	String studentId = "2025004001";
        
        // 서비스 메소드에 pageable 객체를 그대로 전달합니다.
        PageDto<CounselingHistoryDto> pagedHistory = counselStudentService.getCounselingHistory(studentId, pageable, period, status, type);
        return ResponseEntity.ok(pagedHistory);
    }
    
    @PatchMapping("/reservations/{applyId}/status")
    public ResponseEntity<Void> updateReservationStatus(
            @PathVariable("applyId") Long applyId,
            @Valid @RequestBody UpdateReservationStatusDto statusDto,
            Authentication authentication) {
        
        // TODO: 로그인 기능 완성 후, 실제 로그인한 학생 ID로 교체 필요
        //String studentId = authentication.getName();
    	String studentId = "2025004001";

        counselStudentService.updateReservationStatus(studentId, applyId, statusDto.getStatus());
        return ResponseEntity.ok().build();
    }
    
    @GetMapping("/results/{resultId}") // <-- 이 주소가 정확히 "/results/{resultId}" 인지 확인!
    public ResponseEntity<CounselingHistoryDto> getCounselingResultDetail(@PathVariable("resultId") Long resultId) {
        CounselingHistoryDto resultDetail = counselStudentService.getCounselingResultDetail(resultId);
        return ResponseEntity.ok(resultDetail);
    }
    
    @PutMapping("/results/{resultId}/satisfaction")
    public ResponseEntity<Void> updateSatisfaction(
            @PathVariable("resultId") Long resultId,
            @RequestBody SatisfactionRequestDto requestDto,
            Authentication authentication) {

        //String studentId = authentication.getName();
    	String studentId = "2025004001";

        // 서비스 계층에 권한 검사 로직 위임 (더 나은 설계)
        counselStudentService.updateSatisfactionScore(resultId, requestDto.getScore(), studentId);

        return ResponseEntity.ok().build();
    }
}