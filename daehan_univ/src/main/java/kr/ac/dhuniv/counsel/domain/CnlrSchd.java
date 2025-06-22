package kr.ac.dhuniv.counsel.domain;


import jakarta.persistence.*;
import kr.ac.dhuniv.empl_info.domain.EmplInfo;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalTime;


@Entity
@Table(name = "cnlr_schd")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CnlrSchd {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    @Column(name = "schd_id", nullable = false, unique = true)
    private String schdId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "empl_no", referencedColumnName = "empl_no")
    private EmplInfo employee;


    @Column(name = "day_cd")
    private LocalDate dayCode;

    @Column(name = "st_tm")
    private LocalTime startTime;

    @Column(name = "end_tm")
    private LocalTime endTime;


    @Builder
    public CnlrSchd(String schdId, EmplInfo employee, LocalDate dayCode, LocalTime startTime, LocalTime endTime) {
        this.schdId = schdId;
        this.employee = employee;
        this.dayCode = dayCode;
        this.startTime = startTime;
        this.endTime = endTime;

    }
}