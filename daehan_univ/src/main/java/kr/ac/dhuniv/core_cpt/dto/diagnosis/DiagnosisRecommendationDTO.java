package kr.ac.dhuniv.core_cpt.dto.diagnosis;

import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * ✅ DiagnosisRecommendationDTO
 * - 개선 방안 + 프로그램 추천 DTO
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DiagnosisRecommendationDTO {
    private Long stdNo;                          // 학생 번호
    private String userId;                       // 학번 or 사번
    private LocalDateTime submittedAt;           // 제출일시
    private List<RecommendationItem> recommendations; // 핵심역량별 추천

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class RecommendationItem {
        private Long cciId;
        private String cciNm;
        private Integer score;
        private String comment;
        private List<ProgramItem> programs;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ProgramItem {
        private Long prgId;
        private String prgNm;
        private String desc;
        private String applyDeadline;
    }
}