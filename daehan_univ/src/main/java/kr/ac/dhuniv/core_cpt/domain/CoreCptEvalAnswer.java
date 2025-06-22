package kr.ac.dhuniv.core_cpt.domain;

import jakarta.persistence.*;
import lombok.*;

/**
 * CoreCptEvalAnswer
 * - 진단 세션에서 각 문항에 대한 답변 저장
 */
@Entity
@Table(name = "core_cpt_eval_answer")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class CoreCptEvalAnswer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "answer_id")
    private Long answerId; // PK

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "eval_id")
    private CoreCptEval eval; // 해당 진단 세션

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "qst_id")
    private CoreCptQst question; // 문항

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "option_id")
    private CoreCptOptionTemplate selectedOption; // 선택지

    @Column(name = "ans_score", nullable = false)
    private Integer answerScore; // 선택지 점수
}
