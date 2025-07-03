package kr.ac.dhuniv.ncs.service;

import kr.ac.dhuniv.ncs.dto.CompletionDto;
import kr.ac.dhuniv.ncs.mapper.NcsCmpInfoMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional
@RequiredArgsConstructor
public class NcsCompletionManageService {

    private final NcsCmpInfoMapper cmpInfoMapper;

    public Map<String, Object> getCompletionList(int page, int size) {
        Map<String, Object> params = new HashMap<>();
        params.put("offset", (page - 1) * size);
        params.put("size", size);
        
        List<CompletionDto> list = cmpInfoMapper.findCompletionList(params);
        int totalCount = cmpInfoMapper.countCompletionList(params);
        
        Map<String, Object> result = new HashMap<>();
        result.put("list", list);
        result.put("totalCount", totalCount);
        return result;
    }

    public void processCompletion(Long cmpId) {
        cmpInfoMapper.updateStatusToComplete(cmpId);
        // TODO: 이수 처리 시 마일리지 지급 등 추가 로직 구현
    }
}