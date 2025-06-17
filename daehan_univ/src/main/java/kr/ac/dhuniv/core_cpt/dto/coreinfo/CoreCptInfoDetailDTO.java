package kr.ac.dhuniv.core_cpt.dto.coreinfo;


import lombok.*;
import java.util.List;

@Getter
@Setter
public class CoreCptInfoDetailDTO {
    private Long cciId;                   // 상위 역량 id
    private String cciCode;
    private String cciNm;                   // 역량명
    private String cciDesc;                 // 설명
    private Integer weight;                 // 가중치 (%)
    private String colorHex;                // 표시 색상
    private int questionCount;              // 문항 수
    private List<SubCompetencyDTO> children; // 하위 역량 목록
}