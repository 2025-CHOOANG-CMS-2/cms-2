package kr.ac.dhuniv.std_info.domain;

import jakarta.persistence.*;
import lombok.*;
import kr.ac.dhuniv.user.User; // User 엔티티 임포트
import java.time.LocalDate;

@Entity
@Table(name = "std_info")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class StdInfo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "std_id", nullable = false)
    private Long stdId;

    @Column(name = "std_no", nullable = false, unique = true, length = 20) // varchar(20)
    private String stdNo; // 학번

    @Column(name = "std_nm", nullable = false, length = 100) // varchar(100)
    private String stdNm; // 이름

    @Column(name = "scsbjt_cd", nullable = false, length = 20) // varchar(20)
    private String scsbjtCd; // 학과 코드
 
    @Column(name = "sch_yr", nullable = false) // int4
    private Integer schoolYear; // 학년

    @Column(name = "entr_dt", nullable = false) // date
    private LocalDate entranceDate; // 입학일자

    @Column(name = "std_stat_cd", nullable = false, length = 10) // varchar(10)
    private String statusCode; // 상태 코드 (재학, 휴학, 졸업 등)

    @Column(name = "std_zip", length = 5) // varchar(5) -> DB 스키마 이미지에서는 varchar(5)로 보이지만, 안전하게 7로 설정하겠습니다.
    private String stdZip; // 우편번호

    @Column(name = "std_addr", length = 200) // varchar(200)
    private String stdAddr; // 주소

    @Column(name = "std_daddr", length = 200) // varchar(200)
    private String stdDaddr; // 상세 주소

    @Column(name = "std_telno", length = 11) // varchar(11)
    private String stdTelno; // 전화번호 (이미지에서 std_telno로 확인)

    @Column(name = "std_eml_addr", unique = true, length = 320) // varchar(320) (이미지에서 std_eml_addr로 확인)
    private String stdEmlAddr; // 이메일

    @Column(name = "use_yn", nullable = false, length = 1) // varchar(1)
    private String useYn; // 사용 여부 (Y/N)
/**
    // user_info 테이블의 user_id (VARCHAR)를 참조하도록 수정합니다.
    // user_id2 컬럼은 user_info 테이블의 user_id (VARCHAR)를 참조
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_idx", referencedColumnName = "user_idx", insertable = false, updatable = false) // user_info의 user_id 컬럼을 참조 (varchar(20))\
    @Column(name = "user_idx")
    private User user; // 등록 및 수정 관리자 정보 (User 엔티티와 연관)*/

    // user_id2 값을 직접 다루기 위한 필드를 추가합니다.
    // 이는 @JoinColumn의 insertable/updatable = false 때문에 필요합니다.
    // JPA가 user 객체를 통해 user_id2를 관리하지 않고, 우리가 직접 String으로 값을 설정할 수 있도록 합니다.
    @Column(name = "user_id2", length = 20)
    private String userId2Value;

}

