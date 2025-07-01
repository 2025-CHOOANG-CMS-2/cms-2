package kr.ac.dhuniv.mileage.dto.employee;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ProgramTopDTO {
    private String programName;
    private String coreCompetency;
    private long participatedCount;
    private BigDecimal totalMileage;
}
