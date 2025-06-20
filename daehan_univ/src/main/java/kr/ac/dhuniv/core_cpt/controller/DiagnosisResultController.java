package kr.ac.dhuniv.core_cpt.controller;


import kr.ac.dhuniv.core_cpt.dto.result.DiagnosisResultDTO;
import kr.ac.dhuniv.core_cpt.service.DiagnosisResultService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


/**
 * ✅ DiagnosisResultController
 * - 점수 집계 API
 */
@RestController
@RequestMapping("/api/diagnosis")
@RequiredArgsConstructor
public class DiagnosisResultController {

    private final DiagnosisResultService diagnosisResultService;

    /**
     * ✅ getResult
     * - 학생 점수 집계 결과 조회 API
     *
     * @param stdNo 학생 번호
     * @return DiagnosisResultDTO
     */
    @GetMapping("/result/{stdNo}")
    public ResponseEntity<DiagnosisResultDTO> getResult(@PathVariable Long stdNo) {
        DiagnosisResultDTO result = diagnosisResultService.getDiagnosisResult(stdNo);
        return ResponseEntity.ok(result);
    }
}