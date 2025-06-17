package kr.ac.dhuniv.core_cpt.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "core_cpt_qst_option")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CoreCptQstOption {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "qst_id", nullable = false)
    private CoreCptQst coreCptQst;

    @Column(name = "option_text", length = 500, nullable = false)
    private String optionText;

    @Column(name = "score", nullable = false)
    private Integer score;

    @Column(name = "is_correct")
    private Boolean isCorrect = false;
}

