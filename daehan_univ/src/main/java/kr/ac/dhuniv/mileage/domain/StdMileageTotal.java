package kr.ac.dhuniv.mileage.domain;

import jakarta.persistence.*;
import kr.ac.dhuniv.std_info.domain.StdInfo;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "std_mileage_total")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StdMileageTotal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "mlg_tot_id")
    private Long mlgTotId; // 자동 증가 PK
    
    @Column(name = "tot_code", length = 20, nullable = false, unique = true)
    private String totCode; // 비즈니스 키

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "std_id", referencedColumnName = "std_id")
    private StdInfo student;

    @Column(name = "tot_mlg_score", precision = 10, scale = 3)
    private BigDecimal totalMileageScore;

    @Column(name = "last_upd_dt")
    private LocalDateTime lastUpdated;
}