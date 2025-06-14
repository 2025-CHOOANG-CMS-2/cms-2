package kr.ac.dhuniv.core_cpt.dto;


import lombok.*;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CoreCptInfoDetailDTO {
    private String cciId;                   // 상위 역량 코드
    private String cciNm;                   // 역량명
    private String cciDesc;                 // 설명
    private Integer weight;                 // 가중치 (%)
    private String colorHex;                // 표시 색상
    private int questionCount;              // 문항 수
    private List<SubCompetencyDTO> children; // 하위 역량 목록
}