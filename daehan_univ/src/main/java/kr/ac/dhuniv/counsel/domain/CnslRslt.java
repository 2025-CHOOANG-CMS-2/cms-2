package kr.ac.dhuniv.counsel.domain;


import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "cnsl_rslt")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CnslRslt {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id; // 자동 증가 PK

    @Column(name = "cnsl_rslt_id", length = 20, nullable = false, unique = true)
    private String cnslRsltId; // 비즈니스 키

    @Column(name = "cnsl_dttm")
    private LocalDateTime counselingDateTime;

    @Column(name = "cnsl_cn", columnDefinition = "TEXT")
    private String counselingContent;

    @Column(name = "satisf_score", precision = 10, scale = 3)
    private BigDecimal satisfactionScore;

    @Column(name = "rslt_cd", length = 10)
    private String resultCode;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cnsl_aply_id", referencedColumnName = "cnsl_aply_id")
    private CnslAply counselingApplication;
}