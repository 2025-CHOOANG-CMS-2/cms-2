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
    private Long stdId; // 학생 정보 테이블의 기본 키

    // 학생의 학번은 User 엔티티의 user_id를 참조하는 외래키로 사용됩니다.
    // 이는 user_info 테이블의 user_id와 std_info 테이블의 std_no가 1:1 관계를 가짐을 명시합니다.
    @OneToOne(fetch = FetchType.LAZY) // 학생 정보 로딩 시 User 정보는 필요할 때 로딩
    @JoinColumn(name = "std_no", referencedColumnName = "user_id", nullable = false, unique = true)
    // name: 현재 테이블(std_info)의 외래키 컬럼명 (std_no)
    // referencedColumnName: 참조하는 테이블(user_info)의 컬럼명 (user_id)
    private User user; // 해당 학생의 사용자 계정 정보 (이 객체의 userId가 학번 역할을 함)

    // 기존의 String stdNo 필드는 User user 필드로 대체되었으므로 제거됩니다.

    @Column(name = "std_nm", nullable = false, length = 100)
    private String stdNm; // 이름

    @Column(name = "scsbjt_cd", nullable = false, length = 20)
    private String scsbjtCd; // 학과 코드

    @Column(name = "sch_yr", nullable = false)
    private Integer schoolYear; // 학년

    @Column(name = "entr_dt", nullable = false)
    private LocalDate entranceDate; // 입학일자

    @Column(name = "std_stat_cd", nullable = false, length = 10)
    private String statusCode; // 상태 코드 (예: 재학, 휴학, 졸업)

    @Column(name = "std_zip", length = 5) // 우편번호
    private String stdZip;

    @Column(name = "std_addr", length = 200)
    private String stdAddr; // 주소

    @Column(name = "std_daddr", length = 200)
    private String stdDaddr; // 상세 주소

    @Column(name = "std_telno", length = 11)
    private String stdTelno; // 전화번호

    @Column(name = "std_eml_addr", unique = true, length = 320)
    private String stdEmlAddr; // 이메일


    @Column(name = "use_yn", nullable = false, length = 1)
    private String useYn; // 학생 정보 자체의 사용 여부 (Y/N)
    
    @Column(name = "profile_image_url", length = 500) 
    private String profileImageUrl; // 프로필 이미지 URL

    // 해당 학생 정보를 등록 또는 수정한 관리자의 사용자 ID
    @Column(name = "created_by", length = 20)
    private String createdBy;
}

