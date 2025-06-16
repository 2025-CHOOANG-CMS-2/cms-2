package kr.ac.dhuniv.mileage.domain;

import jakarta.persistence.*;
import kr.ac.dhuniv.ncs.domain.NcsPrgInfo;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 비교과 프로그램 마일리지 정보 엔티티
 * <p>
 * NCS_PRG_MILEAGE 테이블 매핑
 */
@Entity
@Table(name = "ncs_prg_mileage")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NcsPrgMileage {

    /**
     * 마일리지 정보 ID (기본 키, 자동 증가)
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "prg_mlg_id")
    private Long prgMlgId;

    /**
     * 마일리지 고유 코드 (비즈니스 키, 유니크)
     */
    @Column(name = "prg_mlg_code", length = 20, unique = true)
    private String prgMlgCode;

    /**
     * 연관된 비교과 프로그램 정보
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "prg_id", referencedColumnName = "prg_id")
    private NcsPrgInfo program;

    /**
     * 마일리지 점수 (소수점 3자리까지 허용)
     */
    @Column(name = "mlg_score", precision = 10, scale = 3)
    private BigDecimal mileageScore;

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
}
