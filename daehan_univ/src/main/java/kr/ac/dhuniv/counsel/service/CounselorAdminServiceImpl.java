//package kr.ac.dhuniv.counsel.service;
//
//import java.time.DayOfWeek;
//import java.time.LocalDate;
//import java.time.LocalTime;
//import java.util.List;
//import java.util.stream.Collectors;
//
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//
//import kr.ac.dhuniv.counsel.domain.CnlrDefaultSchd;
//import kr.ac.dhuniv.counsel.domain.CnlrSchd;
//import kr.ac.dhuniv.counsel.domain.CnslrInfo;
//import kr.ac.dhuniv.counsel.dto.CounselorListDto;
//import kr.ac.dhuniv.counsel.dto.CreateCounselorRequestDto;
//import kr.ac.dhuniv.counsel.dto.UnregisteredEmpDto;
//import kr.ac.dhuniv.counsel.dto.UpdateCounselorRequestDto;
//import kr.ac.dhuniv.counsel.repository.CnlrDefaultSchdRepository;
//import kr.ac.dhuniv.counsel.repository.CnlrSchdRepository;
//import kr.ac.dhuniv.counsel.repository.CnslrInfoRepository;
//import kr.ac.dhuniv.counsel.repository.EmplInfoRepository;
//import kr.ac.dhuniv.empl_info.domain.EmplInfo;
//import lombok.RequiredArgsConstructor;
//
//@Service
//@RequiredArgsConstructor
//@Transactional(readOnly = true)
//public class CounselorAdminServiceImpl implements CounselorAdminService {
//
//    private final CnslrInfoRepository cnslrInfoRepository;
//    private final EmplInfoRepository emplInfoRepository;
//    private final CnlrSchdRepository cnlrSchdRepository;
//    private final CnlrDefaultSchdRepository cnlrDefaultSchdRepository;
//
//    @Override
//    public List<CounselorListDto> getCounselorList() {
//        return cnslrInfoRepository.findCounselorList();
//    }
//    
//    @Override
//    public CounselorListDto getCounselorDetail(String counselorId) {
//        return cnslrInfoRepository.findCounselorDetailByEmplNo(counselorId)
//                .orElseThrow(() -> new IllegalArgumentException("해당 상담사를 찾을 수 없습니다. ID: " + counselorId));
//    }
//    
//    @Override
//    @Transactional // 데이터를 변경하므로 @Transactional 필요
//    public void updateCounselor(String counselorId, UpdateCounselorRequestDto requestDto) {
//        // 1. DB에서 해당 상담사 정보를 찾음
//        CnslrInfo counselor = cnslrInfoRepository.findByEmplNo(counselorId)
//                .orElseThrow(() -> new IllegalArgumentException("해당 상담사를 찾을 수 없습니다."));
//
//        // 2. 찾아온 엔티티의 값을 DTO의 값으로 변경
//        counselor.update(requestDto.getCnslSpec(), requestDto.getIntro(), requestDto.getIsActive());
//    }
//    
//    @Override
//    public List<UnregisteredEmpDto> getUnregisteredEmployees() {
//        // 1. 네이티브 쿼리를 호출하여 Object[] 리스트를 받습니다.
//        List<Object[]> results = emplInfoRepository.findUnregisteredCounselorsNative();
//
//        // 2. 받아온 Object[] 리스트를 UnregisteredEmpDto 리스트로 변환합니다.
//        return results.stream()
//                .map(row -> new UnregisteredEmpDto(
//                        (String) row[0], // empl_no
//                        (String) row[1], // empl_nm
//                        (String) row[2], // empl_eml_addr
//                        (String) row[3]  // empl_telno
//                ))
//                .collect(Collectors.toList());
//    }
//    
//    @Override
//    @Transactional
//    public void createCounselor(CreateCounselorRequestDto requestDto) {
//        cnslrInfoRepository.findByEmplNo(requestDto.getEmplNo()).ifPresent(c -> {
//            throw new IllegalArgumentException("이미 등록된 상담사입니다.");
//        });
//        
//        // 1. emplNo로 EmplInfo 엔티티를 먼저 조회합니다.
//        EmplInfo employee = emplInfoRepository.findByEmplNo(requestDto.getEmplNo())
//                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 교직원입니다."));
//
//        // 2. 상담사 프로필 정보를 저장합니다.
//        CnslrInfo newCounselor = new CnslrInfo(
//            requestDto.getEmplNo(),
//            requestDto.getCnslSpec(),
//            requestDto.getIsActive(),
//            requestDto.getIntro()
//        );
//        cnslrInfoRepository.save(newCounselor);
//        
//     // [최종 수정 로직]
//        // CnlrSchd 테이블이 아닌 CnlrDefaultSchd 테이블에 기본 패턴을 저장합니다.
//        for (int i = 1; i <= 7; i++) {
//            boolean isWorking = (i >= 1 && i <= 5); // 월(1)~금(5)은 근무일
//            LocalTime startTime = isWorking ? LocalTime.of(9, 0) : null;
//            LocalTime endTime = isWorking ? LocalTime.of(17, 0) : null;
//
//            CnlrDefaultSchd defaultSchedule = CnlrDefaultSchd.builder()
//                    .employee(employee) // 이전에 조회한 EmplInfo 객체
//                    .dayOfWeek(i)      // 1:월요일, 2:화요일 ... 7:일요일
//                    .isWorkingDay(isWorking)
//                    .startTime(startTime)
//                    .endTime(endTime)
//                    .build();
//            
//            cnlrDefaultSchdRepository.save(defaultSchedule);
//        }
//    }
//    
//    @Override
//    @Transactional
//    public void deleteCounselor(String counselorId) {
//    	
//    	// [수정] 1. 관련된 '기본 반복 일정' 데이터부터 삭제
//        cnlrDefaultSchdRepository.deleteByEmployee_EmplNo(counselorId);
//    	
//        // 2. 관련된 '특정일 예외 일정' 데이터 삭제 (이전 코드)
//    	cnlrSchdRepository.deleteByEmployee_EmplNo(counselorId);
//    	
//        // 3. 상담사 프로필 정보 삭제
//        CnslrInfo counselor = cnslrInfoRepository.findByEmplNo(counselorId)
//                .orElseThrow(() -> new IllegalArgumentException("삭제할 상담사를 찾을 수 없습니다."));
//        
//        cnslrInfoRepository.delete(counselor);
//    }
//    
//    @Override
//    @Transactional
//    public void updateCounselorStatus(String counselorId, boolean isActive) {
//        CnslrInfo counselor = cnslrInfoRepository.findByEmplNo(counselorId)
//                .orElseThrow(() -> new IllegalArgumentException("해당 상담사를 찾을 수 없습니다."));
//        
//        counselor.changeStatus(isActive); // 엔티티의 상태 변경 메소드 호출
//    }
//}