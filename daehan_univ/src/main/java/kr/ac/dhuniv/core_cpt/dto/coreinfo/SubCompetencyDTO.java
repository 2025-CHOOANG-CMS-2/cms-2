package kr.ac.dhuniv.core_cpt.dto.coreinfo;

import lombok.*;

@Getter
@Setter
public class SubCompetencyDTO {
    private String cciCode;    // 하위 역량 코드
    private String cciNm;    // 하위 역량명
    private String cciDesc;  // 설명
    private Integer weight;  // 가중치 (%)
}