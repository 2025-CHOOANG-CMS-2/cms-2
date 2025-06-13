package kr.ac.dhuniv.ncs.domain;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "ncs_prg_mileage")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NcsPrgMileage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id; // 자동 증가 PK

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "prg_id", referencedColumnName = "prg_id")
    private NcsPrgInfo program;

    @Column(name = "mlg_score", precision = 10, scale = 3)
    private BigDecimal mileageScore;

    @Column(name = "reg_user_id", length = 20)
    private String regUserId;

    @Column(name = "reg_dt")
    private LocalDateTime regDt;

    @Column(name = "upd_user_id", length = 20)
    private String updUserId;

    @Column(name = "upd_dt")
    private LocalDateTime updDt;
}
