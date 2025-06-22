package kr.ac.dhuniv.core_cpt.dto.diagnosis;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * ✅ 단일 문항에 대한 사용자의 답변 DTO
 * - qstId: CoreCptQst 엔티티의 PK
 * - score: 사용자가 선택한 옵션의 점수 값
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AnswerDTO {
    /** 문항 ID (CoreCptQst.qstId) */
    private Long qstId;

    /** 선택된 옵션의 점수 */
    private Integer score;
}
