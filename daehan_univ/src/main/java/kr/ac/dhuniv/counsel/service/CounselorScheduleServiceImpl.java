package kr.ac.dhuniv.counsel.service;

import kr.ac.dhuniv.counsel.domain.CnlrDefaultSchd;
import kr.ac.dhuniv.counsel.domain.CnlrSchd;
import kr.ac.dhuniv.counsel.domain.CnslrInfo; // [추가] 상담사 정보 조회를 위해 import
import kr.ac.dhuniv.counsel.dto.CounselorScheduleDto;
import kr.ac.dhuniv.counsel.dto.DefaultScheduleDto;
import kr.ac.dhuniv.counsel.dto.ScheduleExceptionDto;
import kr.ac.dhuniv.counsel.repository.CnlrDefaultSchdRepository; // [오류] 다음 단계에서 생성 예정
import kr.ac.dhuniv.counsel.repository.CnlrSchdRepository;         // [오류] 다음 단계에서 생성 예정
import kr.ac.dhuniv.counsel.repository.CnslrInfoRepository;       // [오류] 다음 단계에서 생성 예정
import kr.ac.dhuniv.empl_info.domain.EmplInfo;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CounselorScheduleServiceImpl implements CounselorScheduleService {

    private static final Logger log = LoggerFactory.getLogger(CounselorScheduleServiceImpl.class);
    
    // [수정] EmplInfoRepository 대신, 상담사 정보(CnslrInfo) Repository를 주입받습니다.
    private final CnslrInfoRepository cnslrInfoRepository; 
    private final CnlrDefaultSchdRepository defaultSchdRepository;
    private final CnlrSchdRepository cnlrSchdRepository;
    
    @Override
    @Transactional(readOnly = true)
    public CounselorScheduleDto getCounselorSchedule(String counselorId, int year, int month) {
        log.info(">> [Service] 스케줄 조회 시작 | 상담사 ID: {}, 조회 연월: {}-{}", counselorId, year, month);

        // [수정] EmplInfo의 user.userId를 통해 조회하도록 변경
        List<CnlrDefaultSchd> defaults = defaultSchdRepository.findByEmployee_User_UserId(counselorId);
        List<DefaultScheduleDto> defaultDtos = defaults.stream()
            .map(entity -> {
                DefaultScheduleDto dto = new DefaultScheduleDto();
                dto.setDayOfWeek(entity.getDayOfWeek());
                dto.setWorkingDay(entity.getIsWorkingDay());
                dto.setStartTime(entity.getStartTime());
                dto.setEndTime(entity.getEndTime());
                return dto;
            }).collect(Collectors.toList());
            
        LocalDate startDate = LocalDate.of(year, month, 1);
        LocalDate endDate = startDate.withDayOfMonth(startDate.lengthOfMonth());
        List<CnlrSchd> exceptions = cnlrSchdRepository.findByEmployee_User_UserIdAndDayCodeBetween(counselorId, startDate, endDate);
        List<ScheduleExceptionDto> exceptionDtos = exceptions.stream()
            .map(entity -> {
                ScheduleExceptionDto dto = new ScheduleExceptionDto();
                dto.setExceptionDate(entity.getDayCode());
                dto.setStartTime(entity.getStartTime());
                dto.setEndTime(entity.getEndTime());
                return dto;
            }).collect(Collectors.toList());

        log.info(">> [Service] 스케줄 조회 완료 | 기본: {}건, 예외: {}건", defaultDtos.size(), exceptionDtos.size());
        
        return CounselorScheduleDto.builder()
                .defaultSchedules(defaultDtos)
                .exceptions(exceptionDtos)
                .build();
    }
    
    @Override
    @Transactional
    public void updateDefaultSchedules(String counselorId, List<DefaultScheduleDto> scheduleDtos) {
        log.info(">> [Service] 기본 근무시간 업데이트 시작 | 상담사 ID: {}", counselorId);

        // [수정] CnslrInfoRepository를 통해 상담사(EmplInfo) 정보를 가져옵니다.
        CnslrInfo cnslrInfo = cnslrInfoRepository.findByEmployee_User_UserId(counselorId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 상담사입니다. ID: " + counselorId));
        EmplInfo counselor = cnslrInfo.getEmployee();

        Map<Integer, CnlrDefaultSchd> existingSchedules = defaultSchdRepository.findByEmployee_User_UserId(counselorId)
                .stream().collect(Collectors.toMap(CnlrDefaultSchd::getDayOfWeek, Function.identity()));
        
        for (DefaultScheduleDto dto : scheduleDtos) {
            CnlrDefaultSchd schedule = existingSchedules.getOrDefault(dto.getDayOfWeek(), 
                CnlrDefaultSchd.builder()
                               .employee(counselor)
                               .dayOfWeek(dto.getDayOfWeek())
                               .build());
                               
            schedule.setIsWorkingDay(dto.isWorkingDay());
            schedule.setStartTime(dto.getStartTime());
            schedule.setEndTime(dto.getEndTime());
            
            defaultSchdRepository.save(schedule);
            log.info("... {}요일 근무시간 저장 완료", dto.getDayOfWeek());
        }
        log.info(">> [Service] 기본 근무시간 업데이트 완료 | 상담사 ID: {}", counselorId);
    }

    @Override
    @Transactional
    public void saveOrUpdateScheduleException(String counselorId, ScheduleExceptionDto exceptionDto) {
        LocalDate date = exceptionDto.getExceptionDate();
        log.info(">> [Service] 예외 스케줄 저장/수정 시작 | 상담사 ID: {}, 날짜: {}", counselorId, date);
        
        CnslrInfo cnslrInfo = cnslrInfoRepository.findByEmployee_User_UserId(counselorId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 상담사입니다. ID: " + counselorId));
        EmplInfo counselor = cnslrInfo.getEmployee();
        
        CnlrSchd exception = cnlrSchdRepository.findByEmployee_User_UserIdAndDayCode(counselorId, date)
                .orElse(CnlrSchd.builder()
                        .employee(counselor)
                        .dayCode(date)
                        .schdId("SCHD-" + System.currentTimeMillis()) // 임시 비즈니스 키 생성
                        .build());
        
        exception.setStartTime(exceptionDto.getStartTime());
        exception.setEndTime(exceptionDto.getEndTime());
        
        cnlrSchdRepository.save(exception);
        log.info(">> [Service] 예외 스케줄 저장/수정 완료");
    }

    @Override
    @Transactional
    public void deleteScheduleException(String counselorId, LocalDate date) {
        log.info(">> [Service] 예외 스케줄 삭제 시작 | 상담사 ID: {}, 날짜: {}", counselorId, date);
        
        cnlrSchdRepository.findByEmployee_User_UserIdAndDayCode(counselorId, date)
            .ifPresent(exception -> {
                cnlrSchdRepository.delete(exception);
                log.info("... 날짜 {}의 예외 스케줄 삭제 완료", date);
            });
    }
}
