package kr.ac.dhuniv.core_cpt.domain;

import jakarta.persistence.Entity;

import jakarta.persistence.*;
import kr.ac.dhuniv.ncs.domain.NcsPrgInfo;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
// 📁 kr/ac/dhuniv/core_cpt/domain/CoreCptInfo.java
@Entity
@Table(name = "core_cpt_info")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class CoreCptInfo {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;                    // PK

    @Column(name = "cci_id", length = 20, nullable = false, unique = true)
    private String cciId;               // 비즈니스키

    // (1) 부모 역량: up_cci_id → core_cpt_info.id (bigint FK)
// CoreCptInfo.java (자기참조)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")           // 기존 up_cci_id → parent_id
    private CoreCptInfo parent;

    // (2) 자식(하위) 역량 리스트
    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CoreCptInfo> children = new ArrayList<>();

    @Column(name = "cci_nm", length = 100)
    private String cciNm;                // 역량명

    @Column(name = "cci_desc", length = 500)
    private String cciDesc;              // 역량 설명

    @Column(name = "weight")
    private Integer weight;              // 가중치 (%)

    @Column(name = "color_hex", length = 10)
    private String colorHex;             // 표시 색상

    // (3) 연관된 문항 목록 (CoreCptQst ↔ CoreCptInfo)
    @OneToMany(
            mappedBy = "coreCptInfo",       // CoreCptQst.coreCptInfo 필드 참조
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private List<CoreCptQst> questions = new ArrayList<>();

    // (4) 추천 프로그램 목록 (기존 매핑 유지)
    @OneToMany(
            mappedBy = "coreCpt",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private List<NcsPrgInfo> recommendedPrograms = new ArrayList<>();

    // (5) 점수별 피드백 코멘트 템플릿
    @OneToMany(
            mappedBy = "coreCpt",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private List<CoreCptCommentTemplate> commentTemplates = new ArrayList<>();

    @Column(name = "reg_user_id", length = 20)
    private String regUserId;            // 등록자

    @Column(name = "reg_dt")
    private LocalDateTime regDt;         // 등록일

    @Column(name = "upd_user_id", length = 20)
    private String updUserId;            // 수정자

    @Column(name = "upd_dt")
    private LocalDateTime updDt;         // 수정일
}
