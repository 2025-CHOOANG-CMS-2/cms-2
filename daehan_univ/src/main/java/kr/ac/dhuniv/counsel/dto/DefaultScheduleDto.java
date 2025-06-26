// DefaultScheduleDto.java - 요일별 기본 근무 시간을 담을 DTO
package kr.ac.dhuniv.counsel.dto;

import lombok.Data;
import java.time.LocalTime;

@Data
public class DefaultScheduleDto {
    private Integer dayOfWeek; // 1(월요일) ~ 7(일요일)
    private boolean isWorkingDay;
    private LocalTime startTime;
    private LocalTime endTime;
}