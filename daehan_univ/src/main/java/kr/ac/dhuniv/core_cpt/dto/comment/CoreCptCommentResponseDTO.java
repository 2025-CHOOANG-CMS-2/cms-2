package kr.ac.dhuniv.core_cpt.dto.comment;

import lombok.Builder;
import lombok.Data;

/**
 * ✅ CoreCptCommentResponseDTO
 * - 점수 구간 코멘트 응답용 DTO
 */
@Data
@Builder
public class CoreCptCommentResponseDTO {
    private Long id;              // 코멘트 ID
    private Integer minScore;     // 최소 점수
    private Integer maxScore;     // 최대 점수
    private String content;       // 코멘트 내용
    private String scoreLevel;    // 점수 구간명 (optional)
}