// CounselorScheduleDto.java - 한 상담사의 전체 스케줄 정보를 종합한 DTO
package kr.ac.dhuniv.counsel.dto;

import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
public class CounselorScheduleDto {
    private List<DefaultScheduleDto> defaultSchedules;
    private List<ScheduleExceptionDto> exceptions;
}