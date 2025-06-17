package kr.ac.dhuniv.core_cpt.dto.comment;


import lombok.Data;

/**
 * ✅ CoreCptCommentRequestDTO
 * - 점수 구간 코멘트 등록용 요청 DTO
 */
@Data
public class CoreCptCommentRequestDTO {
    private Integer minScore;     // 최소 점수 (예: 0)
    private Integer maxScore;     // 최대 점수 (예: 60)
    private String content;       // 코멘트 내용
    private String scoreLevel;    // 점수 구간명 (예: "0~60점", 옵션)
}