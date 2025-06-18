package kr.ac.dhuniv.core_cpt.dto.qst;

import lombok.Data;

import java.util.List;

/**
 * ✅ CoreCptQstRequestDTO
 * - 문항 등록 요청용 DTO
 */
@Data
public class CoreCptQstRequestDTO {
    private String qstCode;           // 문항 코드
    private String questionText;      // 문항 내용
    private Integer qstOrd;           // 문항 순번
    private Long coreCptInfoId;       // 하위 역량 ID
    private Long optionTemplateId;    // 공통 선택지 템플릿 ID
    private String regUserId;         // 등록자 ID
}