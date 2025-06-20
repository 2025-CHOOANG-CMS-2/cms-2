package kr.ac.dhuniv.core_cpt.controller;

import kr.ac.dhuniv.core_cpt.dto.diagnosis.DiagnosisHistoryDTO;
import kr.ac.dhuniv.core_cpt.service.DiagnosisHistoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * ✅ DiagnosisHistoryController
 * - 진단 이력 API
 */
@RestController
@RequestMapping("/api/diagnosis")
@RequiredArgsConstructor
public class DiagnosisHistoryController {

    private final DiagnosisHistoryService diagnosisHistoryService;

    /**
     * ✅ getHistory
     * - 진단 이력 조회 API
     *
     * @param stdNo 학생 번호
     * @return DiagnosisHistoryDTO
     */
    @GetMapping("/history/{stdNo}")
    public ResponseEntity<DiagnosisHistoryDTO> getHistory(@PathVariable Long stdNo) {
        DiagnosisHistoryDTO history = diagnosisHistoryService.getDiagnosisHistory(stdNo);
        return ResponseEntity.ok(history);
    }
}
