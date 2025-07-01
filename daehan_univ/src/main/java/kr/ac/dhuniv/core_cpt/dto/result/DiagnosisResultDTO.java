package kr.ac.dhuniv.core_cpt.dto.result;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 전체 진단 결과 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DiagnosisResultDTO {
    /** 전체 종합 점수(0–100) */
    private double totalScore;
    /** 상위역량별 상세 결과 */
    private List<CompetencyScoreDTO> details;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CompetencyScoreDTO {
        private Long cciId;           // 상위역량 ID
        private String cciNm;         // 역량명
        private int questionCount;    // 해당역량(하위 포함) 문항수
        private int sumScore;         // 문항별 합산 점수
        private int maxScore;         // 문항별 최대 합산 점수
        private double percentage;    // sumScore/maxScore * 100
        private int weight;           // CoreCptInfo.weight (%)
    }
}