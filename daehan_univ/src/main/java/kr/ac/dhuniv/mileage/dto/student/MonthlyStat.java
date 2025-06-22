package kr.ac.dhuniv.mileage.dto.student;

import java.math.BigDecimal;
import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data 
@AllArgsConstructor
public class MonthlyStat {
	private LocalDate periodStart;
	private LocalDate periodEnd;
    private BigDecimal earned;
    private BigDecimal used;
    private BigDecimal earnedDiff;
    private BigDecimal usedDiff;
}
