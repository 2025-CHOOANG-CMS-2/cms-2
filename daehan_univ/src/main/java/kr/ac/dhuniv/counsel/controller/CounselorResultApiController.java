package kr.ac.dhuniv.counsel.controller;

import kr.ac.dhuniv.counsel.dto.CounselingResultItemDto;
import kr.ac.dhuniv.counsel.dto.CounselorScheduleDto;
import kr.ac.dhuniv.counsel.dto.PageDto;
import kr.ac.dhuniv.counsel.dto.WriteResultRequestDto;
import kr.ac.dhuniv.counsel.service.CounselorService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;

import java.util.List;

@RestController
@RequestMapping("/api/counselor") // [중요] 상담사 관련 API의 기본 경로
@RequiredArgsConstructor
public class CounselorResultApiController {

    private final CounselorService counselorService;

    /**
     * 상담사가 자신의 상담 결과 목록을 조회하는 API
     * (결과 작성이 필요한 '예약확정' 건과 '완료'된 건을 함께 조회)
     */
    @GetMapping("/results")
    public ResponseEntity<PageDto<CounselingResultItemDto>> getCounselingResults(
            @RequestParam(value = "period", defaultValue = "all") String period,
            @RequestParam(value = "type", defaultValue = "all") String type,
            @RequestParam(value = "statuses") List<String> statuses, // JS에서 보낸 "APPROVED,COMPLETED"를 리스트로 받음
            @PageableDefault(size = 10, sort = "applyDateTime", direction = Sort.Direction.DESC) Pageable pageable) {
        
        // TODO: 로그인 기능 구현 후, 실제 로그인한 상담사 ID로 교체해야 합니다.
        String counselorId = "2025110002"; // 임시 상담사 ID

        PageDto<CounselingResultItemDto> pagedResults = counselorService.getCounselingResults(counselorId, statuses, period, type, pageable);
        return ResponseEntity.ok(pagedResults);
    }

    @GetMapping("/results/{resultId}")
    public ResponseEntity<CounselingResultItemDto> getCounselingResultDetail(@PathVariable("resultId") Long resultId) {
        // TODO: 로그인 기능 구현 후, 실제 로그인한 상담사 ID로 권한 확인 로직 추가 필요
        CounselingResultItemDto resultDetail = counselorService.getCounselingResultDetail(resultId);
        return ResponseEntity.ok(resultDetail);
    }
    
    @PostMapping("/results")
    public ResponseEntity<Void> writeCounselingResult(@Valid @RequestBody WriteResultRequestDto requestDto) {
        // TODO: 로그인 기능 구현 후, 실제 로그인한 상담사의 ID로 교체해야 합니다.
        String counselorId = "2025110002"; 
        counselorService.writeCounselingResult(counselorId, requestDto);
        return ResponseEntity.ok().build();
    }
    
    @PutMapping("/results/{resultId}")
    public ResponseEntity<Void> updateCounselingResult(@PathVariable("resultId") Long resultId,
                                                       @Valid @RequestBody WriteResultRequestDto requestDto) {
        String counselorId = "2025110002"; // TODO: 로그인 기능 구현 후, 실제 상담사 ID로 교체
        counselorService.updateCounselingResult(resultId, requestDto, counselorId);
        return ResponseEntity.ok().build();
    }
    
    
}
