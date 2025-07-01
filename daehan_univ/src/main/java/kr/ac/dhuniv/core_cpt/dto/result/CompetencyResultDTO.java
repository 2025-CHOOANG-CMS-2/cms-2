package kr.ac.dhuniv.core_cpt.dto.result;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * ✅ 핵심역량 결과 DTO
 * - 각 상위역량별 점수, 코멘트, 색상, 학번 포함
 */
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CompetencyResultDTO {
    private String studentNo;          // 학생 학번
    private String competencyName;     // 상위 역량 이름
    private int score;                 // 상위 역량 진단 점수
    private String comment;            // 점수 구간 코멘트
    private String colorHex;           // Chart.js 연동용 색상 HEX 코드
}
