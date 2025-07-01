package kr.ac.dhuniv.mileage.dto.student;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class MileageProgramDTO {
    private String programName;
    private BigDecimal mileagescore;
}
