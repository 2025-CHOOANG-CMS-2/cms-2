package kr.ac.dhuniv.mileage.dto.student;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data 
@AllArgsConstructor
public class CompetencyStat {
    private String competencyName;
    private BigDecimal mileagePoint;
    private int percent;
}
