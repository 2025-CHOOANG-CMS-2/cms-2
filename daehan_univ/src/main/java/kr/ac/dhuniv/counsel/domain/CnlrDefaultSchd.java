//package kr.ac.dhuniv.counsel.domain;
//
//import java.time.LocalTime;
//import jakarta.persistence.*;
//import kr.ac.dhuniv.empl_info.domain.EmplInfo;
//import lombok.*;
//
//@Entity
//@Table(name = "cnlr_default_schd") // 테이블 이름 예시
//@Getter
//@NoArgsConstructor(access = AccessLevel.PROTECTED)
//public class CnlrDefaultSchd {
//
//    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    private Long id;
//
//    // 월요일(1) ~ 일요일(7)을 저장
//    @Column(nullable = false)
//    private Integer dayOfWeek; 
//
//    @Column
//    private LocalTime startTime;
//
//    @Column
//    private LocalTime endTime;
//
//    // 근무 여부 (체크박스)
//    @Column(nullable = false)
//    private Boolean isWorkingDay;
//
//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "empl_no", referencedColumnName = "empl_no")
//    private EmplInfo employee;
//
//    @Builder
//    public CnlrDefaultSchd(Integer dayOfWeek, LocalTime startTime, LocalTime endTime, Boolean isWorkingDay, EmplInfo employee) {
//        this.dayOfWeek = dayOfWeek;
//        this.startTime = startTime;
//        this.endTime = endTime;
//        this.isWorkingDay = isWorkingDay;
//        this.employee = employee;
//    }
//    
//    // 정보 수정을 위한 메소드
//    public void update(LocalTime startTime, LocalTime endTime, Boolean isWorkingDay) {
//        this.startTime = startTime;
//        this.endTime = endTime;
//        this.isWorkingDay = isWorkingDay;
//    }
//}