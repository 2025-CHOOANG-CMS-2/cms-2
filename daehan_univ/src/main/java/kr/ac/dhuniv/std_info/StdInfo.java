package kr.ac.dhuniv.std_info;

import jakarta.persistence.*;
import kr.ac.dhuniv.user.User;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "std_info")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StdInfo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id; // 자동 증가 PK

    @Column(name = "std_no", length = 20, nullable = false, unique = true)
    private String stdNo; // 비즈니스 키

    @Column(name = "std_nm", length = 100)
    private String stdNm;

    @Column(name = "scsbjt_cd", length = 20)
    private String scsbjtCd;

    @Column(name = "sch_yr")
    private Integer schoolYear;

    @Column(name = "entr_dt")
    private LocalDateTime entranceDate;

    @Column(name = "std_stat_cd", length = 10)
    private String statusCode;

    @Column(name = "std_zip", length = 5)
    private String zip;

    @Column(name = "std_addr", length = 200)
    private String address;

    @Column(name = "std_daddr", length = 200)
    private String detailAddress;

    @Column(name = "std_telno", length = 11)
    private String tel;

    @Column(name = "std_eml_addr", length = 320)
    private String email;

    @Column(name = "use_yn", length = 1)
    private String useYn;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id2", referencedColumnName = "user_id")
    private User user;
}