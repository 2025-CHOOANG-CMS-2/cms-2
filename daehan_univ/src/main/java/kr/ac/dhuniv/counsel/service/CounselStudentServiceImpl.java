package kr.ac.dhuniv.counsel.service;

import kr.ac.dhuniv.counsel.domain.CnlrDefaultSchd;
import kr.ac.dhuniv.counsel.domain.CnlrSchd;
import kr.ac.dhuniv.counsel.domain.CnslAply;
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
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CounselStudentServiceImpl implements CounselStudentService {

    // 필요한 Repository들을 주입받습니다.
    private final CnslrInfoRepository cnslrInfoRepository;
    private final CnlrDefaultSchdRepository cnlrDefaultSchdRepository;
    private final CnlrSchdRepository cnlrSchdRepository;
    private final CnslAplyRepository cnslAplyRepository;

    @Override
    public List<CounselorSimpleDto> findCounselorsByType(String counselingType) {
        // "all"은 모든 상담 유형을 의미하므로, specialty 파라미터에 "all"을 그대로 넘겨 처리합니다.
        return cnslrInfoRepository.findActiveCounselorsByFilter(counselingType, "all");
    }

    @Override
    public Map<String, List<AvailableSlotsDto>> getAvailableSlotsForMonth(Integer year, Integer month, String counselingType, String counselorId) {
        
        // 1. 필터에 맞는 상담사 목록(ID, 이름) 조회
        List<CounselorSimpleDto> counselors = cnslrInfoRepository.findActiveCounselorsByFilter(counselingType, counselorId);
        if (counselors.isEmpty()) {
            return Collections.emptyMap();
        }
        List<String> targetEmplNos = counselors.stream().map(CounselorSimpleDto::getEmplNo).collect(Collectors.toList());
        Map<String, String> counselorNameMap = counselors.stream().collect(Collectors.toMap(CounselorSimpleDto::getEmplNo, CounselorSimpleDto::getEmplNm));

        // 2. 해당 월의 시작일과 종료일 계산
        LocalDate startDate = LocalDate.of(year, month, 1);
        LocalDate endDate = startDate.withDayOfMonth(startDate.lengthOfMonth());

        // 3. 필요한 모든 데이터를 DB에서 한 번에 가져옴
        List<CnlrDefaultSchd> defaults = cnlrDefaultSchdRepository.findByEmployee_EmplNoIn(targetEmplNos);
        List<CnlrSchd> exceptions = cnlrSchdRepository.findByEmployee_EmplNoInAndDayCodeBetween(targetEmplNos, startDate, endDate);
        List<CnslAply> bookings = cnslAplyRepository.findByEmployee_EmplNoInAndApplyDateTimeBetween(targetEmplNos, startDate.atStartOfDay(), endDate.atTime(LocalTime.MAX));

        // 4. 다루기 쉬운 형태로 데이터 가공
        Map<String, List<CnlrDefaultSchd>> defaultMap = defaults.stream().collect(Collectors.groupingBy(d -> d.getEmployee().getEmplNo()));
        Map<String, CnlrSchd> exceptionMap = exceptions.stream().collect(Collectors.toMap(e -> e.getEmployee().getEmplNo() + ":" + e.getDayCode(), e -> e));
        Set<LocalDateTime> bookedSlots = bookings.stream().map(CnslAply::getApplyDateTime).collect(Collectors.toSet());

        // 5. 최종 결과물(Map) 생성
        Map<String, List<AvailableSlotsDto>> result = new LinkedHashMap<>();
        for (LocalDate day = startDate; !day.isAfter(endDate); day = day.plusDays(1)) {
            List<AvailableSlotsDto> dailySlots = new ArrayList<>();
            for (String emplNo : targetEmplNos) {
                String counselorName = counselorNameMap.get(emplNo);
                CnlrSchd exception = exceptionMap.get(emplNo + ":" + day);
                LocalTime startTime, endTime;

                if (exception != null) {
                    startTime = exception.getStartTime();
                    endTime = exception.getEndTime();
                } else {
                    int dayOfWeek = day.getDayOfWeek().getValue();
                    CnlrDefaultSchd defaultSchd = defaultMap.getOrDefault(emplNo, Collections.emptyList()).stream()
                        .filter(d -> d.getDayOfWeek() == dayOfWeek && d.getIsWorkingDay())
                        .findFirst().orElse(null);
                    
                    if (defaultSchd == null) continue;
                    startTime = defaultSchd.getStartTime();
                    endTime = defaultSchd.getEndTime();
                }

                if (startTime == null || endTime == null) continue;

                for (LocalTime slot = startTime; slot.isBefore(endTime); slot = slot.plusHours(1)) {
                    if (!bookedSlots.contains(LocalDateTime.of(day, slot))) {
                        dailySlots.add(new AvailableSlotsDto(emplNo, counselorName, slot.toString()));
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