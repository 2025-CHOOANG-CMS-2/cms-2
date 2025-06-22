package kr.ac.dhuniv.core_cpt.dto.qst;

import kr.ac.dhuniv.core_cpt.domain.CoreCptOptionTemplate;
import kr.ac.dhuniv.core_cpt.dto.diagnosis.CoreCptOptionTemplateDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * ✅ CoreCptQstListDTO
 * - 목록 조회용 DTO
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CoreCptQstListDTO {
    private Long qstId;           // 문항 ID
    private String qstCode;       // 문항 코드
    private String questionText;  // 문항 내용
    private Integer qstOrd;       // ✅ 문항 순서 추가
    private String competencyName;// 상위 역량명
    private String topCompetencyName;   // 상위 역량명
    private String colorHex;        // 상위 역량 색상 (hex)
    private String subCompetencyName;   // 하위 역량명
    private Long topCompetencyId;
    private Long subCompetencyId;
    private List<CoreCptOptionTemplateDTO> options;  // 옵션 리스트 포함
}
