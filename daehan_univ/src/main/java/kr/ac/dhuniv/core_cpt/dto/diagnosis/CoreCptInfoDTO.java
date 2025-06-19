package kr.ac.dhuniv.core_cpt.dto.diagnosis;

import lombok.*;

import java.util.List;
/**
 * ✅ CoreCptInfoDTO
 * - 핵심역량(core competency) 정보를 클라이언트로 전달하기 위한 DTO
 * - 하위 역량(children)과 문항 목록 포함
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CoreCptInfoDTO {
    private Long cciId;                  // 핵심역량 ID
    private String cciCode;              // 역량 코드
    private String cciNm;                // 역량명
    private String cciDesc;              // 역량 설명
    private String colorHex;             // 색상 코드
    private List<CoreCptQstDTO> questions; // 역량에 속한 문항 목록
    private List<CoreCptInfoDTO> children; // 하위 역량 목록 (재귀적 구조)
}