package kr.ac.dhuniv.core_cpt.controller;

import kr.ac.dhuniv.core_cpt.domain.CoreCptInfo;
import kr.ac.dhuniv.core_cpt.domain.CoreCptOptionTemplate;
import kr.ac.dhuniv.core_cpt.dto.diagnosis.CoreCptInfoDTO;
import kr.ac.dhuniv.core_cpt.repository.CoreCptInfoRepository;
import kr.ac.dhuniv.core_cpt.repository.CoreCptOptionTemplateRepository;
import kr.ac.dhuniv.core_cpt.service.DiagnosisService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.NoSuchElementException;

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
     * ✅ 전체 상위역량 + 하위 + 문항 + 선택지 트리 조회
     */
    @GetMapping("/questions")
    public ResponseEntity<List<CoreCptInfoDTO>> getAllQuestionsTree() {
        List<CoreCptInfoDTO> list = diagnosisService.getAllCompetencyTree();
        return ResponseEntity.ok(list);
    }

    /**
     * ✅ 특정 상위역량 ID로부터 하위 + 문항 + 선택지 트리 조회
     */
    @GetMapping("/questions/{cciId}")
    public ResponseEntity<CoreCptInfoDTO> getQuestionsByCciId(@PathVariable Long cciId) {
        CoreCptInfoDTO dto = diagnosisService.getCompetencyTreeByCciId(cciId);
        return ResponseEntity.ok(dto);
    }
}