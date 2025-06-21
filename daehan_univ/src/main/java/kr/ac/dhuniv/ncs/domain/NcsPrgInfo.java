package kr.ac.dhuniv.ncs.domain;

import jakarta.persistence.*;
import kr.ac.dhuniv.core_cpt.domain.CoreCptInfo;
import kr.ac.dhuniv.mileage.domain.NcsPrgMileage;
import lombok.*;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * NCS 비교과 프로그램 정보 엔티티
 * <p>
 * NCS_PRG_INFO 테이블 매핑
 */
@Entity
@Table(name = "ncs_prg_info")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NcsPrgInfo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "prg_id")
    private Long prgId;

    @Column(name = "prg_code", length = 20, unique = true, nullable = false)
    private String prgCode;

    @Column(name = "prg_nm", length = 100, nullable = false)
    private String prgNm;

    @Column(name = "prg_desc", length = 1000)
    private String prgDesc;

    @Column(name = "max_cnt")
    private Integer maxCnt;

    @Column(name = "aply_bgng_ymd")
    private LocalDateTime aplyBgngYmd;

    @Column(name = "aply_end_ymd")
    private LocalDateTime aplyEndYmd;

    @Column(name = "prg_st_dt")
    private LocalDateTime prgStDt;

    @Column(name = "prg_end_dt")
    private LocalDateTime prgEndDt;

    @Column(name = "image_url", length = 255)
    private String imageUrl;

    @Column(name = "reg_dt")
    private LocalDateTime regDt;

    @Column(name = "upd_dt")
    private LocalDateTime updDt;

    @Column(name = "reg_user_id", length = 20)
    private String regUserId;

    @Column(name = "upd_user_id", length = 20)
    private String updUserId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cci_id", foreignKey = @ForeignKey(name = "fk_prg_info_category"))
    private CoreCptInfo coreCpt;
    
    @JsonProperty("categoryName")
    public String getCategoryName() {
        return (coreCpt != null ? coreCpt.getCciNm() : null);
    }
    
    @OneToOne(mappedBy = "program", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private NcsPrgMileage mileage;
}
