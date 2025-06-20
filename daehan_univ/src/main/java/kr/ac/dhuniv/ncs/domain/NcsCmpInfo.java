//package kr.ac.dhuniv.ncs.domain;
//
//import jakarta.persistence.*;
//import kr.ac.dhuniv.std_info.domain.StdInfo;
//import lombok.*;
//import java.time.LocalDateTime;
//
///**
// * 비교과 프로그램 이수 정보 엔티티
// * <p>
// * NCS_CMP_INFO 테이블 매핑
// */
//@Entity
//@Table(name = "ncs_cmp_info")
//@Getter
//@Setter
//@NoArgsConstructor
//@AllArgsConstructor
//@Builder
//public class NcsCmpInfo {
//
//    /**
//     * 이수 ID (기본 키, 자동 증가)
//     */
//    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    @Column(name = "cmp_id")
//    private Long cmpId;
//
//    /**
//     * 이수 고유 코드 (비즈니스 키, 유니크)
//     */
//    @Column(name = "cmp_code", length = 20, unique = true)
//    private String cmpCode;
//
//    /**
//     * 연관된 비교과 프로그램 정보
//     */
//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "prg_id", referencedColumnName = "prg_id")
//    private NcsPrgInfo program;
//
//    /**
//     * 연관된 신청 정보
//     */
//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "aply_id", referencedColumnName = "aply_id")
//    private NcsPrgAply application;
//
//    /**
//     * 학생 정보
//     */
//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "std_id", referencedColumnName = "std_id")
//    private StdInfo student;
//
//    /**
//     * 이수 일자
//     */
//    @Column(name = "cmp_dt")
//    private LocalDateTime completeDate;
//
//    /**
//     * 이수 결과 코드
//     */
//    @Column(name = "cmp_stat_cd", length = 10)
//    private String statusCode;
//}
