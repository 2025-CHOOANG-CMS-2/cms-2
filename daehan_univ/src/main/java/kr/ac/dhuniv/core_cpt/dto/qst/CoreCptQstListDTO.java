package kr.ac.dhuniv.core_cpt.dto.qst;

import lombok.Builder;
import lombok.Data;

/**
 * ✅ CoreCptQstListDTO
 * - 목록 조회용 DTO
 */
@Data
@Builder
public class CoreCptQstListDTO {
    private Long qstId;           // 문항 ID
    private String qstCode;       // 문항 코드
    private String questionText;  // 문항 내용
    private String competencyName;// 상위 역량명
}
