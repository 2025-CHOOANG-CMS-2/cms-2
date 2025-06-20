package kr.ac.dhuniv.core_cpt.dto.result;

import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * ✅ DiagnosisResultDTO
 * - 학생의 진단 결과 응답용 DTO
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DiagnosisResultDTO {
    private Long stdNo;                  // 학생 번호
    private String userId;               // 학번 or 사번 (비즈니스 키)
    private Integer totalScore;          // 총합 점수 (평균 또는 합산)
    private LocalDateTime submittedAt;   // 제출일시
    private List<CompetencyScore> competencies; // 핵심역량별 점수

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CompetencyScore {
        private Long cciId;         // 핵심역량 ID
        private String cciNm;       // 핵심역량명
        private Integer score;      // 핵심역량 점수
    }
}