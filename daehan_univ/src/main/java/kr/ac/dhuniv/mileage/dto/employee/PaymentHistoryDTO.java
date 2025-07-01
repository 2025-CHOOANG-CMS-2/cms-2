package kr.ac.dhuniv.mileage.dto.employee;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PaymentHistoryDTO {
    private Long mlgId;
    private LocalDateTime mileageDate;
    private String studentNo;
    private String studentName;
    private String competencyName;
    private String programName;
    private BigDecimal mileageScore;
    private String mlgStatCode;
}
