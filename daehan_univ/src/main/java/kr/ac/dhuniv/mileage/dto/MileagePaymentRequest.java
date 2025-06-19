package kr.ac.dhuniv.mileage.dto;

import java.math.BigDecimal;

import lombok.Data;

@Data
public class MileagePaymentRequest {
    private Long stdId;
    private Long prgId;
    private Long cmpId;
    private BigDecimal mileageScore;
}
