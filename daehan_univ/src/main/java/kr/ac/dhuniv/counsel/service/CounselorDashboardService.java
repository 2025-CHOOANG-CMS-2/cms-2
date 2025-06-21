package kr.ac.dhuniv.counsel.service;

import kr.ac.dhuniv.counsel.dto.ReservationDetailDto;
import kr.ac.dhuniv.counsel.dto.MonthlyReservationStatusDto;
import kr.ac.dhuniv.counsel.dto.WriteResultRequestDto; // 추가된 DTO import
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface CounselorDashboardService {

    /**
     * 특정 상담사의 월별 예약/일정 상태를 요약하여 조회합니다. (캘린더 색상 표시용)
     */
    List<MonthlyReservationStatusDto> getMonthlyStatus(String emplNo, int year, int month);

    /**
     * 특정 상담사의 특정 날짜 예약 목록을 상세하게 조회합니다.
     */
    List<ReservationDetailDto> getDailyReservations(String emplNo, LocalDate date);

    /**
     * 특정 상담 신청을 '승인' 상태로 변경합니다.
     */
    void approveReservation(Long applyId, String emplNo);

    /**
     * 특정 상담 신청을 '거부' 상태로 변경합니다.
     */
    void rejectReservation(Long applyId, String emplNo);

    /**
     * [추가] 상담사가 완료된 상담의 결과를 작성합니다.
     * @param emplNo 결과를 작성하는 상담사의 ID (권한 확인용)
     * @param requestDto 저장할 결과 데이터
     */
    void writeCounselingResult(String emplNo, WriteResultRequestDto requestDto);

}