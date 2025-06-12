package kr.ac.dhuniv.std_mileage_hist;

import jakarta.persistence.*;
import kr.ac.dhuniv.ncs_cmp_info.NcsCmpInfo;
import kr.ac.dhuniv.ncs_prg_mileage.NcsPrgMileage;
import kr.ac.dhuniv.std_mileage_total.StdMileageTotal;
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
    @Column(name = "id")
    private Long id; // 자동 증가 PK

    @Column(name = "mlg_id", length = 20, nullable = false, unique = true)
    private String mlgId; // 비즈니스 키

    @Column(name = "mlg_score", precision = 10, scale = 3)
    private BigDecimal mileageScore;

    @Column(name = "mlg_dt")
    private LocalDateTime mileageDate;

    @Column(name = "mlg_add_cd", length = 10)
    private String additionCode;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cmp_id", referencedColumnName = "cmp_id")
    private NcsCmpInfo completion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "prg_id", referencedColumnName = "prg_id")
    private NcsPrgMileage programMileage;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "std_no", referencedColumnName = "std_no")
    private StdMileageTotal studentTotal;
}