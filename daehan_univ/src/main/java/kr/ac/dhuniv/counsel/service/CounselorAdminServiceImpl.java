package kr.ac.dhuniv.counsel.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import kr.ac.dhuniv.counsel.domain.CnslrInfo;
import kr.ac.dhuniv.counsel.dto.CounselorListDto;
import kr.ac.dhuniv.counsel.dto.CreateCounselorRequestDto;
import kr.ac.dhuniv.counsel.dto.UnregisteredEmpDto;
import kr.ac.dhuniv.counsel.dto.UpdateCounselorRequestDto;
import kr.ac.dhuniv.counsel.repository.CnslrInfoRepository;
import kr.ac.dhuniv.counsel.repository.EmplInfoRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CounselorAdminServiceImpl implements CounselorAdminService {

    private final CnslrInfoRepository cnslrInfoRepository;
    private final EmplInfoRepository emplInfoRepository;

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
    @Transactional // 데이터를 생성/수정/삭제 하므로 @Transactional 필요
    public void createCounselor(CreateCounselorRequestDto requestDto) {
        // 이미 등록된 교직원인지 확인하는 방어 로직 (선택)
        cnslrInfoRepository.findByEmplNo(requestDto.getEmplNo()).ifPresent(c -> {
            throw new IllegalArgumentException("이미 등록된 상담사입니다.");
        });

        // DTO를 Entity로 변환하여 생성
        CnslrInfo newCounselor = new CnslrInfo(
            requestDto.getEmplNo(),
            requestDto.getCnslSpec(),
            requestDto.getIsActive(),
            requestDto.getIntro()
        );
        
        cnslrInfoRepository.save(newCounselor);
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