package kr.ac.dhuniv.empl_info;

import jakarta.persistence.*;
import kr.ac.dhuniv.com_info.ComInfo;
import kr.ac.dhuniv.user.UserInfo;
import lombok.*;

@Entity
@Table(name = "empl_info")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmplInfo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id; // 자동 증가 PK

    @Column(name = "empl_no", length = 20, nullable = false, unique = true)
    private String emplNo; // 비즈니스 키

    @Column(name = "empl_nm", length = 100)
    private String emplNm;

    @Column(name = "empl_stat_cd", length = 10)
    private String emplStatCd;

    @Column(name = "empl_zip", length = 5)
    private String emplZip;

    @Column(name = "empl_addr", length = 200)
    private String emplAddr;

    @Column(name = "empl_daddr", length = 200)
    private String emplDaddr;

    @Column(name = "empl_telno", length = 11)
    private String emplTelno;

    @Column(name = "empl_eml_addr", length = 320)
    private String emplEmailAddr;

    @Column(name = "use_yn", length = 1)
    private String useYn;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "desc_no")
    private ComInfo company;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id2", referencedColumnName = "user_id")
    private UserInfo user;
}
