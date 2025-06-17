package kr.ac.dhuniv.counsel.domain;

import java.time.LocalDate;
import java.time.LocalTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import kr.ac.dhuniv.empl_info.domain.EmplInfo;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "cnlr_schd")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CnlrSchd {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id; // 자동 증가 PK

    @Column(name = "schd_id", length = 20, nullable = false, unique = true)
    private String schdId; // 비즈니스 키

    @Column(name = "day_cd")
    private LocalDate dayCode;

    @Column(name = "st_tm")
    private LocalTime startTime;

    @Column(name = "end_tm")
    private LocalTime endTime;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "empl_no", referencedColumnName = "empl_no")
    private EmplInfo employee;
}