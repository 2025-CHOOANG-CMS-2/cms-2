package kr.ac.dhuniv.ncs.domain;

import jakarta.persistence.*;
import kr.ac.dhuniv.core_cpt.domain.CoreCptInfo;
import lombok.*;

import java.time.LocalDateTime;

/**
 * NCS 비교과 프로그램 정보 엔티티
 * <p>
 * NCS_PRG_INFO 테이블 매핑
 */
@Entity
@Table(name = "ncs_prg_info")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NcsPrgInfo {

    /**
     * 프로그램 ID (기본 키, 자동 증가)
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "prg_id")
    private Long prgId;

    /**
     * 핵심역량 정보 (외래키: CORE_CPT_INFO)
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cci_id")
    private CoreCptInfo coreCpt;

    /**
     * 프로그램 고유코드 (비즈니스 키, 유니크)
     */
    @Column(name = "prg_code", length = 20, unique = true)
    private String prgCode;

    /**
     * 프로그램명
     */
    @Column(name = "prg_nm", length = 100)
    private String prgNm;

    /**
     * 프로그램 설명
     */
    @Column(name = "prg_desc", length = 1000)
    private String prgDesc;

    /**
     * 운영 시작일
     */
    @Column(name = "prg_st_dt")
    private LocalDateTime prgStartDate;

    /**
     * 운영 종료일
     */
    @Column(name = "prg_end_dt")
    private LocalDateTime prgEndDate;

    /**
     * 모집 인원 수
     */
    @Column(name = "max_cnt")
    private Integer maxCnt;

    /**
     * 등록자 ID
     */
    @Column(name = "reg_user_id", length = 20)
    private String regUserId;

    /**
     * 등록 일시
     */
    @Column(name = "reg_dt")
    private LocalDateTime regDt;

    /**
     * 수정자 ID
     */
    @Column(name = "upd_user_id", length = 20)
    private String updUserId;

    /**
     * 수정 일시
     */
    @Column(name = "upd_dt")
    private LocalDateTime updDt;

    /**
     * 신청 시작일
     */
    @Column(name = "aply_bgng_ymd")
    private LocalDateTime aplyBeginDate;

    /**
     * 신청 마감일
     */
    @Column(name = "aply_end_ymd")
    private LocalDateTime aplyEndDate;
}
