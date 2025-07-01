package kr.ac.dhuniv.mileage.dto.student;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class MileageHistoryDTO {
    private LocalDateTime date;
    private String programName;
    private LocalDateTime prgStartDate;
    private LocalDateTime prgEndDate;
    private String competencyName;
    private BigDecimal mileageScore;
}