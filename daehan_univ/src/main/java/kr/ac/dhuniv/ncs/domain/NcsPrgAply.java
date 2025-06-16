package kr.ac.dhuniv.ncs.domain;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import kr.ac.dhuniv.std_info.domain.StdInfo;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 비교과 프로그램 신청 정보 엔티티
 * <p>
 * NCS_PRG_APLY 테이블 매핑
 */
@Entity
@Table(name = "ncs_prg_aply")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NcsPrgAply {

    /**
     * 신청 ID (기본 키, 자동 증가)
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "aply_id")
    private Long aplyId;

    /**
     * 신청 고유 코드 (비즈니스 키, 유니크)
     */
    @Column(name = "aply_code", length = 20, unique = true)
    private String aplyCode;

    /**
     * 연관된 비교과 프로그램 정보
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "prg_id")
    private NcsPrgInfo program;

    /**
     * 신청한 학생 정보 (STD_INFO 참조)
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "std_id")
    private StdInfo student;

    /**
     * 신청 구분 코드
     */
    @Column(name = "aply_sel_cd", length = 10)
    private String applySelectCode;

    /**
     * 신청 일자
     */
    @Column(name = "aply_dt")
    private LocalDateTime applyDate;

    /**
     * 상태 코드
     */
    @Column(name = "aply_stat_cd", length = 10)
    private String applyStatusCode;
}
