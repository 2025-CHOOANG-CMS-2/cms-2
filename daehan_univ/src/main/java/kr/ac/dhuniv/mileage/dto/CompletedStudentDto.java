package kr.ac.dhuniv.mileage.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CompletedStudentDto {
	private Long stdId;                   //자동증가값
	private String stdNo;                 //학번
	private String stdNm;                 //학생 이름
	private String prgNm;                 //비고과 프로그램명
	private String cciNm;                 //핵심역량명
    private LocalDateTime completeDate;   //이수 완료일
    private BigDecimal mileageScore;      //마일리지 점수
}
