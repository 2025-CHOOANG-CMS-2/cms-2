package kr.ac.dhuniv.core_cpt.controller;


import kr.ac.dhuniv.core_cpt.dto.diagnosis.DiagnosisRequestDTO;
import kr.ac.dhuniv.core_cpt.dto.diagnosis.DiagnosisResponseDTO;
import kr.ac.dhuniv.core_cpt.dto.diagnosis.DiagnosisStatusResponseDTO;
import kr.ac.dhuniv.core_cpt.service.DiagnosisEvalService;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/diagnosis")
@RequiredArgsConstructor
public class DiagnosisEvalController {

    private final DiagnosisEvalService evalService;
//    private final DiagnosisScoreService scoreService;

    @PostMapping("/submit/{cciId}")
    public ResponseEntity<DiagnosisResponseDTO> submitDiagnosis(
            @PathVariable Long cciId,
            @RequestBody DiagnosisRequestDTO dto) {
        DiagnosisResponseDTO response = evalService.saveDiagnosis(cciId, dto);
        return ResponseEntity.ok(response);
    }
    /**
     * ✅ 학생 최신 진단 현황 조회 API
     * @param studentId 학생 ID
     * @return 진단 현황
     */
    @GetMapping("/latest/{studentId}")
    public ResponseEntity<DiagnosisStatusResponseDTO> getLatestDiagnosis(@PathVariable Long studentId) {
        DiagnosisStatusResponseDTO dto = evalService.getLatestDiagnosis(studentId);
        return ResponseEntity.ok(dto);
    }

}