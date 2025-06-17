package kr.ac.dhuniv.empl_info.domain;

import jakarta.persistence.*;
import lombok.*;
import kr.ac.dhuniv.user.User; // User 엔티티 임포트
import java.time.LocalDate;

@Entity
@Table(name = "empl_info")
@Getter
@Setter // Setter 추가 (필요에 따라)
@NoArgsConstructor(access = AccessLevel.PROTECTED) // JPA 사용을 위해 기본 생성자 필요
@AllArgsConstructor // @Builder 사용을 위해 모든 필드를 포함하는 생성자 필요
@Builder // 빌더 패턴 사용
public class EmplInfo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "empl_id", nullable = false)
    private Long emplId;

    @Column(name = "empl_no", nullable = false, unique = true)
    private String emplNo; // 사번

    @Column(name = "empl_nm", nullable = false)
    private String emplNm; // 이름

    @Column(name = "dept_cd", nullable = false)
    private String deptCd; // 부서 코드

    @Column(name = "position_cd")
    private String positionCd; // 직급

    @Column(name = "empl_stat_cd", nullable = false)
    private String emplStatCd; // 재직 상태 코드 (Y/N/L)

    @Column(name = "empl_zip")
    private String emplZip; // 우편번호

    
    @Column(name = "hire_dt") // 입사일자 필드 유지
    private LocalDate hireDt;
    
    @Column(name = "empl_addr")
    private String emplAddr; // 주소

    @Column(name = "empl_daddr")
    private String emplDaddr; // 상세 주소

    @Column(name = "empl_telno")
    private String emplTelno; // 연락처

    @Column(name = "empl_eml_addr", unique = true)
    private String emplEmailAddr; // 이메일

    @Column(name = "use_yn", length = 1, nullable = false)
    private String useYn; // 사용 여부 (Y/N)

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id2", referencedColumnName = "user_id") // user_info 테이블의 user_id 컬럼을 참조
    private User user; // 등록 및 수정 관리자 정보 (User 엔티티와 연관)
}
