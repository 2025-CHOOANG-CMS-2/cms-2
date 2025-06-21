package kr.ac.dhuniv.counsel.dto;

import lombok.Data;

@Data
public class MonthlyReservationStatusDto {
    private String date; // "YYYY-MM-DD" 형식의 날짜
    private String status; // "PENDING", "APPROVED" 등
}