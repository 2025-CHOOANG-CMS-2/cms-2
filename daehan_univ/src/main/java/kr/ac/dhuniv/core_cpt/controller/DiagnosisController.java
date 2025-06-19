package kr.ac.dhuniv.core_cpt.controller;

import kr.ac.dhuniv.core_cpt.dto.diagnosis.CoreCptInfoDTO;
import kr.ac.dhuniv.core_cpt.service.DiagnosisService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * ✅ DiagnosisController
 * - 핵심역량 진단 관련 API를 제공하는 컨트롤러
 */
@RestController
@RequestMapping("/api/diagnosis")
@RequiredArgsConstructor
public class DiagnosisController {

    private final DiagnosisService diagnosisService;

    /**
     * ✅ getQuestions
     * - 핵심역량 + 문항 + 선택지 트리 구조 데이터를 조회
     *
     * @return 핵심역량 DTO 리스트
     */
    @GetMapping("/questions")
    public ResponseEntity<List<CoreCptInfoDTO>> getQuestions() {
        List<CoreCptInfoDTO> data = diagnosisService.getDiagnosisQuestions();
        return ResponseEntity.ok(data);
    }
}