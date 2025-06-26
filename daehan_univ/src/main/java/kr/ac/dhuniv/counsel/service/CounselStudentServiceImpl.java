package kr.ac.dhuniv.counsel.service;

import kr.ac.dhuniv.counsel.domain.*;
import kr.ac.dhuniv.counsel.dto.AvailableSlotsDto;
import kr.ac.dhuniv.counsel.dto.CounselingHistoryDto;
import kr.ac.dhuniv.counsel.dto.CounselorSimpleDto;
import kr.ac.dhuniv.counsel.dto.CreateReservationRequestDto;
import kr.ac.dhuniv.counsel.dto.PageDto;
import kr.ac.dhuniv.counsel.mapper.CounselingHistoryMapper;
import kr.ac.dhuniv.counsel.repository.*;
import kr.ac.dhuniv.empl_info.domain.EmplInfo;
import kr.ac.dhuniv.std_info.domain.StdInfo;
import lombok.RequiredArgsConstructor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityNotFoundException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CounselStudentServiceImpl implements CounselStudentService {

	private static final Logger log = LoggerFactory.getLogger(CounselStudentServiceImpl.class);
	
    private final CnslrInfoRepository cnslrInfoRepository;
    private final CnlrDefaultSchdRepository cnlrDefaultSchdRepository;
    private final CnlrSchdRepository cnlrSchdRepository;
    private final CnslAplyRepository cnslAplyRepository;
    private final StdInfoRepository stdInfoRepository;
    private final EmplInfoRepository emplInfoRepository;
    private final CounselingHistoryMapper counselingHistoryMapper;
    private final CnslRsltRepository cnslRsltRepository;

    @Override
    public List<CounselorSimpleDto> findCounselorsByType(String counselingType) {
    	if ("all".equalsIgnoreCase(counselingType)) {
            // '전체 유형'일 경우, 새로 만든 메소드를 호출합니다.
            return cnslrInfoRepository.findAllSimpleActiveCounselors();
        } else {
            // 특정 유형일 경우, 기존 메소드를 호출합니다.
            return cnslrInfoRepository.findSimpleActiveCounselorsBySpecialty(counselingType);
        }
    }

    @Override
    public Map<String, List<AvailableSlotsDto>> getAvailableSlotsForMonth(Integer year, Integer month, String counselingType, String counselorId) {
        
        // [수정] 필터링을 위한 단 하나의 메소드를 호출하여 코드를 명확하게 변경
        List<CounselorSimpleDto> counselors = cnslrInfoRepository.findSimpleActiveCounselorsByFilter(counselingType, counselorId);
        
        if (counselors.isEmpty()) {
            return Collections.emptyMap();
        }

        List<String> targetUserIds = counselors.stream().map(CounselorSimpleDto::getEmplNo).collect(Collectors.toList());
        Map<String, String> counselorNameMap = counselors.stream().collect(Collectors.toMap(CounselorSimpleDto::getEmplNo, CounselorSimpleDto::getEmplNm));

        LocalDate startDate = LocalDate.of(year, month, 1);
        LocalDate endDate = startDate.withDayOfMonth(startDate.lengthOfMonth());
        
        List<CnlrDefaultSchd> defaults = cnlrDefaultSchdRepository.findByEmployee_User_UserIdIn(targetUserIds);
        List<CnlrSchd> exceptions = cnlrSchdRepository.findByEmployee_User_UserIdInAndDayCodeBetween(targetUserIds, startDate, endDate);
        List<String> blockingStatusCodes = Arrays.asList("PENDING", "APPROVED");
        List<CnslAply> bookings = cnslAplyRepository.findByEmployee_User_UserIdInAndApplyDateTimeBetweenAndStatusCodeIn(
                targetUserIds,
                startDate.atStartOfDay(),
                endDate.atTime(LocalTime.MAX),
                blockingStatusCodes
            );

        Map<String, List<CnlrDefaultSchd>> defaultMap = defaults.stream().collect(Collectors.groupingBy(d -> d.getEmployee().getUser().getUserId()));
        Map<String, CnlrSchd> exceptionMap = exceptions.stream()
                .collect(Collectors.toMap(e -> e.getEmployee().getUser().getUserId() + ":" + e.getDayCode(), Function.identity(), (e1, e2) -> e1));
        
        Set<LocalDateTime> bookedSlots = bookings.stream().map(CnslAply::getApplyDateTime).collect(Collectors.toSet());

        Map<String, List<AvailableSlotsDto>> result = new LinkedHashMap<>();
        for (LocalDate day = startDate; !day.isAfter(endDate); day = day.plusDays(1)) {
            List<AvailableSlotsDto> dailySlots = new ArrayList<>();
            for (String userId : targetUserIds) {
                String counselorName = counselorNameMap.get(userId);
                CnlrSchd exception = exceptionMap.get(userId + ":" + day);
                LocalTime startTime, endTime;

                if (exception != null) {
                    startTime = exception.getStartTime();
                    endTime = exception.getEndTime();
                } else {
                    int dayOfWeek = day.getDayOfWeek().getValue();
                    CnlrDefaultSchd defaultSchd = defaultMap.getOrDefault(userId, Collections.emptyList()).stream()
                        .filter(d -> d.getDayOfWeek() == dayOfWeek && d.getIsWorkingDay())
                        .findFirst().orElse(null);
                    
                    if (defaultSchd == null) continue;
                    
                    startTime = defaultSchd.getStartTime();
                    endTime = defaultSchd.getEndTime();
                }

                if (startTime == null || endTime == null) continue;

                for (LocalTime slot = startTime; slot.isBefore(endTime); slot = slot.plusHours(1)) {
                    if (!bookedSlots.contains(LocalDateTime.of(day, slot))) {
                        dailySlots.add(new AvailableSlotsDto(userId, counselorName, slot.toString()));
                    }
                }
            }
            if (!dailySlots.isEmpty()) {
                result.put(day.toString(), dailySlots);
            }
        }
        return result;
    }
    
    @Override
    @Transactional // 하나의 트랜잭션으로 묶여 원자성을 보장합니다.
    public void createReservation(CreateReservationRequestDto requestDto) {
        log.info(">> 상담 예약 생성/변경 요청 시작 | 학생 ID: {}", requestDto.getStdNo());

        // [추가] 예약 변경 로직: originalApplyId가 있다면 기존 예약을 먼저 취소 처리합니다.
        if (requestDto.getOriginalApplyId() != null) {
            log.info(".. 예약 변경 시작. 기존 예약 ID {}를 취소합니다.", requestDto.getOriginalApplyId());
            
            // 이전에 만들었던 상태 변경 로직을 재사용하여, 자신의 예약이 맞는지 확인 후 취소합니다.
            // "CANCELED_BY_MODIFY" 와 같은 상태 코드를 사용하면 나중에 추적하기 좋습니다.
            updateReservationStatus(requestDto.getStdNo(), requestDto.getOriginalApplyId(), "CANCELED");
        }

        // --- 여기서부터는 기존의 신규 예약 생성 로직과 동일합니다. ---
        
        // 1. 예외 처리: DB에 해당 학생과 상담사가 존재하는지 확인
        log.debug("... 학생 및 상담사 정보 조회 중 ...");
        StdInfo student = stdInfoRepository.findByUser_UserId(requestDto.getStdNo())
                .orElseThrow(() -> new IllegalArgumentException("학생 정보를 찾을 수 없습니다."));
        
        EmplInfo counselor = emplInfoRepository.findByUser_UserId(requestDto.getEmplNo())
                .orElseThrow(() -> new IllegalArgumentException("상담사 정보를 찾을 수 없습니다."));

        // 2. 예외 처리: 해당 시간에 이미 예약이 있는지 중복 확인
        log.debug("... 예약 시간 중복 확인 중 ...");
        boolean isAlreadyBooked = cnslAplyRepository.existsByEmployee_User_UserIdAndApplyDateTime(
            requestDto.getEmplNo(),
            requestDto.getApplyDateTime()
        );
        if (isAlreadyBooked) {
            log.warn("!! 예약 실패: 예약 시간 충돌. 상담사: {}, 시간: {}", requestDto.getEmplNo(), requestDto.getApplyDateTime());
            throw new IllegalArgumentException("이미 예약된 시간입니다. 다른 시간을 선택해주세요.");
        }
        
        // 3. 상담 신청(CnslAply) 엔티티 생성
        log.debug("... 새로운 상담 신청 엔티티 생성 ...");
        CnslAply newReservation = CnslAply.builder()
                .cnslAplyId("APL-" + System.currentTimeMillis())
                .student(student)
                .employee(counselor)
                .applyDateTime(requestDto.getApplyDateTime())
                .requestDateTime(LocalDateTime.now())
                .typeCode(requestDto.getCounselingType())
                .statusCode("PENDING")
                .content(requestDto.getContent())
                .methodCode(requestDto.getCounselingMethod())
                .build();

        // 4. 데이터베이스에 저장
        cnslAplyRepository.save(newReservation);
        log.info(">> 새로운 상담 예약 생성 완료 | 예약 ID: {}", newReservation.getCnslAplyId());
    }
    
    @Override
    public PageDto<CounselingHistoryDto> getCounselingHistory(String studentId, Pageable pageable, String period, String status, String type) {
        // 1. 디버깅을 위해 로그를 남깁니다.
        log.info(">> 페이징된 상담 내역 조회 | 학생 ID: {}, 페이지 요청: {}페이지, {}개씩", 
                 studentId, pageable.getPageNumber(), pageable.getPageSize());
        
        // 2. 기간 필터 값을 실제 날짜로 변환합니다.
        LocalDateTime startDate = null;
        if ("week".equals(period)) startDate = LocalDateTime.now().minusWeeks(1);
        else if ("month".equals(period)) startDate = LocalDateTime.now().minusMonths(1);
        else if ("quarter".equals(period)) startDate = LocalDateTime.now().minusMonths(3);
        else if ("year".equals(period)) startDate = LocalDateTime.now().minusYears(1);

        // 3. Mybatis 매퍼를 두 번 호출합니다.
        // 3-1. 필터 조건에 맞는 전체 데이터 개수부터 조회합니다.
        long totalElements = counselingHistoryMapper.countByStudentIdAndFilters(studentId, startDate, status, type);

        // 3-2. 실제 현재 페이지에 보여줄 목록을 조회합니다. (LIMIT, OFFSET 적용)
        List<CounselingHistoryDto> content = counselingHistoryMapper.findByStudentIdAndFilters(
            studentId,
            startDate,
            status,
            type,
            (int) pageable.getOffset(), // 건너뛸 개수
            pageable.getPageSize()     // 가져올 개수
        );
        
        log.info(">> 페이징 조회 완료 | 총 {}건 중 {}건 조회", totalElements, content.size());

        // 4. 조회된 두 가지 정보를 바탕으로 최종 PageDto 객체를 생성하여 반환합니다.
        return new PageDto<>(content, pageable.getPageNumber(), pageable.getPageSize(), totalElements);
    }
    
    /**
     * [추가] 학생이 자신의 상담 예약을 취소하는 기능의 실제 구현
     */
    @Override
    @Transactional // 데이터를 변경하는 작업이므로 @Transactional을 붙여줍니다.
    public void updateReservationStatus(String studentId, Long applyId, String newStatus) {
        // 1. 디버깅을 위해 로그를 남깁니다.
        log.info(">> 예약 상태 변경 요청 | 예약 ID: {}, 변경 상태: {}, 요청 학생: {}", applyId, newStatus, studentId);

        // 2. DB에서 해당 예약 정보를 조회합니다.
        CnslAply reservation = cnslAplyRepository.findById(applyId)
                // 3. 예외 처리 1: 예약 정보가 없으면 에러를 발생시킵니다.
                .orElseThrow(() -> {
                    log.warn("!! 예약 상태 변경 실패: 존재하지 않는 예약 ID {}", applyId);
                    return new IllegalArgumentException("존재하지 않는 예약입니다.");
                });

        // 4. 예외 처리 2: 권한 확인 (요청한 학생의 예약이 맞는지 확인)
        if (!reservation.getStudent().getUser().getUserId().equals(studentId)) {
            log.warn("!! 예약 상태 변경 권한 없음 | 예약 소유자: {}, 요청자: {}", reservation.getStudent().getUser().getUserId(), studentId);
            throw new SecurityException("자신의 예약만 변경/취소할 수 있습니다.");
        }

        // 5. 예외 처리 3: 이미 완료/취소된 예약은 변경할 수 없도록 막습니다.
        String currentStatus = reservation.getStatusCode();
        if ("COMPLETED".equals(currentStatus) || "CANCELED".equals(currentStatus) || "REJECTED".equals(currentStatus)) {
            log.warn("!! 예약 상태 변경 실패: 이미 처리된 예약 | 예약 ID: {}, 현재 상태: {}", applyId, currentStatus);
            throw new IllegalStateException("이미 처리되었거나 취소된 예약은 상태를 변경할 수 없습니다.");
        }

        // 6. 모든 확인이 끝나면, 예약 상태를 새로운 상태로 변경합니다.
        reservation.setStatusCode(newStatus);
        log.info(".. 예약 ID {}의 상태를 {}로 변경 완료", applyId, newStatus);
        
        // 7. @Transactional 어노테이션 덕분에, 메소드가 끝나면 변경된 내용이 자동으로 DB에 UPDATE 됩니다.
    }
    
    @Override
    @Transactional(readOnly = true)
    public CounselingHistoryDto getCounselingResultDetail(Long resultId) {
        // Mapper에 findByResultId 메소드를 새로 만들어 호출합니다.
        // 결과가 없을 경우 예외를 발생시킵니다.
        return counselingHistoryMapper.findByResultId(resultId)
                .orElseThrow(() -> new IllegalArgumentException("해당 상담 결과를 찾을 수 없습니다. ID: " + resultId));
    }
    
    @Override
    @Transactional
    public void updateSatisfactionScore(Long resultId, Double score) {
        if (score < 1 || score > 5) {
            throw new IllegalArgumentException("점수는 1점에서 5점 사이여야 합니다.");
        }

        // [수정] 올바른 리포지토리 메소드 이름으로 호출합니다.
        CnslRslt cnslRslt = cnslRsltRepository.findById(resultId)
                .orElseThrow(() -> new EntityNotFoundException("해당 상담 결과를 찾을 수 없습니다: " + resultId));

        // 만족도 점수를 업데이트합니다.
        cnslRslt.setSatisfactionScore(new BigDecimal(score));
    }
}