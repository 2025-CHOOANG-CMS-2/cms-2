package kr.ac.dhuniv.core_cpt.domain;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "core_cpt_qst")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class CoreCptQst {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="qst_id")
    private Long qstId;

    @Column(name = "qst_code", length = 20, nullable = false, unique = true)
    private String qstCode;

    // core_cpt_qst.cci_id → core_cpt_info.id
    // CoreCptQst.java
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "core_cpt_info_id")    // 기존 cci_id → core_cpt_info_id
    private CoreCptInfo coreCptInfo;

    @Column(name = "qst_cont", length = 500)
    private String qstCont; //문항 내용

    @Column(name = "qst_ord")
    private Integer qstOrd; //문항 순서

    @Column(name = "reg_user_id", length = 20)
    private String regUserId; //등록자 ID

    @Column(name = "reg_dt")
    private LocalDateTime regDt; //등록일

    @Column(name = "upd_user_id", length = 20)
    private String updUserId; //수정자 ID

    @Column(name = "upd_dt")
    private LocalDateTime updDt; //수정일
}