package kr.ac.dhuniv.core_cpt.domain;

import jakarta.persistence.*;
import kr.ac.dhuniv.std_info.domain.StdInfo;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * ✅ CoreCptEval 엔티티
 * - core_cpt_eval 테이블과 1:1 매핑
 * - 각 문항(qst_id)에 대해 학생(std_no)이 선택한 옵션(option_id)과
 *   그 옵션에 부여된 점수(ans_score)를 저장
 */
@Entity
@Table(name = "core_cpt_eval")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class CoreCptEval {

    /** PK (자동증가) */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "eval_id")
    private Long evalId;

    /** 한 번의 진단 세션을 구분하는 비즈니스키 */
    @Column(name = "eval_code", length = 50, nullable = false, unique = true)
    private String evalCode;

    /** 답변 일시 (insert 시점에 자동 세팅) */
    @Column(name = "ans_dt", nullable = false)
    private LocalDateTime answerDate;

    /** 학생 정보를 StdInfo 엔티티로 매핑 */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "std_no", referencedColumnName = "std_no")
    private StdInfo student;

    /** 문항(CoreCptQst) 매핑 */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "qst_id", referencedColumnName = "qst_id")
    private CoreCptQst question;

    /** 선택된 옵션(CoreCptOptionTemplate) 매핑 */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "option_id", referencedColumnName = "option_id")
    private CoreCptOptionTemplate selectedOption;

    /** 선택지에 부여된 점수 */
    @Column(name = "ans_score", nullable = false)
    private Integer answerScore;

    /**
     * insert 직전에 answerDate 필드에 현재 시각 자동 세팅
     */
    @PrePersist
    public void prePersist() {
        this.answerDate = LocalDateTime.now();
    }
}
