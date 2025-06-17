package kr.ac.dhuniv.core_cpt.dto.coreinfo;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CoreCptInfoRequestDTO {
    // 사용자 입력 필드

    private String cciNm;       // 역량명
    private String cciDesc;     // 역량 설명

    private Integer weight;     // 가중치 (%) 추가
    private String colorHex;    // 표시 색상 (예: "#3498db") 추가

    private String regUserId;   // 등록자 ID
}
