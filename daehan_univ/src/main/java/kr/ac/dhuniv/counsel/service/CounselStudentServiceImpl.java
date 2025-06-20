package kr.ac.dhuniv.counsel.service;

import kr.ac.dhuniv.counsel.domain.*;
import kr.ac.dhuniv.counsel.dto.AvailableSlotsDto;
import kr.ac.dhuniv.counsel.dto.CounselorSimpleDto;
import kr.ac.dhuniv.counsel.repository.*;
import lombok.RequiredArgsConstructor;
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

    private final CnslrInfoRepository cnslrInfoRepository;
    private final CnlrDefaultSchdRepository cnlrDefaultSchdRepository;
    private final CnlrSchdRepository cnlrSchdRepository;
    private final CnslAplyRepository cnslAplyRepository;

    @Override
    public List<CounselorSimpleDto> findCounselorsByType(String counselingType) {
        // [수정] 타입 불일치 문제를 해결하기 위해 올바른 메소드를 호출합니다.
        return cnslrInfoRepository.findSimpleActiveCounselorsBySpecialty(counselingType);
    }

    @Override
    public Map<String, List<AvailableSlotsDto>> getAvailableSlotsForMonth(Integer year, Integer month, String counselingType, String counselorId) {
        
        // [수정] 필터링을 위해 올바른 메소드를 호출합니다.
        List<CounselorSimpleDto> counselors = cnslrInfoRepository.findSimpleActiveCounselorsBySpecialty(counselingType);
        
        if (!"all".equalsIgnoreCase(counselorId) && counselorId != null && !counselorId.isEmpty()) {
            counselors = counselors.stream()
                .filter(c -> c.getEmplNo().equals(counselorId))
                .collect(Collectors.toList());
        }
        
        if (counselors.isEmpty()) {
            return Collections.emptyMap();
        }
        
        List<String> targetUserIds = counselors.stream().map(CounselorSimpleDto::getEmplNo).collect(Collectors.toList());
        Map<String, String> counselorNameMap = counselors.stream().collect(Collectors.toMap(CounselorSimpleDto::getEmplNo, CounselorSimpleDto::getEmplNm));

        // 2. 이후 로직은 이전과 동일하게 진행됩니다.
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
}