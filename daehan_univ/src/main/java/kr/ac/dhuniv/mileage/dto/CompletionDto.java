package kr.ac.dhuniv.mileage.dto;

import java.time.LocalDate;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CompletionDto {
    private Long cmpId;
    private Long stdId;
    private String stdNo;
    private String studentName;
    private String programName;
    private String coreCompetency;
    private LocalDate completeDate;
    private Boolean mileagePaid;
    private Integer mileage;
}
