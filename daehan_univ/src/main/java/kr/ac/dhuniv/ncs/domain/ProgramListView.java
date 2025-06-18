package kr.ac.dhuniv.ncs.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Immutable;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 비교과 프로그램 목록 조회용 뷰 엔티티 (NcsPrgInfo 전체 필드 포함)
 */
@Entity
@Table(name = "v_ncs_program_list") // DB에 생성된 VIEW 이름
@Immutable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProgramListView {

    @Id
    @Column(name = "prg_id")
    private Long prgId;

    @Column(name = "image_url")
    private String imageUrl;

    @Column(name = "prg_nm")
    private String prgNm;

    @Column(name = "prg_code")
    private String prgCode;

    @Column(name = "core_cpt_name")
    private String coreCptName;

    @Column(name = "max_cnt")
    private Integer maxCnt;

    @Column(name = "mlg_score")
    private BigDecimal mileageScore;

    @Column(name = "aply_bgng_ymd")
    private LocalDateTime aplyBeginDate;

    @Column(name = "aply_end_ymd")
    private LocalDateTime aplyEndDate;

    @Column(name = "prg_st_dt")
    private LocalDateTime prgStartDate;

    @Column(name = "prg_end_dt")
    private LocalDateTime prgEndDate;

    @Column(name = "reg_user_id")
    private String regUserId;

    @Column(name = "reg_dt")
    private LocalDateTime regDt;

    @Column(name = "upd_user_id")
    private String updUserId;

    @Column(name = "upd_dt")
    private LocalDateTime updDt;

    @Column(name = "prg_desc")
    private String prgDesc;

    @Column(name = "apply_status")
    private String applyStatus;
}
