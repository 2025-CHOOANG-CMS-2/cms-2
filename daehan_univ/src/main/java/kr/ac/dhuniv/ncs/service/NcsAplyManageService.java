package kr.ac.dhuniv.ncs.service;

import kr.ac.dhuniv.ncs.dto.NcsApplicationDto;
import kr.ac.dhuniv.ncs.mapper.NcsPrgAplyMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional
@RequiredArgsConstructor
public class NcsAplyManageService {

    private final NcsPrgAplyMapper ncsPrgAplyMapper;

    // 신청 목록 조회
    public Map<String, Object> getApplicationList(int page, int size) {
        Map<String, Object> params = new HashMap<>();
        params.put("offset", (page - 1) * size);
        params.put("size", size);
        
        List<NcsApplicationDto> list = ncsPrgAplyMapper.findApplications(params);
        int totalCount = ncsPrgAplyMapper.countApplications(params);
        
        Map<String, Object> result = new HashMap<>();
        result.put("list", list);
        result.put("totalCount", totalCount);
        return result;
    }

    // 신청 상세 정보 조회
    public NcsApplicationDto getApplicationDetail(Long aplyId) {
        return ncsPrgAplyMapper.findApplicationById(aplyId);
    }

    // 신청 승인
    public void approveApplication(Long aplyId) {
        ncsPrgAplyMapper.updateStatus(aplyId, "CONFIRM");
        // TODO: 승인 시 ncs_cmp_info 테이블에 'ING'(진행중) 상태로 데이터 추가하는 로직 필요
    }

    // 신청 반려
    public void rejectApplication(Long aplyId) {
        ncsPrgAplyMapper.updateStatus(aplyId, "REJECT");
    }
}