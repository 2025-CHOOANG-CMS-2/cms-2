package kr.ac.dhuniv.core_cpt.dto.diagnosis;

import lombok.Builder;
import lombok.Data;
import java.util.List;

/**
 * ✅ 진단 결과 응답 DTO
 */
@Data
@Builder
public class DiagnosisResponseDTO {
    private String evalCode;                   // 진단 코드
    private int totalScore;                    // 총점
    private String levelText;                  // 수준 텍스트 (예: 우수)
    private List<DiagnosisDetailDTO> details;  // 역량별 상세 점수
}