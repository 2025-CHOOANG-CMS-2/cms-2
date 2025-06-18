package kr.ac.dhuniv.core_cpt.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "core_cpt_option_template")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CoreCptOptionTemplate {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="option_id")
    private Long optionId;

    @Column(name = "option_text", length = 500, nullable = false)
    private String optionText;

    @Column(name = "score", nullable = false)
    private Integer score;

    @Column(name = "ord", nullable = false)
    private Integer ord;  // 선택지 순서
}
