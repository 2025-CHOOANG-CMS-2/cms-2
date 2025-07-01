package kr.ac.dhuniv.core_cpt.dto.eval;

import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * ✅ CoreCptEvalRequestDTO
 * - 진단 응답 제출 시 클라이언트가 보내는 데이터
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CoreCptEvalRequestDTO {
    private String evalCode;  // 비즈니스 키
    private Long stdNo;       // 학생 번호
    private LocalDateTime answerDate;  // 응답 시간
    private List<Answer> answers;      // 문항별 응답

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Answer {
        private Long qstId;     // 문항 ID
        private Long optionId;  // 선택지 ID
    }
}