package kr.ac.dhuniv.std_info.domain;

import kr.ac.dhuniv.user.User; // User 엔티티 임포트
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.*;
import java.time.LocalDate;

@Entity
@Table(name = "std_info") // 실제 DB 테이블명 'std_info'
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StdInfo {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) 
    @Column(name = "std_id") 
    private Long stdId; 
    
    // 학번은 반드시 고유해야 하며 (unique = true), NULL을 허용하지 않습니다 (nullable = false).
    @Column(name = "std_no", length = 20, nullable = false, unique = true)
    private String stdNo; // <---- 학번 정보를 저장하는 필드 (YYYYNNNMMM 형식)

  
    @ManyToOne(fetch = FetchType.LAZY) // 지연 로딩 (필요할 때만 User 정보 로드)
    @JoinColumn(name = "user_id2", referencedColumnName = "user_id")
    private User user; // 등록/수정 관리자 정보를 담는 User 엔티티

    @Column(name = "std_nm", length = 100, nullable = false)
    private String stdNm;

    @Column(name = "scsbjt_cd", length = 20)
    private String scsbjtCd; // 학과 코드 (예: "001", "011")

    @Column(name = "sch_yr")
    private Integer schoolYear;

    @Column(name = "entr_dt")
    private LocalDate entranceDate;

    @Column(name = "std_stat_cd", length = 10)
    private String statusCode; // 재학 상태 코드 (예: "ENROLL", "LEAVE")

    @Column(name = "std_zip", length = 5)
    private String zip;

    @Column(name = "std_addr", length = 200)
    private String address;

    @Column(name = "std_daddr", length = 200)
    private String detailAddress;

    @Column(name = "std_telno", length = 11)
    private String tel;

    @Column(name = "std_eml_addr", unique = true, length = 320)
    private String email;

    @Column(name = "use_yn", length = 1, nullable = false)
    private String useYn;
}