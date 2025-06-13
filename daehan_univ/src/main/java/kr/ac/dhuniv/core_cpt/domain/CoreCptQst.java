package kr.ac.dhuniv.core_cpt.domain;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "core_cpt_qst")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CoreCptQst {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id; // 자동 증가 PK

    @Column(name = "qst_id", length = 20, nullable = false, unique = true)
    private String qstId; // 비즈니스 키

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cci_id")
    private CoreCptInfo coreCptInfo; //연관된 핵심역량

    @Column(name = "qst_cont", length = 500)
    private String qstCont; //문항 내용

    @Column(name = "qst_ord")
    private Integer qstOrd; //문항 순서

    @Column(name = "reg_user_id", length = 20)
    private String regUserId; //등록자 ID

    @Column(name = "reg_dt")
    private LocalDateTime regDt; //등록일

    @Column(name = "upd_user_id", length = 20)
    private String updUserId; //수정자 ID

    @Column(name = "upd_dt")
    private LocalDateTime updDt; //수정일
}