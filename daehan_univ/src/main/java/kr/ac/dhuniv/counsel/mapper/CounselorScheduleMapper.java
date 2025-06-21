package kr.ac.dhuniv.counsel.mapper;

import kr.ac.dhuniv.counsel.dto.MonthlyReservationStatusDto;
import kr.ac.dhuniv.counsel.dto.ReservationDetailDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.time.LocalDate;
import java.util.List;

@Mapper
public interface CounselorScheduleMapper {
    // 특정 상담사의 특정 날짜 예약 목록 조회
    List<ReservationDetailDto> findReservationsByCounselorAndDate(
        @Param("emplNo") String emplNo,
        @Param("date") LocalDate date
    );
    
    List<MonthlyReservationStatusDto> getMonthlyReservationStatus(
            @Param("emplNo") String emplNo,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
        );
}