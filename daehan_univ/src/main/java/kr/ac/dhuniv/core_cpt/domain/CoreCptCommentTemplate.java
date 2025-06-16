package kr.ac.dhuniv.core_cpt.domain;

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
    private CoreCptInfo coreCpt;

    @Column(name = "score_level", length = 20)
    private String scoreLevel;

    @Column(name = "content", columnDefinition = "TEXT")
    private String content;
}