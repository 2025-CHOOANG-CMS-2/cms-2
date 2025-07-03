package kr.ac.dhuniv.ncs.service;
import kr.ac.dhuniv.ncs.dto.ProgramDto;
import kr.ac.dhuniv.ncs.mapper.NcsPrgInfoMapper;
import org.springframework.stereotype.Service;
import jakarta.transaction.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class NcsPrgInfoService {
    private final NcsPrgInfoMapper mapper;
    
    public NcsPrgInfoService(NcsPrgInfoMapper mapper) {
        this.mapper = mapper;
    }
    
    public Map<String, Object> getList(Map<String, Object> params) {
        if (params.containsKey("page") && params.containsKey("size")) {
            int page = (Integer) params.get("page");
            int size = (Integer) params.get("size");
            params.put("offset", (page - 1) * size);
        }
        
        // 1. 데이터 목록 조회
        List<ProgramDto> list = mapper.selectList(params);
        // 2. 전체 개수 조회
        int totalCount = mapper.selectListCount(params);
        
        // 3. 목록과 전체 개수를 Map에 담아 반환
        Map<String, Object> result = new HashMap<>();
        result.put("list", list);
        result.put("totalCount", totalCount);
        
        return result;
    }
    
    public ProgramDto getOne(Long prgId) {
        return mapper.selectOne(prgId);
    }
    
    @Transactional
    public void create(ProgramDto dto) {
        // 1. 새로운 ID 생성 (현재 최대값 + 1)
        Long newId = mapper.getNextPrgId();
        dto.setPrgId(newId);
        
        // 2. 프로그램 코드 생성 (PRG + 7자리 ID)
        dto.setPrgCode("PRG" + String.format("%07d", newId));
        
        // 3. 마일리지 코드 생성
        String mileageCode = mapper.getNextMileageCode();
        dto.setMileageCode(mileageCode);
        
        // 4. 프로그램 등록
        mapper.insertProgram(dto);
        
        // 5. 마일리지 등록
        mapper.insertProgramMileage(dto);
    }
    
    @Transactional
    public void update(ProgramDto dto) {
        // 1. 프로그램 정보 수정
        mapper.updateProgram(dto);
        
        // 2. 마일리지 정보 수정
        mapper.updateProgramMileage(dto);
    }
    
    @Transactional
    public void delete(Long prgId) {
        try {
            // 1. 먼저 연관된 마일리지 데이터 삭제
            mapper.deleteProgramMileage(prgId);
            
            // 2. 그 다음 프로그램 삭제
            mapper.deleteProgram(prgId);
            
        } catch (Exception e) {
            throw new RuntimeException("프로그램 삭제 중 오류가 발생했습니다: " + e.getMessage());
        }
    }
}