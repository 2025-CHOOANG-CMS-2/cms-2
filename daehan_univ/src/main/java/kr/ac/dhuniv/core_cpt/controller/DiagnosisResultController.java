package kr.ac.dhuniv.core_cpt.controller;


import kr.ac.dhuniv.core_cpt.dto.result.CompetencyResultDTO;
import kr.ac.dhuniv.core_cpt.dto.result.DiagnosisResultDTO;
import kr.ac.dhuniv.core_cpt.service.DiagnosisResultService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;


//*
// * ✅ DiagnosisResultController
// * - 점수 집계 API
//

@RestController
@RequestMapping("/api/diagnosis")
@RequiredArgsConstructor
public class DiagnosisResultController {

    private final DiagnosisResultService resultService;

    /**
     * ✅ 진단 결과 조회 API
     * @param studentNo 학번
     * @return 상위역량별 점수 + 코멘트 + 색상 JSON
     */
    @GetMapping("/analysis/{studentNo}")
    public List<CompetencyResultDTO> getDiagnosisAnalysis(@PathVariable String studentNo) {
        return resultService.getDiagnosisResults(studentNo);
    }
}
