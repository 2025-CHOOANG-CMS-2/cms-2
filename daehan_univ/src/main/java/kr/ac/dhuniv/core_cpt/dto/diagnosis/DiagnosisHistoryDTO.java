package kr.ac.dhuniv.core_cpt.dto.diagnosis;

import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * ✅ DiagnosisHistoryDTO
 * - 학생 진단 이력 응답 DTO
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DiagnosisHistoryDTO {
    private Long stdNo;                       // 학생 번호
    private String userId;                    // 학번 or 사번
    private List<HistoryItem> history;        // 진단 이력 리스트

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class HistoryItem {
        private String evalCode;              // 진단 코드
        private LocalDateTime submittedAt;    // 제출일시
        private Integer totalScore;           // 총점
    }
}