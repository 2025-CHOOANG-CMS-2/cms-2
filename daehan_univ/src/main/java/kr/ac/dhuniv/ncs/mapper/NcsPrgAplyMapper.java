package kr.ac.dhuniv.ncs.mapper;

import kr.ac.dhuniv.ncs.dto.MyProgramDto;
import kr.ac.dhuniv.ncs.dto.NcsPrgAplyDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

@Mapper
public interface NcsPrgAplyMapper {

    /** 프로그램 현재 신청자 수 조회 */
    int countByProgramId(@Param("prgId") Long prgId);

    /** 특정 학생이 특정 프로그램에 신청했는지 확인 (중복 신청 방지용) */
    int existsByProgramIdAndStudentId(Map<String, Object> params);

    /** 신청 정보 등록 */
    int insert(NcsPrgAplyDto aplyDto);
    
    List<MyProgramDto> findMyProgramsByStudentId(@Param("stdId") Long stdId);
}