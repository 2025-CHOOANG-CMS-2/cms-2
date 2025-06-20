//package kr.ac.dhuniv.empl_info.domain;
//
//import jakarta.persistence.*;
//import lombok.*;
//import kr.ac.dhuniv.user.User;
//import java.time.LocalDate;
//
//@Entity
//@Table(name = "empl_info")
//@Getter
//@Setter
//@NoArgsConstructor(access = AccessLevel.PROTECTED)
//@AllArgsConstructor
//@Builder
//public class EmplInfo {
//
//    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    @Column(name = "empl_id", nullable = false)
//    private Long emplId;
//
//    @OneToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "empl_no", referencedColumnName = "user_id", nullable = false, unique = true)
//    private User user;
//
//    @Column(name = "empl_nm", nullable = false)
//    private String emplNm;
//
//    @Column(name = "dept_cd", nullable = false)
//    private String deptCd;
//
//    @Column(name = "position_cd")
//    private String positionCd;
//
//    @Column(name = "empl_stat_cd", nullable = false)
//    private String emplStatCd;
//
//    @Column(name = "empl_zip")
//    private String emplZip;
//
//    @Column(name = "hire_dt")
//    private LocalDate hireDt;
//
//    @Column(name = "empl_addr")
//    private String emplAddr;
//
//    @Column(name = "empl_daddr")
//    private String emplDaddr;
//
//    @Column(name = "empl_telno")
//    private String emplTelno;
//
//    @Column(name = "empl_eml_addr", unique = true)
//    private String emplEmailAddr;
//
//    @Column(name = "use_yn", length = 1, nullable = false)
//    private String useYn;
//
//    @Column(name = "created_by", length = 20)
//    private String createdBy;
//
//    // --- 새로 추가되는 필드 ---
//    @Column(name = "profile_image_url", length = 500) // 프로필 이미지 URL 저장 컬럼
//    private String profileImageUrl;
//}