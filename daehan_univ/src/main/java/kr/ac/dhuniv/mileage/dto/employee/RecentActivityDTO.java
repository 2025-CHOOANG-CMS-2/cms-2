package kr.ac.dhuniv.mileage.dto.employee;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class RecentActivityDTO {
	// 타입 유의할 것 : long, BigDecimal
    private LocalDateTime date;
    private String paymentType; // "일괄지급" or "개별지급"
    private long programCount; // 일괄일 경우 프로그램 갯수 
    private long studentCount; // 일괄일 경우 학생수 
    private String programName; // 프로그램명 중 사전순으로 첫번째 프로그램명
    private String studentName; // 학생 이름 중 사전순으로 첫번재 이름
    private BigDecimal totalMileage; //총 마일리지 합계
}
