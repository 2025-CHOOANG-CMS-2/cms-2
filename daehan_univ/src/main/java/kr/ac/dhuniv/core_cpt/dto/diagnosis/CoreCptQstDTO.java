package kr.ac.dhuniv.core_cpt.dto.diagnosis;

import lombok.*;

import java.util.List;

/**
 * ✅ CoreCptQstDTO
 * - 문항(question) 정보를 클라이언트로 전달하기 위한 DTO
 * - 선택지 목록을 포함
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CoreCptQstDTO {
    private Long qstId;                           // 문항 ID
    private String qstCode;                       // 문항 코드
    private String qstCont;                       // 문항 내용
    private Integer qstOrd;                       // 문항 순서
    private List<CoreCptOptionTemplateDTO> options; // 선택지 목록
}