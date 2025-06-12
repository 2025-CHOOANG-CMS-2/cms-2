package kr.ac.dhuniv.core_cpt_info;

import jakarta.persistence.Entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "core_cpt_info")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CoreCptInfo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id; // 자동 증가 PK

    @Column(name = "cci_id", length = 20, nullable = false, unique = true)
    private String cciId; // 비즈니스 키

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "up_cci_id", referencedColumnName = "cci_id")
    private CoreCptInfo parent; // 자기참조 관계

    @Column(name = "cci_nm", length = 100)
    private String cciNm;

    @Column(name = "cci_desc", length = 500)
    private String cciDesc;

    @Column(name = "reg_user_id", length = 20)
    private String regUserId;

    @Column(name = "reg_dt")
    private LocalDateTime regDt;

    @Column(name = "upd_user_id", length = 20)
    private String updUserId;

    @Column(name = "upd_dt")
    private LocalDateTime updDt;
}