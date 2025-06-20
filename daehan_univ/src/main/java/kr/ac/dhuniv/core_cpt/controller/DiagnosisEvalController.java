package kr.ac.dhuniv.core_cpt.controller;

import kr.ac.dhuniv.core_cpt.dto.eval.CoreCptEvalRequestDTO;
import kr.ac.dhuniv.core_cpt.service.DiagnosisEvalService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/diagnosis")
@RequiredArgsConstructor
public class DiagnosisEvalController {

    private final DiagnosisEvalService diagnosisEvalService;

    @PostMapping("/submit")
    public ResponseEntity<String> saveResponses(@RequestBody CoreCptEvalRequestDTO requestDTO) {
        diagnosisEvalService.saveDiagnosisResponses(requestDTO);
        return ResponseEntity.ok("진단 응답이 성공적으로 저장되었습니다.");
    }
}
