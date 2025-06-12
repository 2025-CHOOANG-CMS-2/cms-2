package kr.ac.dhuniv.ncs_cmp_info;
import jakarta.persistence.*;
import kr.ac.dhuniv.ncs_prg_aply.NcsPrgAply;
import kr.ac.dhuniv.ncs_prg_info.NcsPrgInfo;
import kr.ac.dhuniv.std_info.StdInfo;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "ncs_cmp_info")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NcsCmpInfo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id; // 자동 증가 PK

    @Column(name = "cmp_id", length = 20, nullable = false, unique = true)
    private String cmpId; // 비즈니스 키

    @Column(name = "cmp_dt")
    private LocalDateTime completeDate;

    @Column(name = "cmp_stat_cd", length = 10)
    private String statusCode;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "aply_id", referencedColumnName = "aply_id")
    private NcsPrgAply application;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "prg_id", referencedColumnName = "prg_id")
    private NcsPrgInfo program;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "std_no", referencedColumnName = "std_no")
    private StdInfo student;
}
