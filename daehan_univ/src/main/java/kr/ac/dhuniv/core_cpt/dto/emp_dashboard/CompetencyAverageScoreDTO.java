package kr.ac.dhuniv.core_cpt.dto.emp_dashboard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * ✅ 역량별 평균 점수 차트 데이터를 담는 DTO
 * - 각 상위역량의 이름, 평균 점수, 색상코드를 포함함
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CompetencyAverageScoreDTO {

    // 상위 역량 ID (cci_id 또는 parent_id가 null인 core_cpt_info)
    private Long upperCciId;

    // 상위 역량 이름
    private String upperCciName;

    // 평균 점수 (소수점 첫째자리까지 반올림)
    private Double avgScore;

    // 역량별 색상 코드 (예: #007bff)
    private String colorHex;
}