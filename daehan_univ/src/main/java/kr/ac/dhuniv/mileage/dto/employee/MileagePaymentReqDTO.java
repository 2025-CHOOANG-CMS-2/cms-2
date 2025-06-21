package kr.ac.dhuniv.mileage.dto.employee;

import java.math.BigDecimal;

import lombok.Data;

@Data
public class MileagePaymentReqDTO {
    private Long stdId;
    private Long prgId;
    private Long cmpId;
    private BigDecimal mileageScore;
}
