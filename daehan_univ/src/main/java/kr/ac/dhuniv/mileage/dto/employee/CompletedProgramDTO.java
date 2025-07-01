package kr.ac.dhuniv.mileage.dto.employee;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CompletedProgramDTO {
	private Long prgId;        //프로그램ID
    private String programName;      //프로그램명
    private String coreCompetency;   //핵심역량
    private LocalDateTime startDate;     //프로그램 운영 시작일자
    private LocalDateTime endDate;       //프로그램 운영 종료일자
    private LocalDateTime completeDate;  //이수일자
}
