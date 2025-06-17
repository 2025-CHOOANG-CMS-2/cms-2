package kr.ac.dhuniv.core_cpt.dto.qst;

import lombok.Data;

import java.util.List;

/**
 * ✅ CoreCptQstRequestDTO
 * - 진단 문항 등록 요청 DTO
 */
@Data
public class CoreCptQstRequestDTO {

    private String qstCode;               // 문항 코드
    private String questionText;          // 문항 내용
    private Integer qstOrd;               // 문항 순서
    private Long coreCptInfoId;           // 상위 역량 ID
    private String regUserId;             // 등록자 ID
    private List<OptionDTO> options;      // 선택지 리스트

    /**
     * ✅ OptionDTO
     * - 선택지 정보 DTO
     */
    @Data
    public static class OptionDTO {
        private String text;               // 선택지 내용
        private Integer score;             // 점수
        private Boolean isCorrect;         // 객관식 정답 여부
    }
}