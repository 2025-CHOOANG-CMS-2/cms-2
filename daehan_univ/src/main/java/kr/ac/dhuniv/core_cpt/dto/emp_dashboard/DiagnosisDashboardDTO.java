package kr.ac.dhuniv.core_cpt.dto.emp_dashboard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 역량진단 대시보드 상단 카드 요약 정보를 담는 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DiagnosisDashboardDTO {

    // 전체 진단 수
    private Long totalEvalCount;

    // 평균 점수
    private Double averageScore;

    // 전체 진단 문항 수
    private Long questionCount;

    // 진단 미완료 학생 수
    private Long incompleteStudentCount;
}
