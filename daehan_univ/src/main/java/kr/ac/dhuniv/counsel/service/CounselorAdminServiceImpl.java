package kr.ac.dhuniv.counsel.service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import kr.ac.dhuniv.counsel.domain.CnlrSchd;
import kr.ac.dhuniv.counsel.domain.CnslrInfo;
import kr.ac.dhuniv.counsel.dto.CounselorListDto;
import kr.ac.dhuniv.counsel.dto.CreateCounselorRequestDto;
import kr.ac.dhuniv.counsel.dto.UnregisteredEmpDto;
import kr.ac.dhuniv.counsel.dto.UpdateCounselorRequestDto;
import kr.ac.dhuniv.counsel.repository.CnlrSchdRepository;
import kr.ac.dhuniv.counsel.repository.CnslrInfoRepository;
import kr.ac.dhuniv.counsel.repository.EmplInfoRepository;
import kr.ac.dhuniv.empl_info.domain.EmplInfo;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CounselorAdminServiceImpl implements CounselorAdminService {

    private final CnslrInfoRepository cnslrInfoRepository;
    private final EmplInfoRepository emplInfoRepository;
    private final CnlrSchdRepository cnlrSchdRepository;

    @Override
    public List<CounselorListDto> getCounselorList() {
        return cnslrInfoRepository.findCounselorList();
    }
    
    @Override
    public CounselorListDto getCounselorDetail(String counselorId) {
        return cnslrInfoRepository.findCounselorDetailByEmplNo(counselorId)
                .orElseThrow(() -> new IllegalArgumentException("해당 상담사를 찾을 수 없습니다. ID: " + counselorId));
    }
    
    @Override
    @Transactional // 데이터를 변경하므로 @Transactional 필요
    public void updateCounselor(String counselorId, UpdateCounselorRequestDto requestDto) {
        // 1. DB에서 해당 상담사 정보를 찾음
        CnslrInfo counselor = cnslrInfoRepository.findByEmplNo(counselorId)
                .orElseThrow(() -> new IllegalArgumentException("해당 상담사를 찾을 수 없습니다."));

        // 2. 찾아온 엔티티의 값을 DTO의 값으로 변경
        counselor.update(requestDto.getCnslSpec(), requestDto.getIntro(), requestDto.getIsActive());
    }
    
    @Override
    public List<UnregisteredEmpDto> getUnregisteredEmployees() {
        // 1. 네이티브 쿼리를 호출하여 Object[] 리스트를 받습니다.
        List<Object[]> results = emplInfoRepository.findUnregisteredCounselorsNative();

        // 2. 받아온 Object[] 리스트를 UnregisteredEmpDto 리스트로 변환합니다.
        return results.stream()
                .map(row -> new UnregisteredEmpDto(
                        (String) row[0], // empl_no
                        (String) row[1], // empl_nm
                        (String) row[2], // empl_eml_addr
                        (String) row[3]  // empl_telno
                ))
                .collect(Collectors.toList());
    }
    
    @Override
    @Transactional
    public void createCounselor(CreateCounselorRequestDto requestDto) {
        cnslrInfoRepository.findByEmplNo(requestDto.getEmplNo()).ifPresent(c -> {
            throw new IllegalArgumentException("이미 등록된 상담사입니다.");
        });
        
        // 1. emplNo로 EmplInfo 엔티티를 먼저 조회합니다.
        EmplInfo employee = emplInfoRepository.findByEmplNo(requestDto.getEmplNo())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 교직원입니다."));

        // 2. 상담사 프로필 정보를 저장합니다.
        CnslrInfo newCounselor = new CnslrInfo(
            requestDto.getEmplNo(),
            requestDto.getCnslSpec(),
            requestDto.getIsActive(),
            requestDto.getIntro()
        );
        cnslrInfoRepository.save(newCounselor);
        
        // 3. [수정된 로직] 앞으로 다가올 평일 5일에 대한 기본 스케줄을 생성합니다.
        LocalDate today = LocalDate.now();
        int schedulesCreated = 0;
        int daysToAdd = 1;

        while (schedulesCreated < 5) {
            LocalDate nextDay = today.plusDays(daysToAdd);
            DayOfWeek dayOfWeek = nextDay.getDayOfWeek();
            
            // 주말(토,일)이 아니면 스케줄 생성
            if (dayOfWeek != DayOfWeek.SATURDAY && dayOfWeek != DayOfWeek.SUNDAY) {
                String scheduleId = "SCHD-" + nextDay.toString() + "-" + requestDto.getEmplNo();

                CnlrSchd defaultSchedule = CnlrSchd.builder()
                        .schdId(scheduleId) // schdId는 고유해야 하므로 생성 규칙이 필요합니다.
                        .employee(employee) // String이 아닌 EmplInfo 객체를 전달
                        .dayCode(nextDay)   // 요일 문자열이 아닌, 실제 날짜(LocalDate)를 전달
                        .startTime(LocalTime.of(9, 0))
                        .endTime(LocalTime.of(17, 0))
                        .build();
                
                cnlrSchdRepository.save(defaultSchedule);
                schedulesCreated++;
            }
            daysToAdd++;
        }
    }
    
    @Override
    @Transactional
    public void deleteCounselor(String counselorId) {
        // 먼저 삭제할 엔티티가 있는지 확인
        CnslrInfo counselor = cnslrInfoRepository.findByEmplNo(counselorId)
                .orElseThrow(() -> new IllegalArgumentException("삭제할 상담사를 찾을 수 없습니다."));
        
        // 엔티티를 직접 삭제 (물리적 삭제)
        cnslrInfoRepository.delete(counselor);
    }
    
    @Override
    @Transactional
    public void updateCounselorStatus(String counselorId, boolean isActive) {
        CnslrInfo counselor = cnslrInfoRepository.findByEmplNo(counselorId)
                .orElseThrow(() -> new IllegalArgumentException("해당 상담사를 찾을 수 없습니다."));
        
        counselor.changeStatus(isActive); // 엔티티의 상태 변경 메소드 호출
    }
}