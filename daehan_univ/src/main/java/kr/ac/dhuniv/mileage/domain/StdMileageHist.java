package kr.ac.dhuniv.mileage.domain;

import jakarta.persistence.*;
import kr.ac.dhuniv.ncs.domain.NcsCmpInfo;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "std_mileage_hist")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StdMileageHist {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "mlg_id")
    private Long mlgId; // 자동 증가 PK

    @Column(name = "mlg_code", length = 20, nullable = false, unique = true)
    private String mlgCode; // 비즈니스 키

    @Column(name = "mlg_score", precision = 10, scale = 3)
    private BigDecimal mileageScore;

    @Column(name = "mlg_dt")
    private LocalDateTime mileageDate;

    @Column(name = "mlg_add_cd", length = 10)
    private String additionCode;

    @ManyToOne(fetch = FetchType.LAZY)                              //여러건의 마일리지 이력이 한건의 이수정보와 매잉됨(N:1) VS 한건의 마일리지 이력이 한건의 이수정보와 매핑됨(1:1)
    @JoinColumn(name = "cmp_id", referencedColumnName = "cmp_id")
    private NcsCmpInfo completion;

    @ManyToOne(fetch = FetchType.LAZY)                              //학생 여러명의 마일리지 이력이 한건의 프로그램 마일리지 정보와 매핑됨(N:1)
    @JoinColumn(name = "prg_id", referencedColumnName = "prg_id")
    private NcsPrgMileage programMileage;

    @ManyToOne(fetch = FetchType.LAZY)                              //학생기준 여러건의 마일리지 이력이 한건의 마일리지 총합과 매핑됨(N:1)
    @JoinColumn(name = "std_id", referencedColumnName = "std_id")
    private StdMileageTotal studentTotal;
}