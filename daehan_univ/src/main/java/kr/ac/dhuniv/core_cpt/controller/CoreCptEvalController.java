package kr.ac.dhuniv.core_cpt.controller;
import kr.ac.dhuniv.core_cpt.dto.emp_dashboard.CompetencyAverageScoreDTO;
import kr.ac.dhuniv.core_cpt.dto.emp_dashboard.DiagnosisDashboardDTO;
import kr.ac.dhuniv.core_cpt.dto.emp_dashboard.DiagnosisDetailResponseDTO;
import kr.ac.dhuniv.core_cpt.dto.emp_dashboard.RecentDiagnosisResponseDto;
import kr.ac.dhuniv.core_cpt.dto.emp_result.DiagnosisDetailResponseBundleDTO;
import kr.ac.dhuniv.core_cpt.dto.emp_result.DiagnosisDetailWithColorDTO;
import kr.ac.dhuniv.core_cpt.dto.emp_result.ProgramRecommendationDTO;
import kr.ac.dhuniv.core_cpt.service.DiagnosisEvalService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * ✅ CoreCptEvalController
 * - 역량진단 관련 API 컨트롤러
 */
@RestController
@RequestMapping("/api/diagnosis") // 공통 prefix
@RequiredArgsConstructor
public class CoreCptEvalController {

    private final DiagnosisEvalService diagnosisEvalService;

    /**
     * ✅ 역량진단 대시보드 요약 통계 API
     * - 전체 진단 수, 평균 점수, 문항 수, 미완료 학생 수 반환
     * - 응답: DiagnosisDashboardDTO
     */
    @GetMapping("/dashboard-summary")
    public ResponseEntity<DiagnosisDashboardDTO> getDashboardSummary() {
        DiagnosisDashboardDTO dto = diagnosisEvalService.getDashboardSummary();
        return ResponseEntity.ok(dto); // JSON 응답으로 반환
    }

    /**
     * ✅ 상위역량별 평균 점수 + 색상 정보 조회 API
     * - 모든 학생의 가장 최근 진단 기준으로 계산됨
     * - Chart.js 등 프론트 차트에서 사용
     */
    @GetMapping("/competency-avg")
    public ResponseEntity<List<CompetencyAverageScoreDTO>> getAverageScoreByCompetency() {
        List<CompetencyAverageScoreDTO> result = diagnosisEvalService.getAverageScoreByCompetency();
        return ResponseEntity.ok(result);
    }

    @GetMapping("/recent")
    public ResponseEntity<List<RecentDiagnosisResponseDto>> getRecentResults() {
        return ResponseEntity.ok(diagnosisEvalService.getRecentDiagnosisResults());
    }

    @GetMapping("/detail/{stdNo}")
    public ResponseEntity<List<DiagnosisDetailResponseDTO>> getDiagnosisDetail(@PathVariable("stdNo") String stdNo) {
        return ResponseEntity.ok(diagnosisEvalService.getDiagnosisDetail(stdNo));
    }
    @GetMapping("/detail-with-color/{stdNo}")
    public ResponseEntity<DiagnosisDetailResponseBundleDTO> getDiagnosisDetailWithColor(@PathVariable String stdNo) {
        return ResponseEntity.ok(diagnosisEvalService.getDiagnosisDetailWithStudentInfo(stdNo));
    }
    @GetMapping("/recommend/{stdNo}")
    public ResponseEntity<List<ProgramRecommendationDTO>> getRecommendedPrograms(@PathVariable("stdNo") String stdNo) {
        return ResponseEntity.ok(diagnosisEvalService.getRecommendedPrograms(stdNo));
    }
}