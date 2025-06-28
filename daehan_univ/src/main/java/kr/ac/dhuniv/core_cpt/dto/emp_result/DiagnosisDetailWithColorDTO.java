package kr.ac.dhuniv.core_cpt.dto.emp_result;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class DiagnosisDetailWithColorDTO {
    private String competencyName;   // 역량명 (예: 의사소통역량)
    private Double score;            // 점수
    private String colorHex;         // 색상 HEX 코드
}