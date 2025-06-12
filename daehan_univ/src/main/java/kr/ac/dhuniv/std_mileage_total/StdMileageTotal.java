package kr.ac.dhuniv.std_mileage_total;

import jakarta.persistence.*;
import kr.ac.dhuniv.std_info.StdInfo;
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
    @Column(name = "id")
    private Long id; // 자동 증가 PK

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "std_no", referencedColumnName = "std_no")
    private StdInfo student;

    @Column(name = "tot_mlg_score", precision = 10, scale = 3)
    private BigDecimal totalMileageScore;

    @Column(name = "last_upd_dt")
    private LocalDateTime lastUpdated;
}