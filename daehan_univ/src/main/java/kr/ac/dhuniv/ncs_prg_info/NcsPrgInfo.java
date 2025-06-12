package kr.ac.dhuniv.ncs_prg_info;

import jakarta.persistence.*;
import kr.ac.dhuniv.com_info.ComInfo;
import kr.ac.dhuniv.core_cpt_info.CoreCptInfo;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "ncs_prg_info")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NcsPrgInfo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id; // 자동 증가 PK

    @Column(name = "prg_id", length = 20, nullable = false, unique = true)
    private String prgId; // 비즈니스 키

    @Column(name = "prg_nm", length = 100)
    private String prgNm;

    @Column(name = "prg_desc", length = 1000)
    private String prgDesc;

    @Column(name = "prg_st_dt")
    private LocalDateTime prgStartDate;

    @Column(name = "prg_end_dt")
    private LocalDateTime prgEndDate;

    @Column(name = "max_cnt")
    private Integer maxCnt;

    @Column(name = "reg_user_id", length = 20)
    private String regUserId;

    @Column(name = "reg_dt")
    private LocalDateTime regDt;

    @Column(name = "upd_user_id", length = 20)
    private String updUserId;

    @Column(name = "upd_dt")
    private LocalDateTime updDt;

    @Column(name = "aply_bgng_ymd")
    private LocalDateTime aplyBeginDate;

    @Column(name = "aply_end_ymd")
    private LocalDateTime aplyEndDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "com_no")
    private ComInfo company;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cci_id")
    private CoreCptInfo coreCpt;
}