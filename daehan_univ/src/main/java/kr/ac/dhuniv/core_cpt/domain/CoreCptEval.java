package kr.ac.dhuniv.core_cpt.domain;

import jakarta.persistence.*;

import kr.ac.dhuniv.std_info.domain.StdInfo;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "core_cpt_eval")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CoreCptEval {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "eval_id")
    private Long evalId; // 자동 증가 PK

    @Column(name = "eval_code", length = 20, nullable = false, unique = true)
    private String evalCode; // 비즈니스 키

    @Column(name = "ans_score", precision = 10, scale = 3)
    private BigDecimal answerScore;

    @Column(name = "ans_dt")
    private LocalDateTime answerDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "std_no", referencedColumnName = "std_no")
    private StdInfo student;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "qst_id", referencedColumnName = "qst_id")
    private CoreCptQst question;
}