package kr.ac.dhuniv.counsel.domain;

import java.time.LocalDate;
import java.time.LocalTime;
import jakarta.persistence.*;
import kr.ac.dhuniv.empl_info.domain.EmplInfo;
import lombok.*;

@Entity
@Table(name = "cnlr_schd")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CnlrSchd {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // schd_id는 시스템에서 필요 시 생성 규칙에 따라 부여 (예: YYYYMMDD-EMPLNO)
    @Column(length = 20, nullable = false, unique = true)
    private String schdId;

    @Column(name = "day_cd")
    private LocalDate dayCode;

    @Column(name = "st_tm")
    private LocalTime startTime;

    @Column(name = "end_tm")
    private LocalTime endTime;

    // is_holiday 와 같은 플래그 컬럼 추가 고려
    // private boolean isHoliday;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "empl_no", referencedColumnName = "empl_no")
    private EmplInfo employee;
    
    @Builder
    public CnlrSchd(String schdId, LocalDate dayCode, LocalTime startTime, LocalTime endTime, EmplInfo employee) {
        this.schdId = schdId;
        this.dayCode = dayCode;
        this.startTime = startTime;
        this.endTime = endTime;
        this.employee = employee;
    }
}