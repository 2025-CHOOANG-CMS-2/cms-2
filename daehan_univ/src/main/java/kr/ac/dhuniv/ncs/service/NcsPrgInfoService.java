package kr.ac.dhuniv.ncs.service;
import kr.ac.dhuniv.ncs.dto.ProgramDto;
import kr.ac.dhuniv.ncs.mapper.NcsPrgInfoMapper;
import org.springframework.stereotype.Service;
import jakarta.transaction.Transactional;
import java.util.List;
import java.util.Map;

@Service
public class NcsPrgInfoService {
    private final NcsPrgInfoMapper mapper;
    
    public NcsPrgInfoService(NcsPrgInfoMapper mapper) {
        this.mapper = mapper;
    }
    
    public List<ProgramDto> getList(Map<String, Object> params) {
        if (params.containsKey("page") && params.containsKey("size")) {
            int page = (Integer) params.get("page");
            int size = (Integer) params.get("size");
            params.put("offset", (page - 1) * size);
        }
        return mapper.selectList(params);
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