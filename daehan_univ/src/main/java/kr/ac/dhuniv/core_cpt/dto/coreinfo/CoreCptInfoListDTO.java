package kr.ac.dhuniv.core_cpt.dto.coreinfo;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CoreCptInfoListDTO {
    private Long cciId;
    private String cciCode;       // 핵심역량 ID
    private String cciNm;       // 이름
    private String cciDesc;     // 설명
    private int questionCount;  // 문항 수 (지금은 0으로 세팅)
}