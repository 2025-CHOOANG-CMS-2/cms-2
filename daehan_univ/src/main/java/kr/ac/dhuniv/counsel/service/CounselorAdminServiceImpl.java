package kr.ac.dhuniv.counsel.service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import kr.ac.dhuniv.counsel.domain.CnlrDefaultSchd;
import kr.ac.dhuniv.counsel.domain.CnslrInfo;
import kr.ac.dhuniv.counsel.dto.CounselorListDto;
import kr.ac.dhuniv.counsel.dto.CreateCounselorRequestDto;
import kr.ac.dhuniv.counsel.dto.UnregisteredEmpDto;
import kr.ac.dhuniv.counsel.dto.UpdateCounselorRequestDto;
import kr.ac.dhuniv.counsel.repository.CnlrDefaultSchdRepository;
import kr.ac.dhuniv.counsel.repository.CnlrSchdRepository;
import kr.ac.dhuniv.counsel.repository.CnslrInfoRepository;
import kr.ac.dhuniv.counsel.repository.EmplInfoRepository;
import kr.ac.dhuniv.empl_info.domain.EmplInfo;
import kr.ac.dhuniv.counsel.domain.*;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CounselorAdminServiceImpl implements CounselorAdminService {

    private final CnslrInfoRepository cnslrInfoRepository;
    private final EmplInfoRepository emplInfoRepository;
    private final CnlrSchdRepository cnlrSchdRepository;
    private final CnlrDefaultSchdRepository cnlrDefaultSchdRepository;

    @Override
    public List<CounselorListDto> getCounselorList() {
        return cnslrInfoRepository.findActiveCounselorsByFilter("all", "all");
    }
    
    @Override
    public CounselorListDto getCounselorDetail(String counselorId) {
        return cnslrInfoRepository.findCounselorDetailByUserId(counselorId)
                .orElseThrow(() -> new IllegalArgumentException("해당 상담사를 찾을 수 없습니다. ID: " + counselorId));
    }
    
    @Override
    @Transactional
    public void updateCounselor(String counselorId, UpdateCounselorRequestDto requestDto) {
        CnslrInfo counselor = cnslrInfoRepository.findByEmployee_User_UserId(counselorId)
                .orElseThrow(() -> new IllegalArgumentException("해당 상담사를 찾을 수 없습니다."));
        counselor.update(requestDto.getCnslSpec(), requestDto.getIntro(), requestDto.getIsActive());
    }
    
    @Override
    public List<UnregisteredEmpDto> getUnregisteredEmployees() {
        List<Object[]> results = emplInfoRepository.findUnregisteredCounselorsNative();
        return results.stream()
                .map(row -> new UnregisteredEmpDto((String) row[0], (String) row[1], (String) row[2], (String) row[3]))
                .collect(Collectors.toList());
    }
    
    @Override
    @Transactional
    public void createCounselor(CreateCounselorRequestDto requestDto) {
        cnslrInfoRepository.findByEmployee_User_UserId(requestDto.getEmplNo()).ifPresent(c -> {
            throw new IllegalArgumentException("이미 등록된 상담사입니다.");
        });
        
        EmplInfo employee = emplInfoRepository.findByUser_UserId(requestDto.getEmplNo())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 교직원입니다."));

        CnslrInfo newCounselor = CnslrInfo.builder()
            .employee(employee)
            .cnslSpec(requestDto.getCnslSpec())
            .isActive(requestDto.getIsActive())
            .intro(requestDto.getIntro())
            .build();
        cnslrInfoRepository.save(newCounselor);
        
        for (int i = 1; i <= 7; i++) {
            boolean isWorking = (i >= 1 && i <= 5);
            LocalTime startTime = isWorking ? LocalTime.of(9, 0) : null;
            LocalTime endTime = isWorking ? LocalTime.of(17, 0) : null;

            CnlrDefaultSchd defaultSchedule = CnlrDefaultSchd.builder()
                    .employee(employee)
                    .dayOfWeek(i)
                    .isWorkingDay(isWorking)
                    .startTime(startTime)
                    .endTime(endTime)
                    .build();
            
            cnlrDefaultSchdRepository.save(defaultSchedule);
        }
    }
    
    @Override
    @Transactional
    public void deleteCounselor(String counselorId) {
    	cnlrSchdRepository.deleteByEmployee_User_UserId(counselorId);
    	cnlrDefaultSchdRepository.deleteByEmployee_User_UserId(counselorId);
    	
        CnslrInfo counselor = cnslrInfoRepository.findByEmployee_User_UserId(counselorId)
                .orElseThrow(() -> new IllegalArgumentException("삭제할 상담사를 찾을 수 없습니다."));
        
        cnslrInfoRepository.delete(counselor);
    }
    
    @Override
    @Transactional
    public void updateCounselorStatus(String counselorId, boolean isActive) {
        CnslrInfo counselor = cnslrInfoRepository.findByEmployee_User_UserId(counselorId)
                .orElseThrow(() -> new IllegalArgumentException("해당 상담사를 찾을 수 없습니다."));
        
        counselor.changeStatus(isActive);
    }
}