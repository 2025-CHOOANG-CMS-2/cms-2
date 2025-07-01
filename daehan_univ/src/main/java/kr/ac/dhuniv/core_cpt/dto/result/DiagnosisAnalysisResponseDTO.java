package kr.ac.dhuniv.core_cpt.dto.result;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * ✅ 상위 핵심역량별 분석 결과 응답용 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DiagnosisAnalysisResponseDTO {
    private String competencyName;  // 상위 역량 이름
    private int score;              // 점수
    private String colorHex;        // 색상 추가
    private String comment;         // 점수별 코멘트 추가
}