// ScheduleExceptionDto.java - 특정 날짜의 예외 근무 시간을 담을 DTO
package kr.ac.dhuniv.counsel.dto;

import lombok.Data;
import java.time.LocalDate;
import java.time.LocalTime;

@Data
public class ScheduleExceptionDto {
    private LocalDate exceptionDate;
    private boolean isDayOff; // 하루 전체 휴무 여부
    private LocalTime startTime;
    private LocalTime endTime;
}