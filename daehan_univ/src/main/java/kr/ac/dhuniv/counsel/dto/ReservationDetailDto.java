package kr.ac.dhuniv.counsel.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ReservationDetailDto {
    private Long applyId; // cnsl_aply 테이블의 PK
    private String studentName;
    private String studentId;
    private String counselingType;
    private String counselingMethod;
    private String status;
    private LocalDateTime applyDateTime;
    private String content;
}