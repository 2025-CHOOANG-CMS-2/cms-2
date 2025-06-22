package kr.ac.dhuniv.counsel.service;

import kr.ac.dhuniv.counsel.domain.*;
import kr.ac.dhuniv.counsel.dto.AvailableSlotsDto;
import kr.ac.dhuniv.counsel.dto.CounselorSimpleDto;
import kr.ac.dhuniv.counsel.dto.CreateReservationRequestDto;
import kr.ac.dhuniv.counsel.repository.*;
import kr.ac.dhuniv.empl_info.domain.EmplInfo;
import kr.ac.dhuniv.std_info.domain.StdInfo;
import lombok.RequiredArgsConstructor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CounselStudentServiceImpl implements CounselStudentService {

	private static final Logger log = LoggerFactory.getLogger(CounselStudentServiceImpl.class);
	
    private final CnslrInfoRepository cnslrInfoRepository;
    private final CnlrDefaultSchdRepository cnlrDefaultSchdRepository;
    private final CnlrSchdRepository cnlrSchdRepository;
    private final CnslAplyRepository cnslAplyRepository;
    private final StdInfoRepository stdInfoRepository;
    private final EmplInfoRepository emplInfoRepository;

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
        List<CnslAply> bookings = cnslAplyRepository.findByEmployee_User_UserIdInAndApplyDateTimeBetween(targetUserIds, startDate.atStartOfDay(), endDate.atTime(LocalTime.MAX));

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
    @Transactional
    public void createReservation(CreateReservationRequestDto requestDto) {
        log.info(">> 상담 예약 생성 요청 시작 | 학생 ID: {}, 상담사 ID: {}", requestDto.getStdNo(), requestDto.getEmplNo());

        // --- 1. 예외 처리: DB에 해당 학생과 상담사가 존재하는지 확인 ---
        log.debug("... 학생 및 상담사 정보 조회 중 ...");
        StdInfo student = stdInfoRepository.findByUser_UserId(requestDto.getStdNo())
                .orElseThrow(() -> {
                    log.warn("!! 예약 실패: 존재하지 않는 학생 ID {}", requestDto.getStdNo());
                    return new IllegalArgumentException("학생 정보를 찾을 수 없습니다.");
                });
        
        EmplInfo counselor = emplInfoRepository.findByUser_UserId(requestDto.getEmplNo())
                .orElseThrow(() -> {
                    log.warn("!! 예약 실패: 존재하지 않는 상담사 ID {}", requestDto.getEmplNo());
                    return new IllegalArgumentException("상담사 정보를 찾을 수 없습니다.");
                });

        // --- 2. 예외 처리: 해당 시간에 이미 예약이 있는지 중복 확인 ---
        log.debug("... 예약 시간 중복 확인 중 ...");
        boolean isAlreadyBooked = cnslAplyRepository.existsByEmployee_User_UserIdAndApplyDateTime(
            requestDto.getEmplNo(),
            requestDto.getApplyDateTime()
        );
        if (isAlreadyBooked) {
            log.warn("!! 예약 실패: 예약 시간 충돌. 상담사: {}, 시간: {}", requestDto.getEmplNo(), requestDto.getApplyDateTime());
            throw new IllegalArgumentException("이미 예약된 시간입니다. 다른 시간을 선택해주세요.");
        }
        
        // --- 3. 상담 신청(CnslAply) 엔티티 생성 ---
        log.debug("... 새로운 상담 신청 엔티티 생성 ...");
        CnslAply reservation = CnslAply.builder()
                .cnslAplyId("APL-" + System.currentTimeMillis()) // 임시 비즈니스 키 생성 규칙
                .student(student)
                .employee(counselor)
                .applyDateTime(requestDto.getApplyDateTime())
                .typeCode(requestDto.getCounselingType())
                .statusCode("PENDING") // 초기 상태는 '승인대기'
                .content(requestDto.getContent())
                .build();

        // --- 4. 데이터베이스에 저장 ---
        cnslAplyRepository.save(reservation);
        log.info(">> 상담 예약 생성 완료 | 예약 ID: {}", reservation.getCnslAplyId());
    }
}