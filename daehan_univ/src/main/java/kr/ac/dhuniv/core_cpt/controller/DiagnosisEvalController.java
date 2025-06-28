package kr.ac.dhuniv.core_cpt.controller;

import kr.ac.dhuniv.core_cpt.dto.diagnosis.DiagnosisRequestDTO;
import kr.ac.dhuniv.core_cpt.dto.diagnosis.DiagnosisResponseDTO;
import kr.ac.dhuniv.core_cpt.dto.diagnosis.DiagnosisStatusResponseDTO;
import kr.ac.dhuniv.core_cpt.dto.result.DiagnosisAnalysisResponseDTO;

import kr.ac.dhuniv.core_cpt.service.DiagnosisEvalService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/diagnosis")
@RequiredArgsConstructor
public class DiagnosisEvalController {

    private final DiagnosisEvalService evalService;

    /**
     * ✅ 진단 제출 API
     */
    @PostMapping("/submit/{cciId}")
    public ResponseEntity<DiagnosisResponseDTO> submitDiagnosis(
            @PathVariable Long cciId,
            @RequestBody DiagnosisRequestDTO dto) {
        DiagnosisResponseDTO response = evalService.saveDiagnosis(cciId, dto);
        return ResponseEntity.ok(response);
    }

    /**
     * 학생 최신 진단 점수 조회 API
     *
     * @param studentId 내부 DB PK (std_id)
     * @param studentNo 학생 번호 (std_no)
     * @return 상위역량별 최신 진단 점수
     */
    @GetMapping("/latest-by-eval/{studentId}/{studentNo}")
    public ResponseEntity<DiagnosisStatusResponseDTO> getLatestByEval(
            @PathVariable Long studentId,
            @PathVariable String studentNo) {
        DiagnosisStatusResponseDTO dto = evalService.getLatestDiagnosisByLatestEval(studentId, studentNo);
        return ResponseEntity.ok(dto);
    }
//    /**
//     * ✅ 결과 분석 API
//     * @param studentNo 학번
//     * @return 분석 결과 JSON
//     */
    @GetMapping("/analysis/{studentNo}")
   public ResponseEntity<List<DiagnosisAnalysisResponseDTO>> getDiagnosisAnalysis(@PathVariable String studentNo) {
        List<DiagnosisAnalysisResponseDTO> result = evalService.getDiagnosisAnalysis(studentNo);
        return ResponseEntity.ok(result);
  }
}
