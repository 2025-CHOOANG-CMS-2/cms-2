package kr.ac.dhuniv.ncs.domain;

import jakarta.persistence.*;
import kr.ac.dhuniv.std_info.StdInfo;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "ncs_prg_aply")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NcsPrgAply {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id; // 자동 증가 PK

    @Column(name = "aply_id", length = 20, nullable = false, unique = true)
    private String aplyId; // 비즈니스 키

    @Column(name = "aply_sel_cd", length = 10)
    private String selectionCode;

    @Column(name = "aply_dt")
    private LocalDateTime applyDate;

    @Column(name = "aply_stat_cd", length = 10)
    private String statusCode;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "prg_id", referencedColumnName = "prg_id")
    private NcsPrgInfo program;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "std_no", referencedColumnName = "std_no")
    private StdInfo student;
}