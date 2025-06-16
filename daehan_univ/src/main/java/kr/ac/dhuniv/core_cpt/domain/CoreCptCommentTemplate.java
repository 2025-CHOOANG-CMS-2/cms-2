package kr.ac.dhuniv.core_cpt.domain;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.*;
@Entity
@Table(name = "core_cpt_comment_template")
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class CoreCptCommentTemplate {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long cmt_id;

    // core_cpt_comment_template.cci_id → core_cpt_info.id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "core_cpt_info_id")    // 기존 cci_id → core_cpt_info_id
    @JsonBackReference
    private CoreCptInfo coreCpt;

    @Column(name = "score_level", length = 20)
    private String scoreLevel;
    @Column(name = "min_score")
    private Integer minScore;   // 점수 구간 하한

    @Column(name = "max_score")
    private Integer maxScore;   // 점수 구간 상한
    @Column(name = "content", columnDefinition = "TEXT")
    private String content;
}