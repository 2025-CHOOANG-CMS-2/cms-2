package kr.ac.dhuniv.ncs.mapper;
import java.util.List;
import java.util.Map;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import kr.ac.dhuniv.ncs.dto.ProgramDto;

@Mapper
public interface NcsPrgInfoMapper {
    List<ProgramDto> selectList(Map<String,Object> params);
    ProgramDto selectOne(@Param("prgId") Long prgId);
    
    // 다음 프로그램 ID 조회
    Long getNextPrgId();
    
    // 다음 마일리지 코드 조회 (추가)
    String getNextMileageCode();
    
    int insertProgram(ProgramDto dto);
    
    // 마일리지 등록 (추가)
    int insertProgramMileage(ProgramDto dto);
    
    int updateProgram(ProgramDto dto);
    
    // 마일리지 수정 (추가)
    int updateProgramMileage(ProgramDto dto);
    
    void deleteProgramMileage(Long prgId);
    int deleteProgram(@Param("prgId") Long prgId);
}