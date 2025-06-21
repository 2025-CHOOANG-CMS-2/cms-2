package kr.ac.dhuniv.counsel.service;

import kr.ac.dhuniv.counsel.domain.CnslAply;
import kr.ac.dhuniv.counsel.domain.CnslRslt;
import kr.ac.dhuniv.counsel.dto.ReservationDetailDto;
import kr.ac.dhuniv.counsel.dto.MonthlyReservationStatusDto;
import kr.ac.dhuniv.counsel.dto.WriteResultRequestDto;
import kr.ac.dhuniv.counsel.mapper.CounselorScheduleMapper;
import kr.ac.dhuniv.counsel.repository.CnslAplyRepository;
import kr.ac.dhuniv.counsel.repository.CnslRsltRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CounselorDashboardServiceImpl implements CounselorDashboardService {

    private static final Logger log = LoggerFactory.getLogger(CounselorDashboardServiceImpl.class);

    // Mybatis Mapper와 JPA Repository를 함께 사용
    private final CounselorScheduleMapper scheduleMapper;
    private final CnslAplyRepository cnslAplyRepository;
    private final CnslRsltRepository cnslRsltRepository; // 결과 저장을 위해 추가

    @Override
    @Transactional(readOnly = true)
    public List<ReservationDetailDto> getDailyReservations(String emplNo, LocalDate date) {
        log.info(">> 일일 상담 예약 조회 | 상담사 ID: {}, 날짜: {}", emplNo, date);
        return scheduleMapper.findReservationsByCounselorAndDate(emplNo, date);
    }

    @Override
    @Transactional(readOnly = true)
    public List<MonthlyReservationStatusDto> getMonthlyStatus(String emplNo, int year, int month) {
        LocalDate startDate = LocalDate.of(year, month, 1);
        LocalDate endDate = startDate.withDayOfMonth(startDate.lengthOfMonth());
        log.info(">> 월별 상담 현황 요약 조회 | 상담사 ID: {}, 기간: {} ~ {}", emplNo, startDate, endDate);
        return scheduleMapper.getMonthlyReservationStatus(emplNo, startDate, endDate);
    }
    
    @Override
    @Transactional
    public void approveReservation(Long applyId, String emplNo) {
        updateReservationStatus(applyId, emplNo, "APPROVED");
    }

    @Override
    @Transactional
    public void rejectReservation(Long applyId, String emplNo) {
        updateReservationStatus(applyId, emplNo, "REJECTED");
    }

    // [추가된 핵심 로직] 상담 결과 작성
    @Override
    @Transactional
    public void writeCounselingResult(String emplNo, WriteResultRequestDto requestDto) {
        log.info(">> 상담 결과 작성 요청 | 예약 ID: {}, 작성자: {}", requestDto.getApplyId(), emplNo);
        
        // 1. 예외 처리: 원본 상담 신청 내역 조회
        CnslAply reservation = cnslAplyRepository.findById(requestDto.getApplyId())
            .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 상담 예약입니다."));

        // 2. 예외 처리: 권한 확인 (결과를 작성하려는 상담사가 실제 담당자인지)
        if (!reservation.getEmployee().getUser().getUserId().equals(emplNo)) {
            log.warn("!! 결과 작성 권한 없음 | 담당자: {}, 요청자: {}", reservation.getEmployee().getUser().getUserId(), emplNo);
            throw new SecurityException("결과를 작성할 권한이 없습니다.");
        }

        // 3. 예외 처리: 이미 결과가 작성되었는지 중복 확인
        if (cnslRsltRepository.existsByCounselingApplication_Id(reservation.getId())) {
            log.warn("!! 결과 작성 실패: 이미 결과가 존재함 | 예약 ID: {}", reservation.getId());
            throw new IllegalStateException("이미 결과가 작성된 상담입니다.");
        }

        // 4. 상담 결과(CnslRslt) 엔티티 생성
        CnslRslt result = CnslRslt.builder()
                .cnslRsltId("RSLT-" + System.currentTimeMillis()) // 임시 ID 생성
                .counselingApplication(reservation) // 어떤 상담에 대한 결과인지 연결
                .counselingDateTime(LocalDateTime.now()) // 결과 작성 시점을 상담일시로 기록
                .counselingContent(requestDto.getCounselingContent())
                .resultCode("COMPLETED") // 결과 코드
                .build();
        
        // 5. 상담 결과 저장
        cnslRsltRepository.save(result);
        log.info(".. 상담 결과 저장 완료. 결과 ID: {}", result.getCnslRsltId());

        // 6. 원래 상담 신청 건의 상태를 '완료(COMPLETED)'로 변경
        reservation.setStatusCode("COMPLETED");
        log.info(".. 예약 ID {} 상태를 COMPLETED로 변경 완료", reservation.getId());
    }

    // --- 내부용 private 메소드 ---
    private void updateReservationStatus(Long applyId, String emplNo, String newStatus) {
        log.info(".. 예약 ID {} 상태를 {}로 변경 시도", applyId, newStatus);
        CnslAply reservation = cnslAplyRepository.findById(applyId)
            .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 예약입니다."));

        if (!reservation.getEmployee().getUser().getUserId().equals(emplNo)) {
            throw new SecurityException("해당 예약을 처리할 권한이 없습니다.");
        }
        
        if (!"PENDING".equals(reservation.getStatusCode())) {
            throw new IllegalStateException("이미 처리된 예약은 상태를 변경할 수 없습니다.");
        }
        
        reservation.setStatusCode(newStatus);
        log.info(".. 예약 ID {} 상태 변경 완료", applyId);
    }
}