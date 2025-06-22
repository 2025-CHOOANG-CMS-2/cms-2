package kr.ac.dhuniv.counsel.domain;

import jakarta.persistence.*;
import kr.ac.dhuniv.empl_info.domain.EmplInfo;
import lombok.*;
import java.time.LocalTime;

@Entity
@Table(name = "cnlr_default_schd")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CnlrDefaultSchd {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "cnlr_default_id")
    private Long cnlrDefaultId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "empl_no", referencedColumnName = "empl_no")
    private EmplInfo employee;

    @Column(name = "day_of_week", nullable = false)
    private Integer dayOfWeek;

    @Column(name = "is_working_day", nullable = false)
    private Boolean isWorkingDay;
    
    @Column(name = "start_time")
    private LocalTime startTime;

    @Column(name = "end_time")
    private LocalTime endTime;

    @Builder
    public CnlrDefaultSchd(EmplInfo employee, Integer dayOfWeek, Boolean isWorkingDay, LocalTime startTime, LocalTime endTime) {
        this.employee = employee;
        this.dayOfWeek = dayOfWeek;
        this.isWorkingDay = isWorkingDay;
        this.startTime = startTime;
        this.endTime = endTime;
    }
}