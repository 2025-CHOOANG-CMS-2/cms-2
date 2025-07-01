package kr.ac.dhuniv.core_cpt.dto.diagnosis;

import lombok.AllArgsConstructor;
import lombok.Data;


import lombok.Builder;
import lombok.NoArgsConstructor;


/**
 * ✅ 역량별 점수 상세 DTO
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DiagnosisDetailDTO {
    private String competencyName;   // 역량명
    private int score;               // 점수
    private int avgScore;            // 평균 점수
}