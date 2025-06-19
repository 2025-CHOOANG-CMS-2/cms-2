package kr.ac.dhuniv.counsel.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import lombok.RequiredArgsConstructor;

import kr.ac.dhuniv.counsel.dto.AvailableSlotsDto;
import kr.ac.dhuniv.counsel.dto.CounselorSimpleDto;
import kr.ac.dhuniv.counsel.service.CounselStudentService;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/counseling") // 학생용 API 경로
@RequiredArgsConstructor
public class CounselStudentApiController {

    private final CounselStudentService counselStudentService;
    
    // 상담 유형으로 상담사 목록 조회 API
    @GetMapping("/counselors")
    public ResponseEntity<List<CounselorSimpleDto>> getCounselorsByType(@RequestParam("type") String counselingType) {
        List<CounselorSimpleDto> counselors = counselStudentService.findCounselorsByType(counselingType);
        return ResponseEntity.ok(counselors);
    }
    
    // 월간 예약 가능 시간 조회 API (이 버전만 남겨둡니다)
    @GetMapping("/available-slots")
    public ResponseEntity<Map<String, List<AvailableSlotsDto>>> getAvailableSlotsForMonth(
            @RequestParam("year") Integer year,
            @RequestParam("month") Integer month,
            @RequestParam(value = "type", required = false, defaultValue = "all") String counselingType,
            @RequestParam(value = "counselorId", required = false, defaultValue = "all") String counselorId) {
        
        Map<String, List<AvailableSlotsDto>> availableSlots = counselStudentService.getAvailableSlotsForMonth(year, month, counselingType, counselorId);
        return ResponseEntity.ok(availableSlots);
    }

    // 여기에 나중에 상담 예약을 생성하는 POST API가 추가될 것입니다.
}